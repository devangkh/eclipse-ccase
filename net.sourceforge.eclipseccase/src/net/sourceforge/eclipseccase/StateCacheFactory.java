/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Matthew Conway - initial API and implementation IBM Corporation -
 * concepts and ideas from Eclipse Gunnar Wagenknecht - new features,
 * enhancements and bug fixes
 ******************************************************************************/
package net.sourceforge.eclipseccase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.eclipseccase.tools.XMLWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class StateCacheFactory implements ISaveParticipant,
        IResourceChangeListener {

    /** trace id */
    private static final String TRACE_STATECACHEFACTORY = "StateCacheFactory"; //$NON-NLS-1$

    private static final String SAVE_FILE_NAME = "statecache"; //$NON-NLS-1$

    /** the singleton instance */
    private static StateCacheFactory instance = new StateCacheFactory();

    /** maps resources to caches */
    HashMap cacheMap = new HashMap();

    /** the listeners */
    private List listeners = new ArrayList();

    /**
     * Hidden constructor.
     */
    private StateCacheFactory() {
        // super
    }

    /**
     * Returns the shared instance.
     * 
     * @return
     */
    public static StateCacheFactory getInstance() {
        return instance;
    }

    /**
     * Adds a state change listener.
     * 
     * @param listener
     */
    public void addStateChangeListerer(IResourceStateListener listener) {
        if (null != listener) {
            synchronized (listeners) {
                if (!listeners.contains(listener)) listeners.add(listener);
            }
        }
    }

    /**
     * Removes a state change listener.
     * 
     * @param listener
     * @return
     */
    public boolean removeStateChangeListerer(IResourceStateListener listener) {
        if (null != listener) {
            synchronized (listeners) {
                return listeners.remove(listener);
            }
        }
        return false;
    }

    /**
     * Fires a state change for the specified state cache.
     * 
     * @param stateCache
     */
    public void fireStateChanged(IResource resource) {

        if (operationCounter > 0) {

            // only queue if there are ongoing operations
            if (null == resource) return;
            synchronized (queuedEvents) {
                queuedEvents.addLast(resource);
            }
        } else {

            // fire event or queued events
            Object[] currentListeners = null;
            synchronized (listeners) {
                currentListeners = listeners.toArray();
            }

            if (null != currentListeners) {
                if (null == resource) {

                    // fire all pending changes
                    Object[] events = null;
                    synchronized (queuedEvents) {
                        events = queuedEvents.toArray();
                        queuedEvents.clear();
                    }
                    if (null != events) {
                        for (int i = 0; i < events.length; i++) {
                            resource = (IResource) events[i];
                            for (int j = 0; j < currentListeners.length; j++) {
                                ((IResourceStateListener) currentListeners[j])
                                        .resourceStateChanged(resource);
                            }
                        }
                    }
                } else {

                    // fire only the given change
                    for (int i = 0; i < currentListeners.length; i++) {
                        ((IResourceStateListener) currentListeners[i])
                                .resourceStateChanged(resource);
                    }
                }
            }
        }
    }

    /** a list of events queued during long running operations */
    private LinkedList queuedEvents = new LinkedList();

    /** the operation counter */
    private int operationCounter = 0;

    /**
     * Starts an operation.
     */
    public void operationBegin() {
        synchronized (this) {
            operationCounter++;
        }
    }

    /**
     * Ends an operation.
     */
    public void operationEnd() {
        boolean fire = false;
        synchronized (this) {
            if (operationCounter == 0) return;

            operationCounter--;

            fire = operationCounter == 0;
        }

        if (fire) {
            fireStateChanged(null);
        }
    }

    /**
     * Indicates if the state of the specified resource is uninitialized.
     * 
     * @param resource
     * @return <code>true</code> if uninitialized
     */
    public boolean isUnitialized(IResource resource) {
        if (!cacheMap.containsKey(resource)) return true;

        return ((StateCache) cacheMap.get(resource)).isUninitialized();
    }

    /**
     * Returns the state cache fo the specified resource.
     * 
     * @param resource
     * @return the state cache fo the specified resource
     */
    public StateCache get(IResource resource) {
        StateCache cache = (StateCache) cacheMap.get(resource);
        if (cache == null) {

            synchronized (cacheMap) {
                cache = (StateCache) cacheMap.get(resource);
                if (null == cache) {
                    cache = new StateCache(resource);
                    cacheMap.put(resource, cache);
                }
            }

            // schedule update if necessary
            if (cache.isUninitialized()) cache.updateAsync(false);
        }
        return cache;
    }

    /**
     * Removes the state cache for the specified resource including all its
     * direct and indirect members.
     * 
     * @param resource
     */
    public void remove(IResource resource) {
        if (resource.isAccessible()) {
            try {
                resource.accept(new IResourceVisitor() {

                    public boolean visit(IResource childResource)
                            throws CoreException {
                        switch (childResource.getType()) {
                        case IResource.PROJECT:
                        case IResource.FOLDER:
                            removeSingle(childResource);
                            return true;
                        default:
                            removeSingle(childResource);
                            return false;
                        }
                    }
                });
            } catch (CoreException ex) {
                // not accessible
            }
        }
        removeSingle(resource);
    }

    /**
     * Removes the state cache for the specified resource.
     * 
     * @param resource
     */
    void removeSingle(IResource resource) {
        if (cacheMap.containsKey(resource)) {
            synchronized (cacheMap) {
                cacheMap.remove(resource);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
     */
    public void doneSaving(ISaveContext context) {
        int previousSaveNumber = context.getPreviousSaveNumber();
        String oldFileName = SAVE_FILE_NAME
                + Integer.toString(previousSaveNumber);
        File file = ClearcasePlugin.getInstance().getStateLocation().append(
                oldFileName).toFile();
        file.delete();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
     */
    public void prepareToSave(ISaveContext context) throws CoreException {
        // prepareToSave
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
     */
    public void rollback(ISaveContext context) {
        // rollback
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
     */
    public void saving(ISaveContext context) throws CoreException {
        switch (context.getKind()) {

        case ISaveContext.FULL_SAVE:
            try {
                int saveNumber = context.getSaveNumber();
                String saveFileName = SAVE_FILE_NAME
                        + Integer.toString(saveNumber);
                IPath statePath = ClearcasePlugin.getInstance()
                        .getStateLocation().append(saveFileName);

                // save state cache
                //ObjectOutputStream os = new ObjectOutputStream(
                //        new FileOutputStream(statePath.toFile()));
                //Collection serList = new LinkedList(cacheMap.values());
                //os.writeObject(serList);
                //os.flush();
                //os.close();
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(statePath.toFile()));
                try {
                    writeStateCache(os);
                    os.flush();
                } finally {
                    os.close();
                }
                context.map(new Path(SAVE_FILE_NAME), new Path(saveFileName));
                context.needSaveNumber();
            } catch (IOException ex) {
                throw new CoreException(new Status(IStatus.WARNING,
                        ClearcasePlugin.PLUGIN_ID, TeamException.IO_FAILED,
                        "Could not persist state cache", ex)); //$NON-NLS-1$
            }
            break;

        case ISaveContext.PROJECT_SAVE:
        case ISaveContext.SNAPSHOT:
            break;
        }
    }

    /** xml element name */
    static final String ELEMENT_RESOURCE = "resource"; //$NON-NLS-1$

    /** xml element name */
    static final String ELEMENT_STATES = "states"; //$NON-NLS-1$

    /** xml attribute name */
    static final String ATTR_PATH = "path"; //$NON-NLS-1$

    /** xml attribute name */
    static final String ATTR_VERSION = "version"; //$NON-NLS-1$

    /** xml attribute name */
    static final String ATTR_TIME_STAMP = "timeStamp"; //$NON-NLS-1$

    /** xml attribute name */
    static final String ATTR_STATE = "state"; //$NON-NLS-1$

    /** xml attribute name */
    static final String ATTR_SYMLINK_TARGET = "symlinkTarget"; //$NON-NLS-1$

    /** the version */
    static final String STATE_CACHE_VERSION = "20040617_104400_GMT+0200"; //$NON-NLS-1$

    /**
     * Writes the comment history to the specified writer.
     * 
     * @param writer
     * @throws IOException
     */
    private void writeStateCache(OutputStream os) throws IOException {

        // create XML writer
        XMLWriter writer = new XMLWriter(os);
        Set knownResource = cacheMap.keySet();

        // get and sort resources
        IResource[] resources = (IResource[]) knownResource
                .toArray(new IResource[knownResource.size()]);
        Arrays.sort(resources, new Comparator() {

            public int compare(Object o1, Object o2) {
                return ((IResource) o1).getFullPath().toString().compareTo(
                        ((IResource) o2).getFullPath().toString());
            }
        });

        // start root tag
        HashMap attributes = new HashMap(1);
        attributes.put(ATTR_VERSION, STATE_CACHE_VERSION);
        writer.startTag(ELEMENT_STATES, attributes, true);

        // write resource tags
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            // only persist state of initialized, existing and non derived
            // resources
            if (isUnitialized(resource) || !resource.exists()
                    || resource.isDerived()) continue;
            StateCache cache = get(resource);
            attributes = new HashMap(5);
            attributes.put(ATTR_PATH, resource.getFullPath().toString());
            attributes.put(ATTR_STATE, Integer.toString(cache.flags));
            attributes.put(ATTR_TIME_STAMP, Long
                    .toString(cache.updateTimeStamp));
            if (null != cache.version)
                    attributes.put(ATTR_VERSION, cache.version);
            if (null != cache.symbolicLinkTarget)
                    attributes.put(ATTR_SYMLINK_TARGET,
                            cache.symbolicLinkTarget);
            writer.startAndEndTag(ELEMENT_RESOURCE, attributes, true);
        }

        // finish root TAG
        writer.endTag(ELEMENT_STATES);
        writer.flush();
    }

    /**
     * Loads the state cache from the specified context.
     * 
     * @param context
     */
    void load(ISavedState context) {
        try {
            if (context != null) {
                String saveFileName = context.lookup(new Path(SAVE_FILE_NAME))
                        .toString();
                File stateFile = ClearcasePlugin.getInstance()
                        .getStateLocation().append(saveFileName).toFile();
                if (stateFile.exists()) {
                    //ObjectInputStream is = new ObjectInputStream(
                    //        new FileInputStream(stateFile));
                    //Collection values = (Collection) is.readObject();
                    //for (Iterator iter = values.iterator(); iter.hasNext();)
                    // {
                    //    StateCache element = (StateCache) iter.next();
                    //    IResource resource = element.getResource();
                    //    if (resource != null && resource.isAccessible()) {
                    //        synchronized (cacheMap) {
                    //            cacheMap.put(resource, element);
                    //        }
                    //
                    //        // inform listeners about a new state
                    //        fireStateChanged(resource);
                    //    }
                    //}
                    //is.close();
                    BufferedInputStream is = new BufferedInputStream(
                            new FileInputStream(stateFile));
                    try {
                        readStateCache(is);
                    } finally {
                        is.close();
                    }
                }
            }
        } catch (Exception ex) {
            ClearcasePlugin
                    .log(
                            IStatus.WARNING,
                            "Could not load saved clearcase state cache, resetting cache", //$NON-NLS-1$
                            ex);
        }
    }

    /**
     * Builds (reads) the state cache from the specified input stream.
     * 
     * @param stream
     * @throws Exception
     * @throws CoreException
     */
    private void readStateCache(InputStream stream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        InputSource source = new InputSource(stream);
        Document document = parser.parse(source);
        NodeList list = document.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;

                // find root node
                if (ELEMENT_STATES.equals(element.getTagName())) {

                    // check version
                    if (!STATE_CACHE_VERSION.equals(element
                            .getAttribute(ATTR_VERSION))) return;

                    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
                            .getRoot();
                    // process resources
                    NodeList resources = element
                            .getElementsByTagName(ELEMENT_RESOURCE);
                    for (int j = 0; j < resources.getLength(); j++) {
                        Element resourceElement = (Element) resources.item(j);
                        String resourcePath = resourceElement
                                .getAttribute(ATTR_PATH);
                        if (null == resourcePath) continue;

                        // determine resource
                        IPath path = new Path(resourcePath);
                        IResource resource = root.findMember(path);
                        if (resource != null && resource.isAccessible()) {
                            try {
                                // create cache
                                StateCache cache = new StateCache(resource);
                                cache.flags = Integer.parseInt(resourceElement
                                        .getAttribute(ATTR_STATE));
                                cache.version = resourceElement
                                        .getAttribute(ATTR_VERSION);
                                cache.symbolicLinkTarget = resourceElement
                                        .getAttribute(ATTR_SYMLINK_TARGET);
                                cache.updateTimeStamp = Long
                                        .parseLong(resourceElement
                                                .getAttribute(ATTR_TIME_STAMP));

                                // store cache
                                synchronized (cacheMap) {
                                    cacheMap.put(resource, cache);
                                }

                                if (ClearcasePlugin.DEBUG_STATE_CACHE) {
                                    ClearcasePlugin.trace(
                                            TRACE_STATECACHEFACTORY,
                                            "state cache restored: " + cache); //$NON-NLS-1$
                                }

                                // fire change
                                fireStateChanged(resource);
                            } catch (RuntimeException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {

        try {
            IResourceDelta rootDelta = event.getDelta();
            if (null != rootDelta) {
                IResourceDelta[] projectDeltas = rootDelta
                        .getAffectedChildren();

                // determine resources to refresh
                final List toRefresh = new ArrayList();

                for (int i = 0; i < projectDeltas.length; i++) {
                    IResourceDelta projectDelta = projectDeltas[i];

                    // filter only shared projects
                    if (RepositoryProvider.isShared((IProject) projectDelta
                            .getResource())) {
                        if (!isAffectedBy(rootDelta)) continue;

                        projectDelta.accept(new IResourceDeltaVisitor() {

                            public boolean visit(IResourceDelta delta)
                                    throws CoreException {
                                switch (delta.getKind()) {
                                case IResourceDelta.REMOVED:
                                    // only remove cache
                                    removeSingle(delta.getResource());
                                    break;

                                default:
                                    if (needsRefresh(delta)) {
                                        // refresh cache
                                        toRefresh.add(delta.getResource());
                                    }
                                }

                                return true;
                            }
                        });
                    }
                }

                if (!toRefresh.isEmpty()) {
                    refreshStateAsync((IResource[]) toRefresh
                            .toArray(new IResource[toRefresh.size()]));
                }
            }
        } catch (CoreException e) {
            ClearcasePlugin.log(IStatus.ERROR,
                    "Unable to do a quick update of resource", e); //$NON-NLS-1$
        }
    }

    /**
     * Refreshes the state of the specified resources.
     * 
     * @param resources
     */
    void refreshStateAsync(IResource[] resources) {
        StateCacheJob[] jobs = new StateCacheJob[resources.length];
        for (int i = 0; i < resources.length; i++) {
            StateCache cache = StateCacheFactory.getInstance()
                    .get(resources[i]);
            jobs[i] = new StateCacheJob(cache);
        }
        getJobQueue().schedule(jobs);
    }

    /**
     * Refreshes the state of the specified resources.
     * 
     * @param resources
     */
    void refreshStateAsyncHighPriority(IResource[] resources) {
        StateCacheJob[] jobs = new StateCacheJob[resources.length];
        for (int i = 0; i < resources.length; i++) {
            StateCache cache = StateCacheFactory.getInstance()
                    .get(resources[i]);
            jobs[i] = new StateCacheJob(cache, StateCacheJob.PRIORITY_HIGH);
        }
        getJobQueue().schedule(jobs);
    }

    /**
     * Indicates if the resource delta is really interesting for a refresh.
     * 
     * @param delta
     * @return
     */
    static boolean needsRefresh(IResourceDelta delta) {
        IResource resource = delta.getResource();

        // ignore linked folders
        if (resource.isLinked()) return false;

        // check the global ignores from Team (includes derived resources)
        if (Team.isIgnoredHint(resource)) return false;

        int interestingChangeFlags = IResourceDelta.CONTENT
                | IResourceDelta.SYNC | IResourceDelta.REPLACED
                | IResourceDelta.DESCRIPTION | IResourceDelta.OPEN
                | IResourceDelta.TYPE;

        if (delta.getKind() == IResourceDelta.ADDED
                || (delta.getKind() == IResourceDelta.CHANGED && 0 != (delta
                        .getFlags() & interestingChangeFlags))) {

            //System.out
            //        .println(((org.eclipse.core.internal.events.ResourceDelta) delta)
            //                .toDebugString());

            // only refresh if current state is not checked out
            return !getInstance().isUnitialized(resource)
                    && !getInstance().get(resource).isCheckedOut();
        }

        //System.out.println("ignored: " + delta);
        return false;
    }

    /**
     * Returns whether a given delta contains some information relevant to the
     * resource state, in particular it will not consider MARKER only deltas.
     */
    static boolean isAffectedBy(IResourceDelta rootDelta) {
        //if (rootDelta == null) System.out.println("NULL DELTA");
        //long start = System.currentTimeMillis();
        if (rootDelta != null) {
            // use local exception to quickly escape from delta traversal
            class FoundRelevantDeltaException extends RuntimeException {
                // empty
            }
            try {
                rootDelta.accept(new IResourceDeltaVisitor() {

                    public boolean visit(IResourceDelta delta)
                            throws CoreException {
                        switch (delta.getKind()) {
                        case IResourceDelta.ADDED:
                        case IResourceDelta.REMOVED:
                            throw new FoundRelevantDeltaException();
                        case IResourceDelta.CHANGED:
                            // if any flag is set but MARKER, this delta should
                            // be considered
                            if (delta.getAffectedChildren().length == 0 // only
                                    // check
                                    // leaf
                                    // delta
                                    // nodes
                                    && (delta.getFlags() & ~IResourceDelta.MARKERS) != 0) { throw new FoundRelevantDeltaException(); }
                        }
                        return true;
                    }
                });
            } catch (FoundRelevantDeltaException e) {
                //System.out.println("RELEVANT DELTA detected in: "+
                // (System.currentTimeMillis() - start));
                return true;
            } catch (CoreException e) { // ignore delta if not able to traverse
            }
        }
        //System.out.println("IGNORE MARKER DELTA took: "+
        // (System.currentTimeMillis() - start));
        return false;
    }

    private final StateCacheJobQueue jobQueue = new StateCacheJobQueue();

    /**
     * Returns the job queue.
     * 
     * @return the job queue
     */
    StateCacheJobQueue getJobQueue() {
        return jobQueue;
    }

    /**
     * Ensures the state cache for the specified resource is initialized.
     * 
     * @param resource
     */
    void ensureInitialized(IResource resource) {
        StateCache cache = get(resource);
        if (cache.isUninitialized()) cache.doUpdate();
    }
}