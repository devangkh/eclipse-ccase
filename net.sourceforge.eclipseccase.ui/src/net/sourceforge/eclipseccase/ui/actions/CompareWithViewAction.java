package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.clearcase.utils.Os;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.dialogs.SelectViewDialog;
import net.sourceforge.eclipseccase.ui.operation.CompareResourcesOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

public class CompareWithViewAction extends ClearCaseWorkspaceAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled() {
		// This function cannot used in windows.
		if (Os.isArch(Os.WINDOWS))
			return false;
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
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource resource = this.getSelectedResources()[0];
		final ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				SelectViewDialog dlg = new SelectViewDialog(getShell());
				dlg.setBlockOnOpen(true);
				if (dlg.open() == Window.OK) {
					final String selectedVersion = provider.getVersion(resource);
					final String selectedView = (dlg.getManualView() != null && !dlg.getManualView().equals("")) ? dlg.getManualView() : dlg.getSelectedView();
					if (selectedView == null || selectedView.equals("")) {
						MessageDialog.openInformation(getShell(), "No view selected", "No view selected. Compare with View aborted.");
					} else {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) {
								monitor.beginTask("Accessing ClearCase. This may take a while...", IProgressMonitor.UNKNOWN);
								ClearCaseProvider provider = new ClearCaseProvider();
								provider.setView(selectedView);
								monitor.done();
							}
						};

						executeInBackground(runnable, "set view");
						// Make sure this is not in non ui thread above (
						// runnable) since all window will be null.
						IWorkbenchPage page;
						if (getWindow() == null) {
							IWorkbench wb = PlatformUI.getWorkbench();
							IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
							page = win.getActivePage();
						} else {
							page = getWindow().getActivePage();
						}
						CompareResourcesOperation mainOp = new CompareResourcesOperation(resource, selectedVersion, selectedView, provider, page, true);
						mainOp.compare();
					}
				}
			}
		});

	}

}
