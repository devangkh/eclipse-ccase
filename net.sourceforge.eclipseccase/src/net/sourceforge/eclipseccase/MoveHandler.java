package net.sourceforge.eclipseccase;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.TeamException;

public class MoveHandler implements IMoveDeleteHook {

    ClearcaseProvider provider;

    /**
     * Constructor for MoveHandler.
     */
    public MoveHandler(ClearcaseProvider provider) {
        this.provider = provider;
    }

    /*
     * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int,
     *      IProgressMonitor)
     */
    public boolean deleteFile(IResourceTree tree, IFile file, int updateFlags,
            IProgressMonitor monitor) {
        if (provider.isIgnored(file) || file.isLinked()
                || !provider.hasRemote(file)) {
            tree.standardDeleteFile(file, updateFlags, monitor);
            return true;
        }

        boolean failed = false;
        IStatus status = null;

        if ((IResource.FORCE & updateFlags) != 0
                && !tree.isSynchronized(file, IResource.DEPTH_INFINITE)) {
            failed = true;
            status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                    TeamException.UNABLE, "Tree not synchronized", null);
        }
        if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0) {
            tree.addToLocalHistory(file);
        }

        if (!failed) {
            synchronized (provider) {
                provider.refreshResources = false;

                try {
                    provider.delete(new IResource[] { file }, monitor);
                } catch (TeamException ex) {
                    failed = true;
                    status = ex.getStatus();
                } finally {
                    provider.refreshResources = true;
                }
            }
        }

        if (failed)
            tree.failed(status);
        else
            tree.deletedFile(file);

        return true;
    }

    /*
     * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int,
     *      IProgressMonitor)
     */
    public boolean deleteFolder(IResourceTree tree, IFolder folder,
            int updateFlags, IProgressMonitor monitor) {
        if (provider.isIgnored(folder) || folder.isLinked()
                || !provider.hasRemote(folder)) {
            tree.standardDeleteFolder(folder, updateFlags, monitor);
            return true;
        }

        boolean failed = false;
        IStatus status = null;

        if ((IResource.FORCE & updateFlags) != 0
                && !tree.isSynchronized(folder, IResource.DEPTH_INFINITE)) {
            failed = true;
            status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                    TeamException.UNABLE, "Tree not synchronized", null);
        }
        if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0) {
            // Have to do this recursively for children?
            //tree.addToLocalHistory(folder);
        }

        if (!failed) {
            synchronized (provider) {
                provider.refreshResources = false;

                try {
                    provider.delete(new IResource[] { folder }, monitor);
                } catch (TeamException ex) {
                    failed = true;
                    status = ex.getStatus();
                } finally {
                    provider.refreshResources = true;
                }
            }
        }

        if (failed)
            tree.failed(status);
        else
            tree.deletedFolder(folder);

        return true;
    }

    /*
     * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int,
     *      IProgressMonitor)
     */
    public boolean deleteProject(IResourceTree tree, IProject project,
            int updateFlags, IProgressMonitor monitor) {
        if (provider.isIgnored(project) || !provider.hasRemote(project)) {
            tree.standardDeleteProject(project, updateFlags, monitor);
            return true;
        }

        boolean failed = false;
        IStatus status = null;

        if ((IResource.FORCE & updateFlags) != 0
                && !tree.isSynchronized(project, IResource.DEPTH_INFINITE)) {
            failed = true;
            status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                    TeamException.UNABLE, "Tree not synchronized", null);
        }
        // KEEP_HISTORY not needed for project delete

        if (project.isOpen()) {
            // If project open, don't delete cotents if flag is true
            if ((IResource.NEVER_DELETE_PROJECT_CONTENT & updateFlags) != 0) {
                tree.deletedProject(project);
                return true;
            }
        } else {
            // If project closed, don't delete cotents if flag is false
            if ((IResource.ALWAYS_DELETE_PROJECT_CONTENT & updateFlags) == 0) {
                tree.deletedProject(project);
                return true;
            }
        }

        if (!failed) {
            synchronized (provider) {
                provider.refreshResources = false;

                try {
                    provider.delete(new IResource[] { project }, monitor);
                } catch (TeamException ex) {
                    failed = true;
                    status = ex.getStatus();
                } finally {
                    provider.refreshResources = true;
                }
            }
        }

        if (failed)
            tree.failed(status);
        else
            tree.deletedProject(project);

        return true;
    }

private IStatus validateDest(IResource destination, IProgressMonitor monitor)
    {
        IStatus status = new Status(IStatus.OK, ClearcaseProvider.ID,
                TeamException.OK, "OK", null);
        IContainer destParent = destination.getParent();
        if (!provider.hasRemote(destParent))
        {
            if (ClearcasePlugin.isAddAuto())
            {
                try
                {
                    LinkedList toAdd = new LinkedList();
                    while(null != destParent && !provider.hasRemote(destParent)) 
                    {
                        if(destParent.getType() == IResource.ROOT)
                        {
                            // workspace root should be in ClearCase
                            throw new TeamException("No ClearCase parent found for: " + destination.getLocation());
                        }
                        toAdd.addFirst(destParent);
                        destParent = destParent.getParent();
                    }
                    provider.add((IResource[]) toAdd.toArray(new IResource[toAdd.size()]),
                            IResource.DEPTH_ZERO, monitor);
                }
                catch (TeamException ex)
                {
                    status = ex.getStatus();
                }
            }
            else
            {
                status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                        TeamException.UNABLE,
                        "Destination folder is not a ClearCase element: "
                                + destParent.getLocation().toOSString(), null);
            }
        }
        return status;
    }    /*
          * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int,
          *      IProgressMonitor)
          */
    public boolean moveFile(IResourceTree tree, IFile source,
            IFile destination, int updateFlags, IProgressMonitor monitor) {
        if (provider.isIgnored(source) || source.isLinked()
                || !provider.hasRemote(source)) {
            tree.standardMoveFile(source, destination, updateFlags
                    | IResource.SHALLOW, monitor);
            return true;
        }
        synchronized (provider) {
            provider.refreshResources = false;

            try {
                monitor.beginTask("Moving " + source.getName(), 100);

                IStatus status = validateDest(destination,
                        new SubProgressMonitor(monitor, 40));

                if ((IResource.FORCE & updateFlags) != 0
                        && !tree.isSynchronized(source,
                                IResource.DEPTH_INFINITE)) {
                    status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                            TeamException.UNABLE, "Tree not synchronized", null);
                }

                if (status.getCode() == IStatus.OK
                        && (IResource.KEEP_HISTORY & updateFlags) != 0) {
                    tree.addToLocalHistory(source);
                }

                if (status.getCode() == IStatus.OK) {
                    status = provider.move(source, destination,
                            new SubProgressMonitor(monitor, 40));
                }

                if (status.getCode() == IStatus.OK)
                    tree.movedFile(source, destination);
                else
                    tree.failed(status);

                return true;

            } finally {
                provider.refreshResources = true;
                monitor.done();
            }
        }
    }

    /*
     * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int,
     *      IProgressMonitor)
     */
    public boolean moveFolder(IResourceTree tree, IFolder source,
            IFolder destination, int updateFlags, IProgressMonitor monitor) {
        if (provider.isIgnored(source) || source.isLinked()
                || !provider.hasRemote(source)) {
            tree.standardMoveFolder(source, destination, updateFlags
                    | IResource.SHALLOW, monitor);
            return true;
        }

        synchronized (provider) {
            provider.refreshResources = false;

            try {
                monitor.beginTask("Moving " + source.getName(), 100);

                IStatus status = validateDest(destination,
                        new SubProgressMonitor(monitor, 40));

                if ((IResource.FORCE & updateFlags) != 0
                        && !tree.isSynchronized(source,
                                IResource.DEPTH_INFINITE)) {
                    status = new Status(IStatus.ERROR, ClearcaseProvider.ID,
                            TeamException.UNABLE, "Tree not synchronized", null);
                }
                if (status.getCode() == IStatus.OK
                        && (IResource.KEEP_HISTORY & updateFlags) != 0) {
                    // Have to do this recursively for children?
                    //tree.addToLocalHistory(source);
                }

                if (status.getCode() == IStatus.OK) {
                    status = provider.move(source, destination,
                            new SubProgressMonitor(monitor, 40));
                }

                if (status.getCode() == IStatus.OK)
                    tree.movedFolderSubtree(source, destination);
                else
                    tree.failed(status);

                return true;
            } finally {
                provider.refreshResources = true;
                monitor.done();
            }
        }
    }

    /*
     * @see IMoveDeleteHook#moveProject(IResourceTree, IProject,
     *      IProjectDescription, int, IProgressMonitor)
     */
    public boolean moveProject(IResourceTree tree, IProject source,
            IProjectDescription description, int updateFlags,
            IProgressMonitor monitor) {
        StateCacheFactory.getInstance().remove(source);
        // return false to handle a standard move
        return false;
    }

}