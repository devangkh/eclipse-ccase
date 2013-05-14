package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.wizard.WizardPage;

public class UseExistingLabelPage extends WizardPage{

	protected UseExistingLabelPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	public void createControl(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);

		  // add all the controls to your wizard page here with 'content' as parent

		  FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SAVE);
		  fileDialog.setFilterExtensions(new String[] { "*.bm" });

		  setControl(content);
		
	}

	public void onEnterPage() {
		// TODO Auto-generated method stub
		
	}

}
