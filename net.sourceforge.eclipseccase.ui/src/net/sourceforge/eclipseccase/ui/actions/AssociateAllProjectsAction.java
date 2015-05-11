package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.eclipseccase.StateCacheFactory;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.ClearCaseDecorator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IAction;

/**
 * Action class for associating all the projects from Project Explorer with ClearCase plug-in.
 * @author Z218543
 */
public class AssociateAllProjectsAction extends ClearCaseWorkspaceAction {

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

					monitor.setTaskName("Associating projects with ClearCase");
					monitor.beginTask("", 200 * projects.length);

					// Operation begin...
					StateCacheFactory.getInstance().operationBegin();

					// We will iterate all the projects.
					for (int i = 0; i < projects.length; i++) {
						IProject project = projects[i];

						if (!project.isOpen()) {
							continue;
						}

						ClearCaseProvider testProvider = ClearCaseProvider.getClearCaseProvider(project);
						if (testProvider != null) {
							continue;
						}

						// The map() call automatically refreshes all labels.
						RepositoryProvider.map(project, ClearCaseProvider.ID);
						StateCacheFactory.getInstance().remove(project);

						// We have to list all the resources from all the projects.
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

						// Now we know the resource count... we can show submonitor...
						SubProgressMonitor submonitor = new SubProgressMonitor(monitor, 200);

						monitor.subTask("Scanning project " + project.getName());

						// 10 for activeDecorator.refresh()
						submonitor.beginTask(project.getName(), resources.size() + 10);

						// We will iterate all the resources...
						for (IResource res : resources) {
							ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(res);
							if (provider != null) {

								// Initialization.
								if (ClearCasePreferences.isFullRefreshOnAssociate()) {
									provider.ensureInitialized(res);
								}
							}

							submonitor.worked(1);
							if (submonitor.isCanceled()) {
								break;
							}
						}

						// Now, we will check project view, if it is empty, we have to dissociate.
						ClearCaseProvider projectProvider = ClearCaseProvider.getClearCaseProvider(project);
						if (projectProvider != null) {

							boolean dissociateProject = false;

							String viewName = ClearCaseProvider.getViewName(project);
							if (viewName == null) {
								dissociateProject = true;
							} else {
								if (viewName.isEmpty()) {
									dissociateProject = true;
								}
							}

							if (dissociateProject) {
								RepositoryProvider.unmap(project);
								StateCacheFactory.getInstance().remove(project);
								StateCacheFactory.getInstance().fireStateChanged(project);
							}
						}

						// Now we will refresh the decorator.
						IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();

						if (manager.getEnabled(ClearCaseDecorator.ID) && !submonitor.isCanceled()) {

							// Active decorator.
							ClearCaseDecorator activeDecorator = (ClearCaseDecorator) manager.getBaseLabelProvider(ClearCaseDecorator.ID);
							if (activeDecorator != null) {

								if (ClearCasePreferences.isFullRefreshOnAssociate()) {
									activeDecorator.refresh(project);
								} else {
									activeDecorator.refresh(new IResource[] {project});
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

		executeInForeground(runnable, PROGRESS_DIALOG, "Associating projects with ClearCase");
	}

	@Override
	public boolean isEnabled() {

		// Some project have to be unassociated.
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (int i = 0; i < projects.length; i++) {
			IResource resource = projects[i];

			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null)
				return true;
		}

		return false;
	}

	@Override
	protected ISchedulingRule getSchedulingRule() {
		// we run on the workspace root
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public void forceToExecute() {
		IAction dummy = null;
		try {
			this.execute(dummy);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}
}
