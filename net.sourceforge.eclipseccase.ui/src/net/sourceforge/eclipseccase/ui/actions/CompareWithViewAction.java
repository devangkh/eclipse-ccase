/*******************************************************************************
 * Copyright (c) 2013 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Contributions from Marcel Overdijk.
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.actions;

import java.lang.reflect.InvocationTargetException;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.dialogs.SelectViewDialog;
import net.sourceforge.eclipseccase.ui.operation.CompareResourcesOperation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;

public class CompareWithViewAction extends ClearCaseWorkspaceAction {

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
						IRunnableWithProgress op = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask("Accessing ClearCase. This may take a while...", IProgressMonitor.UNKNOWN);
								//TODO:Put this in ClearCaseProvider class.
								String command[] = { "cleartool", "setview", "-exec", "exit", selectedView };
								CommandLauncher launcher = new CommandLauncher();
								launcher.setGatherOutput(true);
								launcher.execute(command, null, null, null);
								int exitValue = launcher.getExitValue();
								if (exitValue > 0) {
									throw new InvocationTargetException(new IllegalStateException("Something went wrong during setting view."));
								} 
								monitor.done();
								if (monitor.isCanceled()) {
									throw new InterruptedException("Compare with view was cancelled.");
								}
								CompareResourcesOperation mainOp = new CompareResourcesOperation(resource, selectedVersion, selectedView, provider, true);
								mainOp.compare();
							}
						};
						try {
							new ProgressMonitorDialog(getShell()).run(false, false, op);
						} catch (InvocationTargetException e) {
							MessageDialog.openError(getShell(), "Error", e.getMessage());
						} catch (InterruptedException e) {
							MessageDialog.openInformation(getShell(), "Error", e.getMessage());
						}
					}
				}
			}
		});
	}
}
