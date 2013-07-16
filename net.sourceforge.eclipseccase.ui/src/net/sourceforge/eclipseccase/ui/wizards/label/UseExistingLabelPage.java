package net.sourceforge.eclipseccase.ui.wizards.label;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import java.util.List;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class UseExistingLabelPage extends WizardPage implements ISelectionChangedListener,IDoubleClickListener{

	private ListViewer listViewer;

	private List<String> existingLabels;

	private LabelData data = null;

	private ClearCaseProvider provider;

	private IResource[] resources;

	private String myLabel;

	protected UseExistingLabelPage(String pageName) {
		super(pageName);
		setTitle("Existing label");
		setDescription("Select an existing clearcase label.");
	}

	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		loadData();
		provider = data.getProvider();
		resources = data.getResource();
		existingLabels = provider.getLabels(resources);
		createListViewer(composite);
		setControl(composite);
		setPageComplete(false);
	}

	private void createListViewer(Composite parent) {
		listViewer = new ListViewer(parent);
		listViewer.getControl().setLayoutData(new GridData(GridData.FILL_VERTICAL));
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new CcLabelLabelProvider());
		// sort on name
		listViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object element1, Object element2) {
				return ((String) element1).compareToIgnoreCase(((String) element2));
			}
		});
		listViewer.addSelectionChangedListener(this);
		listViewer.addDoubleClickListener(this);
		listViewer.setInput(existingLabels);

	}

	private class CcLabelLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			// Image file =
			// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			return null;
		}

		@Override
		public String getText(Object element) {
			String label = (String) element;
			return label;
		}
	}

	/**
	 * Load data from model.
	 */
	private void loadData() {
		LabelWizard wizard = (LabelWizard) getWizard();
		data = wizard.getData();

	}

	/**
	 * Saves data to a model
	 */
	private void saveData() {
		LabelWizard wizard = (LabelWizard) getWizard();
		LabelData data = wizard.getData();
		data.setLabelName(myLabel);
		
	}
	
	public void doubleClick(DoubleClickEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		myLabel = (String) selection.getFirstElement();
		saveData();
		setPageComplete(true);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		myLabel = (String) selection.getFirstElement();
		saveData();
		setPageComplete(true);
	}

}
