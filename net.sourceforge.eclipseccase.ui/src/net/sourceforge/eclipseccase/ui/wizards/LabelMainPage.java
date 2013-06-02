package net.sourceforge.eclipseccase.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import org.eclipse.swt.widgets.Listener;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.wizard.WizardPage;

public class LabelMainPage extends WizardPage implements Listener{
	
	
	private IResource[] resources;

	private ClearCaseProvider provider;
	
	boolean choice = false;
	
	private Button newLabelButton;
	
	private Button useExistingLabel;
	
	
	protected LabelMainPage(String pageName, IResource[] resources, ClearCaseProvider provider) {
		super(pageName);
		setTitle("Label elements");
		setDescription("Use existing labels or create new options");
		this.resources = resources;
		this.provider = provider;
		
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
	// create the composite to hold the widgets
	GridData gd;
	Composite composite = new Composite(parent, SWT.NULL);

	// create the desired layout for this wizard page
	GridLayout gl = new GridLayout();
	int ncol = 4;
	gl.numColumns = ncol;
	composite.setLayout(gl);

	// Choice of transport
	newLabelButton = new Button(composite, SWT.RADIO);
	newLabelButton.setText("Create new label");
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.horizontalSpan = ncol;
	newLabelButton.setLayoutData(gd);
	newLabelButton.setSelection(true);

	useExistingLabel = new Button(composite, SWT.RADIO);
	useExistingLabel.setText("Select existing label");
	gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.horizontalSpan = ncol;
	useExistingLabel.setLayoutData(gd);

	// set the composite as the control for this page
	setControl(composite);
	addListeners();
	
	}
	
	private void addListeners() {
		newLabelButton.addListener(SWT.Selection, this);
		useExistingLabel.addListener(SWT.Selection, this);
	}
	
	
	/*
	 * Returns the next page. Saves the values from this page in the model
	 * associated with the wizard. Initializes the widgets on the next page.
	 */

	public IWizardPage getNextPage() {
		saveToLabelData();
		
		if (useExistingLabel.getSelection()) {
			UseExistingLabelPage page = ((LabelWizard) getWizard()).useExistingLabelPage;
			page.onEnterPage();
			return page;
		}
		// Returns the next page depending on the selected button
		if (newLabelButton.getSelection()) {
			CreateLabelPage page = ((LabelWizard) getWizard()).createLabelPage;
			return page;
		}
		return null;
	}
	
	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage()
	{
		if (getErrorMessage() != null) return false;
		//One must be selected to go to next page
		if (useExistingLabel.getSelection() || newLabelButton.getSelection())
			return true;
		return false;
	}

	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void saveToLabelData(){
		LabelWizard wizard = (LabelWizard)getWizard();
		LabelData data = wizard.labelData;
		data.setResource(resources);
		data.setProvider(provider);
	}
	
	
}
