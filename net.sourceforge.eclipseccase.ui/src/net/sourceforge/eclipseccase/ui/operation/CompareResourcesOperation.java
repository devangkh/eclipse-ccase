package net.sourceforge.eclipseccase.ui.operation;

import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCasePreferences;

public class CompareResourcesOperation {

	private IResource resource;

	private String selectedVersion;

	private String comparableVersion;

	private ClearCaseProvider provider;

	private boolean differentView = false;
	//We need to have the active workbench page for internal compare.
	private IWorkbenchPage page;

	public CompareResourcesOperation(IResource resource, String selectedVersion, String preVersion, ClearCaseProvider provider,IWorkbenchPage page) {
		this.resource = resource;
		this.selectedVersion = selectedVersion;
		this.comparableVersion = preVersion;
		this.provider = provider;
		this.page = page;

	}
	
	public CompareResourcesOperation(IResource resource, String selectedVersion, String preVersion, ClearCaseProvider provider, IWorkbenchPage page, boolean differentView) {
		this(resource, selectedVersion, preVersion, provider,page);
		this.differentView = differentView;
	}
	

	public void compare() {

		if (ClearCasePreferences.isCompareExternal()) {
			ExternalCompareOperation extCmpOp = new ExternalCompareOperation(resource, comparableVersion, provider,differentView);
			// TODO: Testing threading...
			extCmpOp.run();
		} else {
			InternalCompareOperation intCmpOp = new InternalCompareOperation(resource,selectedVersion,comparableVersion,provider,page,differentView);
			intCmpOp.execute();
		}
	}

}
