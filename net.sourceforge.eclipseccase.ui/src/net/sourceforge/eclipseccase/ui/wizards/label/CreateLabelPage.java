package net.sourceforge.eclipseccase.ui.wizards.label;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class CreateLabelPage extends WizardPage implements Listener {

	Text labelText;

	Text labelCommentText;

	Button createLabelButton;
	
	private static boolean inTest = false;

	private static final String EMPTY_STRING = "";

	// emptyLabel holds an error if there is not label name entered.
	private IStatus emptyLabelStatus;

	private String myLabel = EMPTY_STRING;

	private String myComment = EMPTY_STRING;

	private LabelData data;


	protected CreateLabelPage(String pageName) {
		super(pageName);
		setTitle("Create Lable");
		setDescription("Create a new label  in clearcase");

	}

	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		GridLayout layout = new GridLayout();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTextInput(composite);
		setControl(composite);
		emptyLabelStatus = new Status(IStatus.OK, "not_used", 0, "", null);
		loadData();
		setPageComplete(false);

	}

	protected void createTextInput(Composite composite) {
		GridData gd;

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

		new Label(composite, SWT.NONE).setText("Comment:");
		labelCommentText = new Text(composite, SWT.BORDER);
		labelCommentText.setBounds(10, 10, 200, 200);

		gd = new GridData(GridData.FILL_HORIZONTAL);

		gd.horizontalSpan = ncol - 1;

		labelCommentText.setLayoutData(gd);

		// create Label Button

		createLabelButton = new Button(composite, SWT.PUSH);

		createLabelButton.setText("Create label");

		createLabelButton.addListener(SWT.Selection, this);

		createLabelButton.setEnabled(false);

		gd = new GridData();

		gd.horizontalAlignment = GridData.END;

		createLabelButton.setLayoutData(gd);

	}

	public void handleEvent(Event event) {
		new Status(IStatus.OK, "not_used", 0, "", null);

		// will be called when text is written then we can enable button.
		if (event.widget == labelText) {
			createLabelButton.setEnabled(true);
		}

		if (event.widget == createLabelButton) {
			if (labelText.getText().equals(EMPTY_STRING) || labelText.getText().matches("\\s*")) {
				emptyLabelStatus = new Status(IStatus.ERROR, "not_used", 0, "Label cannot be empty", null);
				setStatus(emptyLabelStatus);

			}

			myLabel = labelText.getText();
			myComment = labelCommentText.getText();

			if (createLabel(myLabel, myComment).getCode() == IStatus.OK) {
				createLabelButton.setEnabled(false);
				// Lock input field.
				labelText.setEditable(false);
				saveData();
				setPageComplete(true);
				setStatus(new Status(IStatus.OK, "not_used", 0, "Created label " + myLabel, null));
			}
					
		}

	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void setStatus(IStatus status) {
		String message = status.getMessage();
		if (message.length() == 0) {
			message = null;
		}
		switch (status.getSeverity()) {
		case IStatus.OK:
			setErrorMessage(null);
			setMessage(message);
			break;
		case IStatus.WARNING:
			setErrorMessage(null);
			setMessage(message, IMessageProvider.WARNING);
			break;
		case IStatus.INFO:
			setErrorMessage(null);
			setMessage(message, IMessageProvider.INFORMATION);
			break;
		default:
			setErrorMessage(message);
			setMessage(null);
			break;
		}
	}

	private IStatus createLabel(String name, String comment) {
		System.out.println("createLabel() not for real.");
		Status status = new Status(IStatus.OK, "not_used", 0, "", null);
		//TODO:Remove simulation ...
		System.out.println("createLabel() not for real.");
		// IStatus s = data.getProvider().createLabel(name,comment);
		return status;
	}

	/**
	 * Saves data to a model
	 */
	private void saveData() {
		LabelWizard wizard = (LabelWizard) getWizard();
		LabelData data = wizard.getData();
		data.setLabelName(myLabel);
		data.setComment(myComment);

	}

	private void loadData() {
		LabelWizard wizard = (LabelWizard) getWizard();
		data = wizard.getData();
	}

	/*
	 * Disable next button.
	 */
	@Override
	public boolean canFlipToNextPage() {
		return false;
	}
	
	public static boolean isInTest() {
		return inTest;
	}

	public static void setInTest(boolean inTest) {
		CreateLabelPage.inTest = inTest;
	}

}