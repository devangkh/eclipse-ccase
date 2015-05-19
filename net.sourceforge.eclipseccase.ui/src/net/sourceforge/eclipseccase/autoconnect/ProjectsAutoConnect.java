package net.sourceforge.eclipseccase.autoconnect;

import net.sourceforge.eclipseccase.ui.actions.AssociateProjectAction;

import net.sourceforge.eclipseccase.ui.actions.AssociateAllProjectsAction;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.core.resources.IProject;

/**
 * This is a static library for core functions of the auto-association feature.
 * @author Z218543
 */
@SuppressWarnings("restriction")
public class ProjectsAutoConnect {

	/**
	 * Checks the auto-associate flag in the Eclipse settings.
	 * @return Returns true if the auto-association feature is enabled.
	 */
	private static boolean isEnabled()
	{
		return ClearCasePreferences.isAutoConnectEnabled();
	}

	/**
	 * This method associates needed listener for OPEN/CREATE project.
	 * !(IMPORT ~ CREATE).
	 */
	public static void associateListeners()
	{
		ProjectOpenCreateListener L = new ProjectOpenCreateListener();
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		if (ws instanceof Workspace)
		{
			Workspace workspace = (Workspace) ws;
			workspace.addLifecycleListener(L);
		}
	}

	/**
	 * This function starts the auto-associating feature for all projects.
	 */
	public static void connectAllProjects()
	{
		if (isEnabled())
		{
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {

					AssociateAllProjectsAction action = new AssociateAllProjectsAction();
					action.forceToExecute();
				}
			});
		}
	}

	/**
	 * This function starts the auto-associating feature for one project.
	 * @param project
	 */
	public static void connectSingleProject(final IProject project)
	{
		if (isEnabled())
		{
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {

					AssociateProjectAction action = new AssociateProjectAction(project);
					action.forceToExecute();

				}
			});
		}
	}
}
