package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ui.actions.CompareWithVersionAction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.actions.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class BranchSearchView extends ViewPart {

	Combo projectName;

	Button searchButton;

	Text branchName;

	ScrolledForm form;

	List filesResult;

	String[] branchListString;

	List branchList;

	Shell popupBranchList = null;

	String[] searchResult = null;

	Button compareWithPreviousButton;

	Button compareWithParentButton;

	Button compareWithCurrentButton;

	Button showVersionTreeButton;

	Button showHistoryButton;

	enum Version {
		Previous, Parent, Current
	}

	/**
	 * Create the Search in Branch form UI.
	 */
	@Override
	public void createPartControl(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("ClearCase Search");

		toolkit.decorateFormHeading(form.getForm());

		GridLayout gridlayout = new GridLayout();
		gridlayout.numColumns = 5;

		Composite head = toolkit.createComposite(form.getForm().getHead());

		form.setHeadClient(head);
		head.setLayout(gridlayout);

		toolkit.createLabel(head, "Branch name: ");
		branchName = toolkit.createText(head, "", SWT.BORDER);
		searchButton = toolkit.createButton(head, "Search", SWT.PUSH);
		branchName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.createLabel(head, "Project: ");

		projectName = new Combo(head, SWT.READ_ONLY);

		setProject(null);

		FillLayout bodylayout = new FillLayout(SWT.BORDER | SWT.VERTICAL);
		form.getBody().setLayout(bodylayout);

		Composite body = toolkit.createComposite(form.getBody());

		GridLayout gridBodylayout = new GridLayout();
		gridBodylayout.numColumns = 1;
		body.setLayout(gridBodylayout);

		GridLayout gridButton = new GridLayout();
		gridButton.numColumns = 6;
		Composite bodyButtons = toolkit.createComposite(body);
		bodyButtons.setLayout(gridButton);

		toolkit.createLabel(bodyButtons, "Compare with: ");
		compareWithPreviousButton = toolkit.createButton(bodyButtons, "Previous", SWT.PUSH);
		compareWithPreviousButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/diff.png").createImage());
		compareWithParentButton = toolkit.createButton(bodyButtons, "Parent", SWT.PUSH);
		compareWithParentButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/diff.png").createImage());
		compareWithCurrentButton = toolkit.createButton(bodyButtons, "Current", SWT.PUSH);
		compareWithCurrentButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/diff.png").createImage());

		showVersionTreeButton = toolkit.createButton(bodyButtons, "Show Version Tree", SWT.PUSH);
		showVersionTreeButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/tree.png").createImage());

		showHistoryButton = toolkit.createButton(bodyButtons, "Show History", SWT.PUSH);
		showHistoryButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("net.sourceforge.eclipseccase.ui", "icons/full/history.png").createImage());

		compareWithPreviousButton.setEnabled(false);
		compareWithParentButton.setEnabled(false);
		compareWithCurrentButton.setEnabled(false);
		showVersionTreeButton.setEnabled(false);
		showHistoryButton.setEnabled(false);

		filesResult = new List(body, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);

		filesResult.setLayoutData(new GridData(GridData.FILL_BOTH));

		/* Add listeners */

		branchName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// branchName.removeModifyListener(this);
				autoComplete();
				// branchName.addModifyListener(this);
			}
		});

		branchName.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((e.character == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR)) {
					executeSearch();
				}
				if ((e.keyCode == SWT.ESC) && (popupBranchList != null)) {
					popupBranchList.setVisible(false);
				}
				if (((e.keyCode == SWT.ARROW_UP) || (e.keyCode == SWT.ARROW_DOWN)) && (popupBranchList != null)) {
					popupBranchList.setVisible(true);
					branchList.setFocus();
				}
			}
		});

		branchName.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
			}
		});

		searchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.widget == searchButton) {
					executeSearch();
				}
			}
		});

		compareWithPreviousButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((event.widget == compareWithPreviousButton) && (filesResult.getSelection().length > 0)) {
					compareWith(Version.Previous);
				}
			}
		});
		compareWithParentButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((event.widget == compareWithParentButton) && (filesResult.getSelection().length > 0)) {
					compareWith(Version.Parent);
				}
			}
		});
		compareWithCurrentButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((event.widget == compareWithCurrentButton) && (filesResult.getSelection().length > 0)) {
					compareWith(Version.Current);
				}
			}
		});

		showVersionTreeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((event.widget == showVersionTreeButton) && (filesResult.getSelection().length > 0)) {
					showVersionTree();
				}
			}
		});

		showHistoryButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((event.widget == showHistoryButton) && (filesResult.getSelection().length > 0)) {
					showHistory();
				}
			}
		});

		projectName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				newProjectSelected();
			}
		});

		filesResult.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event e) {
				openSelectedResource();
			}
		});

	}

	@Override
	public void setFocus() {

	}

	/**
	 * Select new active project.
	 * 
	 * @param project
	 *            current active project.
	 */
	public void setProject(IProject project) {
		int cursor = 0;
		int projectCursor = 0;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().get
		projectName.removeAll();
		for (int i = 0; i < projects.length; i++) {
			if ((!(projects[i].isHidden())) && (projects[i].isOpen())) {
				// ClearCaseProvider provider =
				// ClearCaseProvider.getClearCaseProvider(projects[i]);
				// if((provider != null) &&
				// (provider.isClearCaseElement(projects[i])))
				// {
				projectName.add(projects[i].getName());
				if (project == projects[i]) {
					projectCursor = cursor;
				}
				cursor++;
				// }
			}
		}
		projectName.select(projectCursor);

		newProjectSelected();
	}

	public void newProjectSelected() {
		branchName.setFocus();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());

		LoadBrancheListAction getBranches = new LoadBrancheListAction(this, project);
		branchListString = null;
		try {
			getBranches.execute((IAction) null);
		} catch (Exception e) {

		}
	}

	private void executeSearch() {
		// TODO implement code.
		if (popupBranchList != null) {
			popupBranchList.setVisible(false);
		}
		filesResult.removeAll();

		projectName.setEnabled(false);
		searchButton.setEnabled(false);
		branchName.setEnabled(false);

		compareWithPreviousButton.setEnabled(false);
		compareWithParentButton.setEnabled(false);
		compareWithCurrentButton.setEnabled(false);
		showVersionTreeButton.setEnabled(false);
		showHistoryButton.setEnabled(false);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());

		BranchSearchAction search = new BranchSearchAction(this, project, branchName.getText());
		try {
			search.execute((IAction) null);
		} catch (Exception e) {

		}
	}

	private void compareWith(Version type) {
		String[] fileList = filesResult.getSelection();
		Pattern fileVersion = Pattern.compile("^(.*)@@(.*)$");

		String filePath;
		String version;

		String version1 = "";
		String version2;

		Matcher m;

		for (String file : fileList) {
			if ((m = fileVersion.matcher(file)).matches()) {
				filePath = m.group(1);
				version = m.group(2);

				version2 = filePath + "@@" + version + "/LATEST";

				switch (type) {
				case Previous: {
					version1 = "-predecessor";
				}
					break;
				case Parent: {
					version1 = filePath + "@@" + version + "/0";
				}
					break;
				case Current: {
					version1 = filePath;
				}
					break;
				}

				compare(version1, version2);
			}
		}

	}

	private void compare(String version1, String version2) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());
		CompareWithVersionAction action = new CompareWithVersionAction(getSite().getPage());
		action.setResource(project);
		action.setVersionA(version1);
		action.setVersionB(version2);

		action.execute((IAction) null);
//TODO: Seems to be duplication of code.
//		ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(project);
//		if (p != null) {
//			p.compareWithVersion(version1, version2);
//		}
	}

	private void showVersionTree() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());
		String[] fileList = filesResult.getSelection();

		for (String file : fileList) {
			VersionTreeAction action = new VersionTreeAction();
			action.setResource(project);
			action.setFile(file + "/LATEST");
			action.execute((IAction) null);
		}
	}

	private void showHistory() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());
		String[] fileList = filesResult.getSelection();

		for (String file : fileList) {
			HistoryAction action = new HistoryAction();
			action.setResource(project);
			action.setFileVersion(file + "/LATEST");
			action.execute((IAction) null);
			return; // run it only for first selected element.
		}
	}

	private void openSelectedResource() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName.getText());
		String[] fileList = filesResult.getSelection();
		Pattern fileVersion = Pattern.compile("^(.*)@@(.*)$");

		String filePath;

		Matcher m;

		for (String file : fileList) {
			if ((m = fileVersion.matcher(file)).matches()) {
				filePath = m.group(1);

				filePath = filePath.replace(project.getRawLocation().toOSString(), "");

				IResource ressource = project.findMember(filePath);

				if (ressource != null) {
					try {
						try {
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							if (page != null) {
								org.eclipse.ui.ide.IDE.openEditor(page, (IFile) ressource);
							}
						} catch (CoreException ex) {
							String title = "Error opening Editor";
							String message = "Could not open Editor";
							ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message, ex.getStatus());
						}

					} catch (Exception e) {
					}
				}
			}
		}
	}

	public void setBranches(String[] branches) {
		this.branchListString = branches;
	}

	public void setSearchResult(String[] result) {
		searchResult = result;

		filesResult.getDisplay().asyncExec(new Runnable() {
			public void run() {
				filesResult.removeAll();
				filesResult.setItems(searchResult);
				projectName.setEnabled(true);
				searchButton.setEnabled(true);
				branchName.setEnabled(true);

				compareWithPreviousButton.setEnabled(true);
				compareWithParentButton.setEnabled(true);
				compareWithCurrentButton.setEnabled(true);
				showVersionTreeButton.setEnabled(true);
				showHistoryButton.setEnabled(true);
			}
		});
	}

	private void createPopupList() {
		if (popupBranchList == null) {
			popupBranchList = new Shell(branchName.getDisplay(), SWT.ON_TOP);

			popupBranchList.setLayout(new FillLayout());
			branchList = new List(popupBranchList, SWT.BORDER | SWT.V_SCROLL);

			branchList.addKeyListener(new KeyListener() {

				public void keyReleased(KeyEvent e) {
				}

				public void keyPressed(KeyEvent e) {
					if ((e.character == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR)) {
						if (branchList.getSelection().length > 0) {
							setBranchName(branchList.getSelection()[0]);
						}

					}
					if ((e.keyCode == SWT.ESC)) {
						popupBranchList.setVisible(false);
					}
				}
			});

			branchList.addFocusListener(new FocusListener() {

				public void focusLost(FocusEvent e) {
					if (!branchName.isFocusControl()) {
						popupBranchList.setVisible(false);
					}
				}

				public void focusGained(FocusEvent e) {
				}
			});

			branchList.addMouseListener(new MouseListener() {

				public void mouseUp(MouseEvent e) {
				}

				public void mouseDown(MouseEvent e) {
				}

				public void mouseDoubleClick(MouseEvent e) {
					if (branchList.getSelection().length > 0) {
						setBranchName(branchList.getSelection()[0]);
					}
				}
			});

		}
	}

	private void autoComplete() {
		if (this.branchListString != null) {
			createPopupList();

			branchList.removeAll();

			for (String branch : branchListString) {
				// if(branchName.getText() == "")
				// {
				// branchList.add(branch);
				// }
				// else
				{
					Pattern p = Pattern.compile("^.*" + branchName.getText().toLowerCase() + ".*$");
					if (p.matcher(branch.toLowerCase()).matches()) {
						branchList.add(branch);
					}
				}

				// if(branch.toLowerCase().startsWith(branchName.getText().toLowerCase()))
				// {
				// branchList.add(branch);
				// }
			}

			if (branchList.getItemCount() == 0) {
				popupBranchList.setVisible(false);
			} else {
				Rectangle textBounds = branchName.getDisplay().map(branchName, null, branchName.getBounds());
				popupBranchList.setBounds(textBounds.x - branchName.getBounds().x - 2, textBounds.y + textBounds.height - branchName.getBounds().y - 2, textBounds.width, 150);
				popupBranchList.setVisible(true);
			}
		}
	}

	private void setBranchName(String branchName) {
		this.branchName.setText(branchName);
		this.branchName.setSelection(branchName.length());
		this.branchName.setFocus();

		popupBranchList.setVisible(false);
	}

}
