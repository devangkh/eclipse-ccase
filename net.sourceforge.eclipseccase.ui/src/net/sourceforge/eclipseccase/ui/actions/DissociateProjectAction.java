package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearCaseDecorator;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class DissociateProjectAction extends ClearCaseWorkspaceAction {

	/**
	 * (non-Javadoc) Method declared on IDropActionDelegate
	 */
	@SuppressWarnings("restriction")
	@Override
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.setTaskName("Dissociating projects from ClearCase");
				try {
					IProject[] projects = getSelectedProjects();
					monitor.beginTask("Dissociating from ClearCase", 10 * projects.length);

					StateCacheFactory.getInstance().operationBegin();
					StateCacheFactory.getInstance().cancelPendingRefreshes();

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

		executeInForeground(runnable, PROGRESS_DIALOG, "Dissociating from ClearCase");
	}

	@Override
	public boolean isEnabled() {
		IProject[] projects = getSelectedProjects();
		if (projects.length == 0)
			return false;
		for (int i = 0; i < projects.length; i++) {
			IResource resource = projects[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null)
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.team.internal.ui.actions.TeamAction#getSelectedProjects()
	 */
	@SuppressWarnings("restriction")
	@Override
	protected IProject[] getSelectedProjects() {
		return super.getSelectedProjects();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seenet.sourceforge.eclipseccase.ui.actions.ClearCaseWorkspaceAction#
	 * getSchedulingRule()
	 */
	@Override
	protected ISchedulingRule getSchedulingRule() {
		// we run on the workspace root
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}