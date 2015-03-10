/*******************************************************************************
 * Copyright (c) 2013 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.wizards.label;

import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.runtime.jobs.Job;

import java.util.List;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;

import org.eclipse.core.resources.IProject;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.ClearCaseUI;
import net.sourceforge.eclipseccase.ui.wizards.ResizableWizard;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author mike
 *
 */
public class LabelWizard extends ResizableWizard implements INewWizard {

	private static final String EMPTY_STRING = "";

	protected LabelData data = null;

	CreateLabelPage createLabelPage;

	UseExistingLabelPage useExistingLabelPage;

	LabelMainPage mainPage;

	// TODO Unused!
	@SuppressWarnings("unused")
	private IResource[] resources;

	@SuppressWarnings("unused")
	private IStructuredSelection selection;

	@SuppressWarnings("unused")
	private ClearCaseProvider provider;

	private static boolean inTest = false;

	public static final String WIZARD_DIALOG_SETTINGS = "LabelWizard"; //$NON-NLS-1$

	public LabelWizard(IResource[] resources, ClearCaseProvider provider) {
		super(WIZARD_DIALOG_SETTINGS, ClearCaseUI.getInstance().getDialogSettings());
		setNeedsProgressMonitor(true);
		this.resources = resources;
		this.provider = provider;
		data = new LabelData(resources, provider);
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		mainPage = new LabelMainPage("Select");
		addPage(mainPage);
		createLabelPage = new CreateLabelPage("Create Label");
		addPage(createLabelPage);
		useExistingLabelPage = new UseExistingLabelPage("Existing Label");
		addPage(useExistingLabelPage);
	}

	/**
	 * Set critera for pages to be able to finish.
	 */
	@Override
	public boolean canFinish() {
		if (getContainer().getCurrentPage() == mainPage)
			return false;
		else if (getContainer().getCurrentPage() == useExistingLabelPage && useExistingLabelPage.isPageComplete())
			return true;
		else if (getContainer().getCurrentPage() == createLabelPage && createLabelPage.isPageComplete())
			return true;

		return false;

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		/*
		 * Build a process that will run using the IRunnableWithProgress
		 * interface so the UI can handle showing progress bars, etc.
		 */
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					/*
					 * The method (see below) which contains the "real"
					 * implementation code.
					 */
					doFinish(data, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			/* This runs the process built above in a seperate thread */
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;

	}

	/**
	 * The worker method. It will make the actual labeling of the resources.
	 */

	protected void doFinish(LabelData data, IProgressMonitor monitor) throws CoreException {
		IResource[] resources = data.getResource();
		final ClearCaseProvider provider = data.getProvider();
		final List<String> toLabel = getElementsToLabel(resources);

		Job job = new Job("Label") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String labelName = getData().getLabelName();
				String comment = getData().getComment() == null ? EMPTY_STRING : getData().getComment();

				monitor.beginTask("Start labling with label:  " + labelName + " ....", toLabel.size());
				try {
					for (String element : toLabel) {

						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						monitor.subTask("Labeling element: " + element);
						if (inTest) {
							try {
								System.out.println("Faked labling of element: "+element);
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							// Do the real thing...
							List<String> e = new ArrayList<String>();
							e.add(element);
							provider.attachLabel(e, labelName, comment, false);
						}
						monitor.worked(1);
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;

			}
		};
		job.setUser(true);
		job.schedule();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;

	}

	public LabelData getData() {
		return data;
	}

	public void setData(LabelData data) {
		this.data = data;
	}

	public CreateLabelPage getCreateLabelPage() {
		return createLabelPage;
	}

	public void setCreateLabelPage(CreateLabelPage createLabelPage) {
		this.createLabelPage = createLabelPage;
	}

	public UseExistingLabelPage getUseExistingLabelPage() {
		return useExistingLabelPage;
	}

	public void setUseExistingLabelPage(UseExistingLabelPage useExistingLabelPage) {
		this.useExistingLabelPage = useExistingLabelPage;
	}

	/**
	 * Retrieve elements ( with full path) recursive ( if need) and label these
	 * too.
	 *
	 * @param selected
	 *            IResource []
	 * @return elements List<String>
	 */
	public List<String> getElementsToLabel(IResource[] selected) {
		List<String> toLabel = new ArrayList<String>();

		for (IResource iResource : selected) {
			// add selected resource
			toLabel.add(iResource.getLocation().toOSString());
			try {

				if (iResource instanceof IProject) {
					// recursively get all resources for Project
					IProject project = (IProject) iResource;
					IResource[] children = project.members();
					for (IResource child : children) {
						toLabel.add(child.getLocation().toOSString());
					}

				} else if (iResource instanceof IContainer) {
					// recursively get all resources for container
					IContainer container = (IContainer) iResource;
					IResource[] children = container.members();
					for (IResource child : children) {
						toLabel.add(child.getLocation().toOSString());
					}
				}

			} catch (CoreException e) {
				System.out.println("Resource: " + iResource.getName() + " does not exist ! " + e.getMessage());
			}
		}
		return toLabel;
	}

	public static boolean isInTest() {
		return inTest;
	}

	public static void setInTest(boolean inTest) {
		LabelWizard.inTest = inTest;
	}
}
