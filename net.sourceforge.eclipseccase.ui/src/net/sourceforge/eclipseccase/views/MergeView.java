package net.sourceforge.eclipseccase.views;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import net.sourceforge.clearcase.MergeData;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.ClearCaseImages;
import net.sourceforge.eclipseccase.ui.operation.MergeResourcesOperation;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MergeView extends ViewPart {

	private Menu mergeMenu;

	private MenuItem mergeMenuItem;

	private TableViewer viewer;

	private Vector<MergeData> data;

	private ClearCaseProvider provider;

	private boolean autoMerge;

	private Action mergeAction;

	private IProject project;

	private IStructuredSelection selection;

	private static final Image CHECKED = ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_CHECKED).createImage();

	private static final Image UNCHECKED = ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_UNCHECKED).createImage();

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		createViewer(parent);
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		viewer.setInput(getData());
		// Make the selection available to other views
		getSite().setSelectionProvider(viewer);
		// Set the sorter for the table

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		mergeMenu = new Menu(parent.getShell(), SWT.POP_UP);
		mergeMenuItem = new MenuItem(mergeMenu, SWT.PUSH);
		mergeMenuItem.setText("Merge...");
		mergeMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				merge();
			}
		});
		mergeMenuItem.setEnabled(false);

		viewer.getTable().setMenu(mergeMenu);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent event) {
				selection = (IStructuredSelection) event.getSelection();
			}
		});

		viewer.getTable().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (viewer.getTable().getSelectionCount() > 0) {
					mergeMenuItem.setText("Merge");
					mergeMenuItem.setEnabled(true);
					mergeAction.setEnabled(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// System.out.println("select default");
			}
		});

		mergeAction = new Action() {
			@Override
			public void run() {
				merge();
			}
		};

		mergeAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/arrow_merge.png"));
		mergeAction.setToolTipText("Merge");
		mergeAction.setEnabled(false);

		getViewSite().getActionBars().getToolBarManager().add(mergeAction);
		getViewSite().getActionBars().getToolBarManager().update(true);

	}

	public TableViewer getViewer() {
		return viewer;
	}

	// This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Name", "Automerge", "Version from", "Version to", "Version base", "Merged" };
		int[] bounds = { 600, 100, 100, 100, 100, 50 };

		// First column is for the name
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MergeData data = (MergeData) element;
				return data.getFileName();
			}
		});

		// Second column is for automatic flag
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MergeData data = (MergeData) element;
				return data.isAutomatic() == true ? "yes" : "no";
			}
		});

		// Third the from version
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MergeData data = (MergeData) element;
				return data.getFrom();
			}
		});

		// Fourth the to version
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MergeData data = (MergeData) element;
				return data.getTo();
			}

		});

		// Fifth the base version
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MergeData data = (MergeData) element;
				return data.getBase();
			}
		});

		col = createTableViewerColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((MergeData) element).isMerged())
					return CHECKED;
				else
					return UNCHECKED;
			}
		});

	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public Vector<MergeData> getData() {
		return data;
	}

	public void setData(Vector<MergeData> data) {
		this.data = data;
		viewer.setInput(data);
	}

	public ClearCaseProvider getProvider() {
		return provider;
	}

	public void setProvider(ClearCaseProvider provider) {
		this.provider = provider;
	}

	public boolean isAutoMerge() {
		return autoMerge;
	}

	public void setAutoMerge(boolean autoMerge) {
		this.autoMerge = autoMerge;
	}

	/** * Passing the focus request to the viewer's control. */

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IStructuredSelection getSelection() {
		return selection;
	}

	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	// Used to update the viewer from outsite
	public void refresh() {
		viewer.refresh();
	}

	private void merge() {

		AutoMergeQuestionRunnable checkoutQuestion = new AutoMergeQuestionRunnable();
		getSite().getShell().getDisplay().syncExec(checkoutQuestion);
		int returncode = checkoutQuestion.getResult();
		if (checkoutQuestion.isRemember()) {
			if (returncode == IDialogConstants.YES_ID) {
				ClearCasePreferences.setMergeAutomatic(true);
			} else if (returncode == IDialogConstants.NO_ID) {
				ClearCasePreferences.setMergeAutomatic(false);
			}
		}
		// if (returncode != IDialogConstants.YES_ID)
		// return new Status(IStatus.CANCEL, ClearCasePlugin.PLUGIN_ID,
		// "Chec, operation was cancelled by user.");

		if (selection != null && selection instanceof IStructuredSelection) {
			// Vector<MergeData> persons = getData();
			IStructuredSelection sel = selection;

			for (Iterator<MergeData> iterator = sel.iterator(); iterator.hasNext();) {
				MergeData data = iterator.next();
				if (data.isAutomatic() && ClearCasePreferences.isMergeAutomatic()) {
					// Merge automatic using clearcase support! And update flag
					// in list with merged.
					// FIXME: checkout target file.

					// Merge it.
					provider.merge(data.getFileName(), data.getFrom(), data.getBase());
					// FIXME:CHECK that it was ok.
					// contrib-&-result-pname.contrib
					// update merge flag.
				} else {
					// Manual merge. Open up the merge window ( internal
					// or external).
					IResource resource = null;
					IPath path = new Path(data.getFileName());
					File f = new File(data.getFileName());
					if (f.isFile()) {
						resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);

					} else {
						resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);

					}

					MergeResourcesOperation mainOp = new MergeResourcesOperation(resource, data.getTo(), data.getFrom(), data.getBase(), provider);
					mainOp.merge();

				}

			}
			viewer.refresh();
		}
	}

	private class AutoMergeQuestionRunnable implements Runnable {

		private int dialogResult;

		private boolean remember;

		public void run() {
			MessageDialogWithToggle checkoutQuestion = new MessageDialogWithToggle(getSite().getShell().getDisplay().getActiveShell(), "Merge Branch Automatic", null, "Merge automatic where possible?.\n", MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0, "Remember my decision", false);
			checkoutQuestion.open();
			dialogResult = checkoutQuestion.getReturnCode();
			remember = checkoutQuestion.getToggleState();
		}

		public int getResult() {
			return dialogResult;
		}

		public boolean isRemember() {
			return remember;
		}

	}

}
