
package net.sourceforge.eclipseccase.ui.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;

public class CheckOutAction extends ClearcaseWorkspaceAction
{
    public void run(IAction action)
    {
        String maybeComment = "";
        int maybeDepth = IResource.DEPTH_ZERO;

        if (ClearcasePlugin.isCommentCheckout())
        {
            CommentDialog dlg = new CommentDialog(getShell(),
                    "Checkout comment");
            if (dlg.open() == CommentDialog.CANCEL) return;
            maybeComment = dlg.getComment();
            maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE
                    : IResource.DEPTH_ZERO;
        }

        final String comment = maybeComment;
        final int depth = maybeDepth;

        IWorkspaceRunnable runnable = new IWorkspaceRunnable()
        {
            public void run(IProgressMonitor monitor) throws CoreException
            {

                try
                {
                    IResource[] resources = getSelectedResources();
                    beginTask(monitor, "Checking out...", resources.length);

                    // Sort resources with directories last so that the
                    // modification of a
                    // directory doesn't abort the modification of files within
                    // it.
                    List resList = Arrays.asList(resources);
                    Collections.sort(resList, new DirectoryLastComparator());

                    for (int i = 0; i < resources.length; i++)
                    {
                        IResource resource = resources[i];
                        ClearcaseProvider provider = ClearcaseProvider
                                .getClearcaseProvider(resource);
                        provider.setComment(comment);
                        provider.checkout(new IResource[]{resource}, depth,
                                subMonitor(monitor));
                    }
                }
                finally
                {
                    monitor.done();
                }
            }
        };
        executeInBackground(runnable, "Checking out resources from ClearCase");
    }

    protected boolean isEnabled() throws TeamException
    {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0) return false;
        for (int i = 0; i < resources.length; i++)
        {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider
                    .getClearcaseProvider(resource);
            if (provider == null || provider.isUnknownState(resource)
                    || provider.isIgnored(resource)
                    || !provider.hasRemote(resource)) return false;
            if (provider.isCheckedOut(resource)) return false;
        }
        return true;
    }

}