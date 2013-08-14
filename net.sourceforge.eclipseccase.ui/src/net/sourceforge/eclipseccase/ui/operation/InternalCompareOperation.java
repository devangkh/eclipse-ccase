package net.sourceforge.eclipseccase.ui.operation;

import org.eclipse.core.runtime.Status;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.compare.ITypedElement;

import org.eclipse.compare.ResourceNode;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbench;

import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.eclipseccase.ui.compare.ClearCaseResourceNode;

import org.eclipse.core.resources.IFile;

import org.eclipse.compare.CompareUI;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;

import org.eclipse.compare.CompareConfiguration;

public class InternalCompareOperation {

	private String selected;

	private IResource resource;

	private String comparableVersion;

	private CompareConfiguration cmpConfig;

	private ClearCaseProvider provider;

	private boolean differentView = false;
	
	private IWorkbenchPage page;

	public InternalCompareOperation(IResource resource, String selectedFile, String comparableVersion, ClearCaseProvider provider,IWorkbenchPage page) {
		this.resource = resource;
		this.selected = selectedFile;
		this.comparableVersion = comparableVersion;
		this.provider = provider;
		this.page = page;
		setup();
		cmpConfig = new CompareConfiguration();
	}
	
	public InternalCompareOperation(IResource resource, String selectedFile, String comparableVersion, ClearCaseProvider provider, IWorkbenchPage page,boolean differentView) {
		this(resource, selectedFile, comparableVersion, provider,page);
		this.differentView = differentView;
	
	}

	private void setup() {
		cmpConfig = new CompareConfiguration();
		cmpConfig.setLeftEditable(true);// lview private version or latest. Can
										// be changed.
		cmpConfig.setRightEditable(false);

	}

	public void execute() {
		// execute
		if (resource instanceof IFile) {
			VersionCompareInput input = new VersionCompareInput(cmpConfig, (IFile) resource, selected, comparableVersion, page, provider, differentView);
			CompareUI.openCompareEditor(input);
		}else{
			//This is only for safety.
			ErrorDialog.openError(page.getActivePart().getSite().getShell(), "Not a file", "You need to select a file!", new Status(IStatus.WARNING, "Selected resource is not a file", "", null));
		}

	}

}
