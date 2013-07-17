package net.sourceforge.eclipseccase.ui.operation;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCasePreferences;

public class CompareResourcesOperation {

	private IResource resource;

	private String selectedVersion;

	private String comparableVersion;

	private ClearCaseProvider provider;

	private boolean differentView = false;

	public CompareResourcesOperation(IResource resource, String selectedVersion, String preVersion, ClearCaseProvider provider) {
		this.resource = resource;
		this.selectedVersion = selectedVersion;
		this.comparableVersion = preVersion;
		this.provider = provider;

	}

	public CompareResourcesOperation(IResource resource, String selectedVersion, String preVersion, ClearCaseProvider provider, boolean differentView) {
		this(resource, selectedVersion, preVersion, provider);
		this.differentView = differentView;
	}

	public void compare() {

		if (ClearCasePreferences.isCompareExternal()) {

			ExternalCompareOperation extCmpOp = new ExternalCompareOperation(resource, comparableVersion, provider);
			// extCmpOp.execute();
			// TODO: Testing threading...
			extCmpOp.run();
		} else {
			InternalCompareOperation intCmpOp = new InternalCompareOperation(resource,selectedVersion,comparableVersion,provider,differentView);
			intCmpOp.execute();
		}
	}

}
