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
package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.swt.widgets.Event;

import org.eclipse.swt.widgets.Listener;

import org.eclipse.ui.INewWizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import net.sourceforge.clearcase.MergeData;
import net.sourceforge.eclipseccase.ui.actions.MergeViewAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;

import org.eclipse.jface.viewers.IStructuredSelection;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ui.ClearCaseUI;

/**
 * @author eraonel
 * 
 */
public class LabelWizard extends ResizableWizard implements INewWizard {
	
	LabelData labelData;

	CreateLabelPage createLabelPage;

	UseExistingLabelPage useExistingLabelPage;

	LabelMainPage mainPage;

	private IResource[] resources;

	private IStructuredSelection selection;

	private ClearCaseProvider provider;

	public static final String WIZARD_DIALOG_SETTINGS = "LabelWizard"; //$NON-NLS-1$

	public LabelWizard(IResource[] resources, ClearCaseProvider provider) {
		super(WIZARD_DIALOG_SETTINGS, ClearCaseUI.getInstance().getDialogSettings());
		setNeedsProgressMonitor(true);
		this.resources = resources;
		this.provider = provider;
		labelData = new LabelData();
	}

	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		mainPage = new LabelMainPage("Select", resources, provider);
		 addPage(mainPage);
		createLabelPage = new CreateLabelPage("");
		addPage(createLabelPage);
		useExistingLabelPage = new UseExistingLabelPage("");
		addPage(useExistingLabelPage);
	}

	

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		final String label = null;// TODO: We need add selected label.
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
					doFinish(provider, resources, label, monitor);
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

		// // Now we need to create a new
		// MergeViewAction view = new MergeViewAction(data, provider,
		// resources[0].getProject());
		//
		// try {
		// view.execute((IAction) null);
		// } catch (Exception e) {
		//
		// }

		return true;

	}

	/**
	 * The worker method. It will make the actual checkin of the resource.
	 */

	private void doFinish(ClearCaseProvider provider, IResource[] resources, String label, IProgressMonitor monitor) throws CoreException {
		// TODO:Which resource. When I selected more than one resource
		// data = new Vector<MergeData>();
		// monitor.beginTask("Labeling resources ...", resources.length);
		// for (int i = 0; i < resources.length; i++) {
		// IResource resource = resources[i];
		// String pname = resource.getLocation().toOSString();// directory or
		// // file.
		// Vector<MergeData> d = provider.findMerge(pname, branch);
		// data.addAll(d);
		//
		// }
		// monitor.done();
		//
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

	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
}
