package net.sourceforge.eclipseccase.ui.actions;

import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.operation.CompareResourcesOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithVersionAction extends ClearCaseWorkspaceAction {
	
	private IWorkbenchPage page;

	private IResource resource = null;

	private String versionA = null;

	private String versionB = null;
	
	public CompareWithVersionAction(IWorkbenchPage page){
		this.page = page;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}

	public void setVersionA(String versionA) {
		this.versionA = versionA;
	}

	public void setVersionB(String versionB) {
		this.versionB = versionB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		if (resource != null) {
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider != null && !provider.isUnknownState(resource) && !provider.isIgnored(resource) && provider.isClearCaseElement(resource))
				return true;
		}

		return false;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
		if (provider != null) {
			//This is to handle when only one is selected from history. Implicitly the versionA is the cone selected by view.
			if(versionA == null){
				versionA = provider.getVersion(resource);
			}
			CompareResourcesOperation mainOp = new CompareResourcesOperation(resource, versionA, versionB, provider,page);
			mainOp.compare();

		}
	}
}
