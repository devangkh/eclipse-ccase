package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;

import org.apache.tools.ant.taskdefs.Execute;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.TeamException;

public class AddToClearcaseAction extends ClearcaseWorkspaceAction {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        String maybeComment = "";
        int maybeDepth = IResource.DEPTH_ZERO;

        if (!ClearcasePlugin.isUseClearDlg() && ClearcasePlugin.isCommentAdd()) {
            CommentDialog dlg = new CommentDialog(shell,
                    "Add to clearcase comment");
            if (dlg.open() == Window.CANCEL) return;
            maybeComment = dlg.getComment();
            maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE
                    : IResource.DEPTH_ZERO;
        }

        final String comment = maybeComment;
        final int depth = maybeDepth;

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Adding...", resources.length);
                    if (ClearcasePlugin.isUseClearDlg()) {
                        monitor.subTask("Executing ClearCase user interface...");
                        ClearDlgHelper.add(resources);
                    } else {
                        for (int i = 0; i < resources.length; i++) {
                            IResource resource = resources[i];
                            ClearcaseProvider provider = ClearcaseProvider
                                    .getClearcaseProvider(resource);
                            provider.setComment(comment);
                            provider.add(resources, depth, subMonitor(monitor));
                        }
                    }
                } finally {
                    monitor.done();
                }
            }

        };

        executeInBackground(runnable, "Adding resources to ClearCase");
    }

    private static final String DEBUG_ID = "AddToClearCaseAction";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
     */
    protected boolean isEnabled() throws TeamException {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0) return false;
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider
                    .getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource)
                    || provider.isIgnored(resource)) return false;

            // Projects may be the view directory containing the VOBS, if so,
            // don't want to be able to add em, or any resource diretcly under
            // them
            if (resource.getType() == IResource.PROJECT
                    && !provider.hasRemote(resource)) {
                ClearcasePlugin.debug(DEBUG_ID,
                        "disabled for project without remote: " + resource);
                return false;
            }
            if (resource.getParent().getType() == IResource.PROJECT
                    && !provider.hasRemote(resource.getParent())) {
                ClearcasePlugin.debug(DEBUG_ID, "disabled for " + resource
                        + " because parent is project without remote: "
                        + resource.getParent());
                return false;
            }
            if (provider.hasRemote(resource)) {
                ClearcasePlugin.debug(DEBUG_ID, "disabled for " + resource
                        + " because already has remote");
                return false;
            }
        }
        return true;
    }

}