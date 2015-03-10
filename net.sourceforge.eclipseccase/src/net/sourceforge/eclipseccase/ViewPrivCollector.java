/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Sodergren - initial API and implementation
 *     Achim Bursian    - complete reworked for v2.2...
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import java.util.*;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * This class gathers view private elements in views.
 * <p>
 * The constructor takes an array of {@link IResource}s which should be
 * directories in which the search commands shall be performed. Typically, it is
 * called with one directory per used view.
 *
 * Strategy:
 * <ol>
 * <li>For each directory, find out if it exists in a snapshot or dynamic view.</li>
 * <li>For all views, perform "cleartool lscheckout"</li>
 * <li>For all dynamic views, perform the "cleartool lsprivate" command</li>
 * <li>For all resources in a snapshot view, perform "cleartools ls -view_only"</li>
 * </ol>
 *
 * <p>
 * Assumptions made by this class:
 * <ul>
 * <li>A project is associated with exactly one or no ClearCase view.
 * <li>A project can contain linked resources that point to elements in the same
 * view.
 * <li>A project can be associated with a dynamic or a snapshot view.
 * </ul>
 *
 * @author Achim Bursian
 * @author Tobias Sodergren
 *
 */
public class ViewPrivCollector {

	private static final String TRACE_ID = ViewPrivCollector.class
			.getSimpleName();

	private final Set<IResource> startupDirectories = new HashSet<IResource>();

	private final Map<String, ClearCaseElementState> elementStates = new HashMap<String, ClearCaseElementState>();

	private boolean findCheckedouts = true;

	private boolean findHijacked = true;

	private boolean findOthers = true;

	public ViewPrivCollector(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			startupDirectories.add(resource);
		}
	}

	private static  class RefreshSourceData {
		private final String viewName;
		private final boolean isSnapshot;
		private final List<IResource> resources = new ArrayList<IResource>();

		public RefreshSourceData(IProject project, IResource resource,
				String viewName, boolean isSnapshot) {
			this.resources.add(resource);
			this.viewName = viewName;
			this.isSnapshot = isSnapshot;
		}

		public String getViewName() {
			return viewName;
		}

		public boolean isSnapshot() {
			return isSnapshot;
		}

		public IResource[] getResources() {
			return resources.toArray(new IResource[resources.size()]);
		}

		public void addResource(IResource resource) {
			resources.add(resource);
		}
	}

	public void collectElements(final IProgressMonitor monitor) {
		monitor.beginTask("Collecting elements", IProgressMonitor.UNKNOWN);
		StateCacheFactory.getInstance().resetVerifiyStates();
		// Find all involved projects and whether they contain dynamic or
		// snapshot views
		Map<IProject, RefreshSourceData> projects = new HashMap<IProject, RefreshSourceData>();
		Iterator<IResource> resourceIterator = startupDirectories.iterator();
		while (resourceIterator.hasNext()) {
			IResource resource = resourceIterator.next();
			IProject project = resource.getProject();
			if (!projects.containsKey(project)) {
				boolean isSnapshotView;
				try {
					isSnapshotView = ClearCaseProvider.getViewType(resource)
							.equals("snapshot");
					projects.put(project, new RefreshSourceData(project,
							resource, ClearCaseProvider.getViewName(resource),
							isSnapshotView));
				} catch (NullPointerException e) {
				}
			} else {
				RefreshSourceData data = projects.get(project);
				data.addResource(resource);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

		}

		// For each project, perform the dynamic or snapshot listing strategy
		Set<String> queriedViews = new HashSet<String>();

		for (Map.Entry<IProject, RefreshSourceData> e : projects.entrySet()) {
			RefreshSourceData data = e.getValue();
			// now do something with key and value
			if (data.isSnapshot() == true) {
				trace("Refreshing snapshot view " + data.getViewName());
				gatherSnapshotViewElements(data.getResources()[0], data
						.getViewName(), queriedViews, monitor);
			} else {
				trace("Refreshing dynamic view " + data.getViewName());
				gatherDynamicViewElements(data.getResources()[0], data
						.getViewName(), queriedViews, monitor);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		StateCacheFactory.getInstance().refreshAllUnverifiedStates(
				findCheckedouts, findOthers, findHijacked);
		monitor.done();
	}

	/**
	 * Two step process for a dynamic view, find the checked-out elements (via
	 * lsco) and the view-private stuff (via lsprivate)
	 *
	 * @param workingdir
	 *            the top level dir
	 * @param viewName
	 * @param queriedViews
	 *            in/out, protocol all handled views
	 * @param monitor
	 *            for cancelation
	 */
	private void gatherDynamicViewElements(IResource workingdir,
			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {

		if (!queriedViews.contains(viewName)) {

			if (monitor.isCanceled())
				throw new OperationCanceledException();
			// TODO int steps = 0;

			// STEP 1:
			if (findCheckedouts) {
				addCheckedOutFiles(viewName, monitor, workingdir.getLocation(),
						false);
				if (monitor.isCanceled())
					throw new OperationCanceledException();
			}

			// STEP 2:
			if (findOthers) {
				String taskname = "View private in " + viewName;
				// processing getViewLSPrivateList line by line in
				// ViewprivOperationListener
				ClearCasePlugin.getEngine().getViewLSPrivateList(
						workingdir.getLocation().toOSString(),
						new ViewprivOperationListenerNF(taskname, monitor));
			}

			queriedViews.add(viewName);

		} else {
			// view was already processed... (?)
		}
	}

	/**
	 * Three step process for a snapshot view, find the checked-out elements
	 * (via lsco), then the hijacked elements (via a non-destructive update) and
	 * finally the view-private stuff (via ls -view_only)
	 *
	 * @param workingdir
	 *            the top level dir, hijacked elements are searched recursively
	 *            from here down
	 * @param viewName
	 *            name of the view
	 * @param queriedViews
	 *            in/out, protocol all handled views
	 * @param monitor
	 *            for cancelation
	 */
	private void gatherSnapshotViewElements(IResource workingdir,
			String viewName, Set<String> queriedViews, IProgressMonitor monitor) {

		if (!queriedViews.contains(viewName)) {

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			// ask for toplevel directory only once
			final String cwd = workingdir.getLocation().toOSString();
			final String topDir = ClearCasePlugin.getEngine().getViewRoot(cwd);

			if (topDir != null) {

				// STEP 1:
				if (findCheckedouts) {
					addCheckedOutFiles(viewName, monitor, workingdir
							.getLocation(), true);
					if (monitor.isCanceled())
						throw new OperationCanceledException();
				}

				// STEP 2:
				if (findHijacked) {
					trace("gatherSnapshotViewElements, findHijacked: " + cwd);
					ClearCasePlugin.getEngine().getUpdateList(
							cwd,
							new ViewprivOperationListenerHJ("Hijacked in "
									+ viewName, topDir, monitor));
					monitor.subTask("Hijacked in " + viewName
							+ ", processing list...");
					if (monitor.isCanceled())
						throw new OperationCanceledException();
				}

				// STEP 3:
				if (findOthers) {
					trace("gatherSnapshotViewElements, view_only: " + cwd);
					// process getCheckedOutElements line by line, not as array
					ClearCasePlugin.getEngine().getViewLSViewOnlyList(
							cwd,
							new ViewprivOperationListenerNF("Checked out in "
									+ viewName, monitor));
					monitor.subTask("View private in " + viewName
							+ ", processing list...");
					if (monitor.isCanceled())
						throw new OperationCanceledException();
				}

			}
			queriedViews.add(viewName);
		} else {
			// view was already processed... (?)
		}
	}

	private void addCheckedOutFiles(String viewName, IProgressMonitor monitor,
			IPath path, boolean isSnapshot) {
		String workingdir = path.toOSString();
		trace("addCheckedOutFiles, dir=: " + workingdir);
		// process getCheckedOutElements line by line, not as array
		monitor.subTask("Checked out in " + viewName + ", processing list...");
		ClearCasePlugin.getEngine().getCheckedOutElements(
				workingdir,
				isSnapshot,
				new ViewprivOperationListenerCO("Checked out in " + viewName,
						monitor));

	}

	public ClearCaseElementState getElementState(StateCache stateCache) {
		ClearCaseElementState state = elementStates.get(stateCache.getPath());
		if (state != null) {
			return state;
		}

		// Try the parent and see if it is marked as outside VOB
		String path = stateCache.getPath();
		String pathSeparator = "\\";
		if (!path.contains(pathSeparator)) {
			pathSeparator = "/";
		}
		String parentPath = path.substring(0, path.lastIndexOf(pathSeparator));
		ClearCaseElementState parentState = elementStates.get(parentPath);
		if (parentState != null && parentState.isOutsideVob()) {
			return new ClearCaseElementState(stateCache.getPath(),
					ClearCase.OUTSIDE_VOB);
		}

		return new ClearCaseElementState(stateCache.getPath(),
				ClearCase.CHECKED_IN | ClearCase.IS_ELEMENT);
	}

	private void trace(String message) {
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_ID, message);
		}

	}

	public boolean isFindCheckedouts() {
		return findCheckedouts;
	}

	public void setFindCheckedouts(boolean findCheckedouts) {
		this.findCheckedouts = findCheckedouts;
	}

	public boolean isFindOthers() {
		return findOthers;
	}

	public void setFindOthers(boolean findOthers) {
		this.findOthers = findOthers;
	}

	public boolean isFindHijacked() {
		return findHijacked;
	}

	public void setFindHijacked(boolean findHijacked) {
		this.findHijacked = findHijacked;
	}

}
