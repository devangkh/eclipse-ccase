/*******************************************************************************
 * Copyright (c) 2012 eclipse-ccase.sourceforge.net.
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

import net.sourceforge.eclipseccase.ui.wizards.LabelWizard;

import net.sourceforge.eclipseccase.ui.wizards.MergeWizard;

import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.ClearDlgHelper;
import net.sourceforge.eclipseccase.ui.wizards.CheckinWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IAction;

/**
 * @author mikael petterson
 * 
 */
public class LabelAction extends ClearCaseWorkspaceAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.internal.ui.actions.TeamAction#execute(org.eclipse.jface
	 * .action.IAction)
	 */
	@Override
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		ClearCaseProvider provider = new ClearCaseProvider();
		LabelWizard wizard = new  LabelWizard(resources, provider);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.open();

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
		}
		return true;
	}

}
