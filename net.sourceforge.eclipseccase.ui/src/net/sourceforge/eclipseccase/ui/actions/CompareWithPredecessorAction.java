package net.sourceforge.eclipseccase.ui.actions;


import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.IWorkbench;

import org.eclipse.ui.IWorkbenchPage;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.operation.CompareResourcesOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

/**
 * Pulls up the compare with predecessor dialog.
 */
public class CompareWithPredecessorAction extends ClearCaseWorkspaceAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0)
			return false;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
				return false;
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		IResource resource = this.getSelectedResources()[0];
		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
		if (provider != null) {
			String selectedVersion = provider.getVersion(resource);
			String preVersion = provider.getPredecessorVersion(resource);
			IWorkbenchPage page;
			if(getWindow() == null){
				IWorkbench wb = PlatformUI.getWorkbench();
				   IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				   page = win.getActivePage();
			}else{
				page = getWindow().getActivePage();
			}
			CompareResourcesOperation mainOp = new CompareResourcesOperation(resource, selectedVersion, preVersion, provider,page);
			mainOp.compare();

		}

	}

}
