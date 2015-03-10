package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.ClearCasePreferences;

import org.eclipse.core.resources.IResource;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ui.ClearCaseDecorator;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

public class AssociateProjectActionNotUsed extends ClearCaseWorkspaceAction {

	/**
	 * (non-Javadoc) Method declared on IDropActionDelegate
	 */
	@Override
	public void execute(IAction action) {

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IProject[] projects = getSelectedProjects();
					// each project gets 200 ticks
					monitor.setTaskName("Associating projects with ClearCase");
					monitor.beginTask("", 200 * projects.length);

					StateCacheFactory.getInstance().operationBegin();

					for (int i = 0; i < projects.length; i++) {
						IProject project = projects[i];
						// the map() call automatically refreshes all labels (at
						// least in Eclipse 3.5)
						RepositoryProvider.map(project, ClearCaseProvider.ID);
						StateCacheFactory.getInstance().remove(project);
						// StateCacheFactory.getInstance().fireStateChanged(project);

						// first, get list of resources
						final List<IResource> resources = new ArrayList<IResource>();
						try {
							project.accept(new IResourceVisitor() {
								public boolean visit(IResource resource) {
									resources.add(resource);
									return true;
								}
							});
						} catch (CoreException e) {
						}
						// now we know how much to do, create a
						// SubProgressMonitor
						SubProgressMonitor submonitor = new SubProgressMonitor(monitor, 200);
						monitor.subTask("Scanning project " + project.getName());
						// 10 for activeDecorator.refresh()
						submonitor.beginTask(project.getName(), resources.size() + 10);
						for (IResource res : resources) {
							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(res);
							if (p != null) {
								if (ClearCasePreferences.isFullRefreshOnAssociate())
									p.ensureInitialized(res);
							}
							submonitor.worked(1);
							if (submonitor.isCanceled())
								break;
						}

						// To get correct state for project.
						// refresh the decorator
						IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
						if (manager.getEnabled(ClearCaseDecorator.ID) && !submonitor.isCanceled()) {
							ClearCaseDecorator activeDecorator = (ClearCaseDecorator) manager.getBaseLabelProvider(ClearCaseDecorator.ID);
							if (activeDecorator != null) {
								if (ClearCasePreferences.isFullRefreshOnAssociate())
									activeDecorator.refresh(project);
								else {
									activeDecorator.refresh(new IResource[] {project});  // refresh first level
									activeDecorator.refresh(activeDecorator.getShownResources(project));
								}
							}
						}
						submonitor.done();
					}
				} finally {
					StateCacheFactory.getInstance().operationEnd();
					updateActionEnablement();
					monitor.done();
				}
			}
		};

		executeInForeground(runnable, PROGRESS_DIALOG, "Associating with ClearCase");
	}

	@Override
	public boolean isEnabled() {
		IProject[] projects = getSelectedProjects();
		if (projects.length == 0)
			return false;
		for (int i = 0; i < projects.length; i++) {
			IResource resource = projects[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider != null)
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