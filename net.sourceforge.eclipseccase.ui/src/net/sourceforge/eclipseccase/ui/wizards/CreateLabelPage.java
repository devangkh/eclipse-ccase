package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.*;

public class CreateLabelPage extends WizardPage implements Listener {

	Text labelText;

	Button createLabelButton;

	protected CreateLabelPage(String pageName) {
		super(pageName);
	}

	public void createControl(Composite parent) {

		// create the composite to hold the widgets

		GridData gd;

		Composite composite = new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page

		GridLayout gl =  new GridLayout();

		int ncol = 4;

		gl.numColumns = ncol;

		composite.setLayout(gl);

		// Label

		new Label(composite, SWT.NONE).setText("Label:");

		labelText = new Text(composite, SWT.BORDER);

		gd = new GridData(GridData.FILL_HORIZONTAL);

		gd.horizontalSpan = ncol - 1;

		labelText.setLayoutData(gd);

		// create Lable Button

		createLabelButton = new Button(composite, SWT.PUSH);

		createLabelButton.setText("Create label");

		createLabelButton.addListener(SWT.Selection, this);

		gd = new GridData();

		gd.horizontalAlignment = GridData.END;

		createLabelButton.setLayoutData(gd);
		
		setControl(composite);

	}

	public void handleEvent(Event event) {
		//TODO:Add more label handling as well as support for some format check of label using regexp.
		if (event.widget == createLabelButton) {
			if(labelText.getText().equals("")){
				System.out.println("No label has been entered.");
				return;
			}
			MessageDialog.openInformation(

			this.getShell(), "", "Create label clicked ");

		}

		setPageComplete(isPageComplete());

		getWizard().getContainer().updateButtons();

	}

	/*
	 * 
	 * Sets the completed field on the wizard class when all the information is
	 * 
	 * entered and the wizard can be completed
	 */

	@Override
	public boolean isPageComplete() {

		return true;

	}

}