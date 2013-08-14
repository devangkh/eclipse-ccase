package net.sourceforge.eclipseccase.views.historyviewer;


import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import net.sourceforge.eclipseccase.ui.actions.VersionTreeAction;

import net.sourceforge.eclipseccase.ui.actions.CompareWithVersionAction;
import org.eclipse.jface.action.IAction;

import org.eclipse.swt.widgets.Display;

import java.util.Vector;
import org.eclipse.core.resources.IResource;

import net.sourceforge.clearcase.ElementHistory;




import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Mattias Rundgren
 *
 */
public class JFHistoryViewer extends ViewPart {
	public static final String ID = "net.sourceforge.eclipseccase.views.HistoryViewer";
	private HistoryViewerComparator comparator;

	private TableViewer viewer;
	private ElementHistoryFilter filter;
	private IResource element;
	private Label element2;
	private Action compareAction;
	private Action versionTreeAction;
	private Menu historyMenu;
	private MenuItem compareMenuItem;
	private MenuItem versionTreeItem;

	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		Label searchLabel = new Label(parent, SWT.NONE);
		searchLabel.setText("Search: ");
		final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		Label elementLabel = new Label(parent, SWT.NONE);
		elementLabel.setText("Element:");
		element2 = new Label(parent, SWT.NONE);
		element2.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		createViewer(parent);
		// Set the sorter for the table
		comparator = new HistoryViewerComparator();
		viewer.setComparator(comparator);

		// New to support the search
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				filter.setSearchText(searchText.getText());
				viewer.refresh();
			}

		});
		filter = new ElementHistoryFilter();
		viewer.addFilter(filter);
		////////////////////
		historyMenu = new Menu(parent.getShell(), SWT.POP_UP);
		compareMenuItem = new MenuItem(historyMenu, SWT.PUSH);
		compareMenuItem.setText("Compare with...");
		compareMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compare();
			}
		});
		compareMenuItem.setEnabled(false);

		versionTreeItem = new MenuItem(historyMenu, SWT.PUSH);
		versionTreeItem.setText("Show Version Tree");
		versionTreeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				versionTree();
			}
		});
		versionTreeItem.setEnabled(true);

		viewer.getTable().setMenu(historyMenu);

		viewer.getTable().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (viewer.getTable().getSelectionCount() == 1) {
					compareMenuItem.setText("Compare Active with selected");
					compareMenuItem.setEnabled(true);
					compareAction.setEnabled(true);
				} else if (viewer.getTable().getSelectionCount() == 2) {
					compareMenuItem.setText("Compare selected versions");
					compareMenuItem.setEnabled(true);
					compareAction.setEnabled(true);
				} else {
					compareMenuItem.setText("Compare with...");
					compareMenuItem.setEnabled(false);
					compareAction.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// System.out.println("select default");
			}
		});

		compareAction = new Action() {
			@Override
			public void run() {
				compare();
			}
		};

		versionTreeAction = new Action() {
			@Override
			public void run() {
				versionTree();
			}
		};
		new Action() {
			@Override
			public void run() {
				open();
			}
		};

		compareAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/diff.png"));
		compareAction.setToolTipText("Compare with history");
		compareAction.setEnabled(false);
		versionTreeAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/tree.png"));
		versionTreeAction.setToolTipText("Open Version Tree");
		versionTreeAction.setEnabled(false);

		getViewSite().getActionBars().getToolBarManager().add(compareAction);
		getViewSite().getActionBars().getToolBarManager().add(versionTreeAction);
		getViewSite().getActionBars().getToolBarManager().update(true);


	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		viewer.setInput(ModelProvider.INSTANCE.getElements());
		// Make the selection available to other views
		getSite().setSelectionProvider(viewer);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
	}

	public TableViewer getViewer() {
		return viewer;
	}

	// This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
//		String[] titles = { "Element", "Version", "User", "Comment" };
		String[] titles = { "Date", "User", "Version", "Label", "Comment" };
		int[] bounds = { 100, 100, 100, 100, 100};

		// Date
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ElementHistory p = (ElementHistory) element;
				return p.getDate();
			}
		});

		// User
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ElementHistory p = (ElementHistory) element;
				return p.getuser();
			}
		});

		// Version
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ElementHistory p = (ElementHistory) element;
				return p.getVersion();
			}
		});

		// Label
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ElementHistory p = (ElementHistory) element;
				return p.getLabel();
			}
		});

		// Comment
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ElementHistory p = (ElementHistory) element;
				return p.getComment();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
				viewer.getTable().showSelection();
			}
		};
		return selectionAdapter;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setHistoryInformation(IResource element, Vector<ElementHistory> history) {
		this.element = element;

		ModelProvider.INSTANCE.setElements(history);
		Display.getDefault().asyncExec(new Runnable() {
               public void run() {
            	   element2.setText(JFHistoryViewer.this.element.getFullPath().toOSString());
            	   getViewer().refresh(true);
               }
            });
	}

	private void compare() {
		if (viewer.getTable().getSelectionCount() == 1 || viewer.getTable().getSelectionCount() == 2) {
			//CompareWithVersionAction action = new CompareWithVersionAction();
			//Send along the active page.
			CompareWithVersionAction action = new CompareWithVersionAction(getSite().getPage());
			action.setResource(element);
			if (viewer.getTable().getSelectionCount() == 1) {
				action.setVersionB(viewer.getTable().getSelection()[0].getText(2));
				// Set version b to current version of element?
			} else if (viewer.getTable().getSelectionCount() == 2) {
				action.setVersionA(viewer.getTable().getSelection()[0].getText(2));
				action.setVersionB(viewer.getTable().getSelection()[1].getText(2));
			}
			action.execute((IAction) null);
		}
	}

	private void versionTree() {
		VersionTreeAction action = new VersionTreeAction();
		action.setResource(element);
		action.execute((IAction) null);
	}

	private void open() {
	
	}
}
