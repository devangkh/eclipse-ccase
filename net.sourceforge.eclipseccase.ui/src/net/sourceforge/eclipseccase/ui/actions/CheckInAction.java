package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.ui.CommentDialog;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CheckInAction extends ClearcaseAction
{
    /*
     * Method declared on IActionDelegate.
     */
    public void run(IAction action)
    {
        String maybeComment = "";
        int maybeDepth = IResource.DEPTH_ZERO;

        if (ClearcasePlugin.isCheckinComment())
        {
            CommentDialog dlg = new CommentDialog(shell, "Checkin comment");
            if (dlg.open() == CommentDialog.CANCEL)
                return;
            maybeComment = dlg.getComment();
            maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
        }

        final String comment = maybeComment;
        final int depth = maybeDepth;
        run(new WorkspaceModifyOperation()
        {
            public void execute(IProgressMonitor monitor)
                throws InterruptedException, InvocationTargetException
            {
                try
                {
                    IResource[] resources = getSelectedResources();
                    monitor.beginTask("Checking in...", resources.length * 1000);

                    // Sort resources with directories last so that the modification of a
                    // directory doesn't abort the modification of files within it.
                    List resList = Arrays.asList(resources);
                    Collections.sort(resList, new DirectoryLastComparator());

                    for (int i = 0; i < resources.length; i++)
                    {
                        IResource resource = resources[i];
                        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
                        ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
                        provider.setComment(comment);
                        provider.checkin(new IResource[] { resource }, depth, subMonitor);
                    }
                }
                catch (TeamException e)
                {
                    throw new InvocationTargetException(e);
                }
                finally
                {
                    monitor.done();
                }
            }
        }, "Checkin", TeamAction.PROGRESS_DIALOG);

        updateActionEnablement();
    }

    protected boolean isEnabled()
    {
        IResource[] resources = getSelectedResources();
        if (resources.length == 0)
            return false;
        for (int i = 0; i < resources.length; i++)
        {
            IResource resource = resources[i];
            ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
            if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.hasRemote(resource))
                return false;
            if (!provider.isCheckedOut(resource))
                return false;
        }
        return true;
    }

}