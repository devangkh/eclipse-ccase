package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.StateCacheFactory;
import org.eclipse.core.resources.IProject;
import net.sourceforge.eclipseccase.ui.ClearCaseDecorator;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IAction;

/**
 * Action class for associating all the projects from Project Explorer with ClearCase plug-in.
 * @author Z218543
 */
public class DissociateAllProjectsAction extends ClearCaseWorkspaceAction {

	@SuppressWarnings("restriction")
	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {

		// We will create runnable for project association.
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			// Run method.
			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					// We have to list all the projects in the workspace.
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

					monitor.setTaskName("Dissociating projects from ClearCase");
					monitor.beginTask("Dissociating from ClearCase", 10 * projects.length);

					StateCacheFactory.getInstance().operationBegin();
					StateCacheFactory.getInstance().cancelPendingRefreshes();

					// We will iterate over all the projects.
					for (int i = 0; i < projects.length; i++) {
						IProject project = projects[i];

						if (!project.isOpen()) {
							continue;
						}

						ClearCaseProvider testProvider = ClearCaseProvider.getClearCaseProvider(project);
						if (testProvider == null) {
							continue;
						}

						monitor.subTask(project.getName());
						RepositoryProvider.unmap(project);
						StateCacheFactory.getInstance().remove(project);
						StateCacheFactory.getInstance().fireStateChanged(project);

						monitor.worked(5);

						// Refresh the decorator.
						IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
						if (manager.getEnabled(ClearCaseDecorator.ID)) {
							ClearCaseDecorator activeDecorator = (ClearCaseDecorator) manager.getBaseLabelProvider(ClearCaseDecorator.ID);

							if (activeDecorator != null) {
								if (ClearCasePreferences.isFullRefreshOnAssociate())
									activeDecorator.refresh(project);
								else
									activeDecorator.refresh(activeDecorator.getShownResources(project));
							}
						}
						monitor.worked(5);
					}
				} finally {
					StateCacheFactory.getInstance().operationEnd();
					updateActionEnablement();
					monitor.done();
				}
			}
		};

		executeInForeground(runnable, PROGRESS_DIALOG, "Dissociating projects with ClearCase");
	}

	@Override
	public boolean isEnabled() {

		// Some project have to be associated.
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (int i = 0; i < projects.length; i++) {
			IResource resource = projects[i];

			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider != null)
				return true;
		}

		return false;
	}

	@Override
	protected ISchedulingRule getSchedulingRule() {
		// we run on the workspace root
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}
