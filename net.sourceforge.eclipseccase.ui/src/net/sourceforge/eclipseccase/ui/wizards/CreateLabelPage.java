package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.*;

public class CreateLabelPage extends WizardPage implements Listener {

	Text labelText;

	Text labelCommentText;

	Button createLabelButton;

	Button labelCodeButton;

	private String EMPTY_STRING = "";

	// emptyLabel holds an error if there is not label name entered.
	private IStatus emptyLabel;

	private ClearCaseProvider provider;

	private IResource[] resources;

	protected CreateLabelPage(String pageName) {
		super(pageName);
		LabelWizard wizard = (LabelWizard) getWizard();
		LabelData data = wizard.labelData;
		provider = data.getProvider();
		resources = data.getResource();
	}

	public void createControl(Composite parent) {

		// create the composite to hold the widgets

		GridData gd;

		Composite composite = new Composite(parent, SWT.NULL);

		// create the desired layout for this wizard page

		GridLayout gl = new GridLayout();

		int ncol = 4;

		gl.numColumns = ncol;

		composite.setLayout(gl);

		// Label

		new Label(composite, SWT.NONE).setText("Label:");

		labelText = new Text(composite, SWT.BORDER);

		gd = new GridData(GridData.FILL_HORIZONTAL);

		gd.horizontalSpan = ncol - 1;

		labelText.setLayoutData(gd);

		labelText.addListener(SWT.Modify, this);

		// create label comment

		new Label(composite,SWT.NONE).setText("Comment:");
		labelCommentText = new Text(composite, SWT.BORDER);

		gd = new GridData(GridData.FILL_HORIZONTAL);

		gd.horizontalSpan = ncol - 1;

		labelCommentText.setLayoutData(gd);

		// create Lable Button

		createLabelButton = new Button(composite, SWT.PUSH);

		createLabelButton.setText("Create label");

		createLabelButton.addListener(SWT.Selection, this);

		createLabelButton.setEnabled(false);

		labelCodeButton = new Button(composite, SWT.PUSH);

		labelCodeButton.setText("Label");

		labelCodeButton.addListener(SWT.Selection, this);

		labelCodeButton.setEnabled(false);

		gd = new GridData();

		gd.horizontalAlignment = GridData.END;

		createLabelButton.setLayoutData(gd);

		setControl(composite);

	}

	public void handleEvent(Event event) {
		// Initialize a variable with the no error status
		Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		// TODO:Add more label handling as well as support for some format check
		// of label using regexp.

		// will be called when text is written then we can enable button.
		if (event.widget == labelText) {
			createLabelButton.setEnabled(true);
		}

		if (event.widget == createLabelButton) {
			if (labelText.getText().equals(EMPTY_STRING) || labelText.getText().matches("\\s*")) {
				status = new Status(IStatus.ERROR, "not_used", 0, "Label cannot be empty", null);
				emptyLabel = status;
				setStatus(emptyLabel);

			}

			String myLabel = labelText.getText();
			String myComment = labelCommentText.getText();
			// TODO:Issue create label command.
			if (createLabel(myLabel,myComment).getCode() == IStatus.OK) {
				labelCodeButton.setEnabled(true);
				createLabelButton.setEnabled(false);
				// Lock input field.
				labelText.setEditable(false);

			}

		}

		if (event.widget == labelCodeButton) {
			status = new Status(IStatus.INFO, "not_used", 0, "Start labeling selected resorces ....", null);

			setStatus(status);
//			if (labelResources(resources) == IStatus.OK) {
//
//			}

		}

		setPageComplete(isPageComplete());

		getWizard().getContainer().updateButtons();

	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void setStatus(IStatus status) {
		String message = status.getMessage();
		if (message.length() == 0)
			message = null;
		switch (status.getSeverity()) {
		case IStatus.OK:
			setErrorMessage(null);
			setMessage(message);
			break;
		case IStatus.WARNING:
			setErrorMessage(null);
			setMessage(message, WizardPage.WARNING);
			break;
		case IStatus.INFO:
			setErrorMessage(null);
			setMessage(message, WizardPage.INFORMATION);
			break;
		default:
			setErrorMessage(message);
			setMessage(null);
			break;
		}
	}

	private IStatus createLabel(String name,String comment) {
		System.out.println("createLabel()");
		Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		provider.createLabel(name,comment);

		return status;
	}

	private IStatus labelResources(IResource [] resources){
		System.out.println("labelResources()");
		Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		return null;
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