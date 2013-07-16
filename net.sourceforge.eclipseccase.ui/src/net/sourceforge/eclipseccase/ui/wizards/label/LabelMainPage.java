package net.sourceforge.eclipseccase.ui.wizards.label;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class LabelMainPage extends WizardPage implements Listener {

	boolean choice = false;

	private Button newLabelButton;

	private Button useExistingLabel;

	protected LabelMainPage(String pageName) {
		super(pageName);
		setTitle("Label elements");
		setDescription("Use existing labels or create new options");
		// We always have a value set so it is ok to go to next step.
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createRadioButtons(composite);
		setControl(composite);

	}

	protected void createRadioButtons(Composite composite) {
		// Choice of new label
		newLabelButton = new Button(composite, SWT.RADIO);
		newLabelButton.setText("Create new label");

		// Choice of exsting label
		useExistingLabel = new Button(composite, SWT.RADIO);
		useExistingLabel.setText("Use existing label");
		useExistingLabel.setSelection(true);
	}

	// Change the order for addPages() definition i Wizard class. Make sure we
	// get useExistingLabelPage
	// since it would use createLabelPage as natural order from LabelWizard.
	@Override
	public IWizardPage getNextPage() {
		if (useExistingLabel.getSelection()) {
			return ((LabelWizard) getWizard()).getUseExistingLabelPage();

		}
		if (newLabelButton.getSelection()) {
			return ((LabelWizard) getWizard()).getCreateLabelPage();
		}
		return null;
	}

	// Not used since we always have a selection.
	public void handleEvent(Event event) {

	}

}
