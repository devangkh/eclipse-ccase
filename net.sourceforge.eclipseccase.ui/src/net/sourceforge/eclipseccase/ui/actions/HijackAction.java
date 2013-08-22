/*******************************************************************************
 * Copyright (c) 2010 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.actions;

import org.eclipse.core.resources.ResourceAttributes;

import net.sourceforge.eclipseccase.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jface.action.IAction;

/**
 * @author mikael
 * 
 */
public class HijackAction extends ClearCaseWorkspaceAction {

	static class HijackQuestion implements Runnable {
		private int returncode;

		public int getReturncode() {
			return returncode;
		}

		public void run() {
			Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			MessageDialog unhijackQuestion = new MessageDialog(activeShell, "Hijack", null, "Do you really want to Hijack file (make local file writeable)?", MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
			returncode = unhijackQuestion.open();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.internal.ui.actions.TeamAction#execute(org.eclipse.jface
	 * .action.IAction)
	 */
	@Override
	public void execute(IAction action) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IResource[] resources = getSelectedResources();
					beginTask(monitor, "Hijacking files...", resources.length);

					HijackQuestion question = new HijackQuestion();

					PlatformUI.getWorkbench().getDisplay().syncExec(question);

					/* Yes=0 No=1 Cancel=2 */
					if (question.getReturncode() == 0) {

						for (int i = 0; i < resources.length; i++) {
							IResource resource = resources[i];
							ResourceAttributes attributes = resource.getResourceAttributes();
							if (attributes != null && attributes.isReadOnly()) {
								attributes.setReadOnly(false);
								resource.setResourceAttributes(attributes);
							} else {
								System.out.println("Error: Could not make resource " + resource.getName() + " writeable!");
							}
						}
					}

				} finally {
					updateActionEnablement();
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "Hijack resources from ClearCase");
	}

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
			if (provider.isHijacked(resource))
				return false;
		}
		return true;
	}

}
