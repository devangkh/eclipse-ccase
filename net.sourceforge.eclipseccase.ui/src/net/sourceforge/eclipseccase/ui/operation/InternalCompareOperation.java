package net.sourceforge.eclipseccase.ui.operation;

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

	public InternalCompareOperation(IResource resource, String selectedFile, String comparableVersion, ClearCaseProvider provider) {
		this.resource = resource;
		this.selected = selectedFile;
		this.comparableVersion = comparableVersion;
		this.provider = provider;
		setup();
		cmpConfig = new CompareConfiguration();
	}

	public InternalCompareOperation(IResource resource, String selectedFile, String comparableVersion, ClearCaseProvider provider, boolean differentView) {
		this(resource, selectedFile, comparableVersion, provider);
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
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();
			VersionCompareInput input = new VersionCompareInput(cmpConfig, (IFile) resource, selected, comparableVersion, page, provider, differentView);
			CompareUI.openCompareEditor(input);
		}

	}

}
