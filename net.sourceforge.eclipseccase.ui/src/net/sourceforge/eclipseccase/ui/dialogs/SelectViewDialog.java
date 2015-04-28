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
package net.sourceforge.eclipseccase.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.provider.ActivityListLabelProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class SelectViewDialog extends Dialog {

	private ComboViewer comboViewer;

	private String[] views = new String[0];

	private String selectedView = null;

	private Text manualViewText = null;

	private String manualView = null;

	public SelectViewDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select View");
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		final ClearCaseProvider provider = new ClearCaseProvider();
		//Show that operation is ongoing...
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					views = provider.getViewNames();
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (views.length == 0) {
			System.out.println("Something went wrong during listing views.");
			MessageDialog.openError(getShell(), "Error", "Something went wrong during listing views.");
		}
		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("User views:");

		comboViewer = createComboViewer(composite, views);
		// sets first element ( view) i array.
		if (views.length > 0) {
			comboViewer.setSelection(new StructuredSelection(views[0]), true);
		}
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedView = (String) selection.getFirstElement();
			}
		});
		comboViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedView = (String) selection.getFirstElement();
				manualViewText.setText("");
			}
		});

		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Enter view name manually:");

		manualViewText = new Text(composite, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 400;
		manualViewText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				manualView = manualViewText.getText();
			}
		});

		manualViewText.setLayoutData(data);

		return composite;
	}

	protected ComboViewer createComboViewer(Composite composite, String[] activities) {
		ComboViewer comboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboViewer.setLabelProvider(new ActivityListLabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setInput(views);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		comboViewer.getCombo().setLayoutData(data);

		return comboViewer;
	}

	public String getSelectedView() {
		return selectedView;
	}

	public void setSelectedView(String selectedView) {
		this.selectedView = selectedView;
	}

	public String getManualView() {
		return manualView;
	}

}
