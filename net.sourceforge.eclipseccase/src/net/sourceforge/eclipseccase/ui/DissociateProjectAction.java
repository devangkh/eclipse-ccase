package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;

public class DissociateProjectAction extends TeamAction
{

	/** (non-Javadoc)
	 * Method declared on IDropActionDelegate
	 */
	public void run(IAction action)
	{

		IProject[] projects = getSelectedProjects();
		for (int i = 0; i < projects.length; i++)
		{
			try
			{
				Team.removeNatureFromProject(projects[i], ClearcaseProvider.ID, null);
				//SimpleProvider p = (SimpleProvider) RepositoryProvider.getProvider(project);
				//p.configureProvider(config);
				MessageDialog.openInformation(
					shell,
					"Clearcase Plugin",
					"Dissociated project '" + projects[i].getName() + "' from clearcase");
			}
			catch (TeamException e)
			{
				ErrorDialog.openError(
					shell,
					"Clearcase Error",
					"Could not dissociate project '" + projects[i].getName() + "' from clearcase",
					e.getStatus());
			}
		}
	}

	protected boolean isEnabled() throws TeamException
	{
		IProject[] projects = getSelectedProjects();
		if (projects.length == 0)
			return false;
		for (int i = 0; i < projects.length; i++)
		{
			IResource resource = projects[i];
			ClearcaseProvider provider =
				ClearcaseProvider.getProvider(resource);
			if (provider == null)
				return false;
		}
		return true;
	}

}