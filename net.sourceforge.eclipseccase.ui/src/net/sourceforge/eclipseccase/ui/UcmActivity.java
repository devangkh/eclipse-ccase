/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import org.eclipse.team.core.TeamException;

import org.eclipse.core.runtime.NullProgressMonitor;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.dialogs.ActivityDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class sets/creates a ucm activity used for checkout or add operation.
 * 
 * @author eraonel
 * 
 */
public class UcmActivity {

	// private class
	private static class ActivityDialogRunnable implements Runnable {

		private Shell shell;

		private ClearCaseProvider provider;

		private IResource[] resources;

		private boolean checkout;

		public ActivityDialogRunnable(ClearCaseProvider provider, IResource[] resources, Shell shell) {
			this.provider = provider;
			this.resources = resources;
			this.shell = shell;
		}

		public void run() {
			final IResource resource = resources[0];
			String view = ClearCaseProvider.getViewName(resource);
			if (view == "") {
				setCheckout(false);
				return;
			}
			ActivityDialog dlg = new ActivityDialog(shell, provider, resource);
			dlg.setBlockOnOpen(true);
			if (dlg.open() == Window.OK) {
				String activity = dlg.getSelectedActivity();
				if (activity != null) {
					provider.setActivity(activity, view);
					try {
						provider.checkout(resources, IResource.DEPTH_ZERO, new NullProgressMonitor());
					} catch (TeamException e) {
						setCheckout(false);
					}
					setCheckout(true);
				}
			} else {
				// Answer was N or Cancel.
				setCheckout(false);
			}
		}

		public void setCheckout(boolean checkout) {
			this.checkout = checkout;
		}

		public boolean getCheckout() {
			return checkout;
		}

	}

	/**
	 * 
	 * @param provider
	 * @param resources
	 * @param shell
	 * @return
	 */
	public static boolean checkoutWithActivity(ClearCaseProvider provider, IResource[] resources, Shell shell) {

		ActivityDialogRunnable activityDialog = new ActivityDialogRunnable(provider, resources, shell);
		Display.getDefault().syncExec(activityDialog);
		return activityDialog.getCheckout();

	}

}
