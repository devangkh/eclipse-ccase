package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.ClearCaseUI;
import net.sourceforge.eclipseccase.ui.wizards.label.CreateLabelPage;
import net.sourceforge.eclipseccase.ui.wizards.label.LabelWizard;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.annotation.Mock;

/**
 * This class is for starting LabelWizard GUI and manually testing the
 * LableWizard without performing any actual clearcase implementations.
 * 
 * @author eraonel
 * 
 */
public class ManualLabelWizardTest extends ApplicationWindow {

	IResource resourceMock = EasyMock.createMock(IResource.class);
	ClearCaseProvider providerMock = EasyMock
			.createMock(ClearCaseProvider.class);

	public ManualLabelWizardTest(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createContents(Composite parent) {
		LabelWizard.setInTest(true);
		CreateLabelPage.setInTest(true);
		IResource[] resources = new IResource[] { resourceMock };
		LabelWizard wizard = new LabelWizard(resources, providerMock);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		int returnCode = dialog.open();
		if(returnCode == Dialog.OK)
	          System.out.println(wizard.getData().toString());
	        else
	          System.out.println("Cancelled");
		return null;

	}

	public static void main(String[] args) {
		ManualLabelWizardTest wizard = new ManualLabelWizardTest(null);
		wizard.setBlockOnOpen(true);
		wizard.open();
	}

}
