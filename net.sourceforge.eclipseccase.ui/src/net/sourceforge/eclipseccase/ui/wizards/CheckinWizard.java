package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.core.runtime.SubProgressMonitor;

import java.util.Arrays;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.ClearDlgHelper;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import net.sourceforge.eclipseccase.ui.ClearCaseUI;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class CheckinWizard extends ResizableWizard implements INewWizard {

	private CheckinWizardPage page;

	private IResource[] resources;
	
	private IResource[] identical;
	
	private IStructuredSelection selection;

	private ClearCaseProvider provider;

	public static final String CHECKIN_WIZARD_DIALOG_SETTINGS = "MergeWizard"; //$NON-NLS-1$

	public static final int SCALE = 100;

	/**
	 * Constructor for CheckinWizard.
	 */
	public CheckinWizard(IResource[] resources, IResource [] identical, ClearCaseProvider provider) {
		super(CHECKIN_WIZARD_DIALOG_SETTINGS, ClearCaseUI.getInstance().getDialogSettings());
		setNeedsProgressMonitor(true);
		this.resources = resources;
		this.identical = identical;
		this.provider = provider;
	}
	

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new CheckinWizardPage("Select Source", resources, provider);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String comment = page.getComment();
		final IResource[] selectedResources = page.getResourceList();
		final boolean recursive = page.isRecursive();
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
					doFinish(provider, selectedResources,comment, recursive, monitor);
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
	 * The worker method. It will gather resources that needs merge.
	 */

	private void doFinish(ClearCaseProvider provider, IResource[] resources, String comment,boolean isRecursive, IProgressMonitor monitor) throws CoreException {
		int depth = isRecursive ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		monitor.beginTask("Checking in...", resources.length);
		ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
		Arrays.sort(resources, new DirectoryLastComparator());
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (provider != null) {
				provider.setComment(comment);
				provider.setOperationListener(opListener);
				provider.checkin(new IResource[] { resource }, depth, new SubProgressMonitor(monitor, 1 * SCALE));
			}
		}

	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}