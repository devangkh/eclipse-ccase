/*******************************************************************************
 * Copyright (c) 2011 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     mikael petterson - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.actions;

import java.util.Iterator;

import java.util.List;

import java.util.ArrayList;

import net.sourceforge.eclipseccase.ClearDlgHelper;

import net.sourceforge.eclipseccase.ClearCasePreferences;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
import net.sourceforge.eclipseccase.ui.wizards.CheckinWizard;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;

/**
 * @author mikael petterson
 * 
 */
public class CheckInAction extends ClearCaseWorkspaceAction {

	/*
	 * @see TeamAction#execute(IAction)
	 */
	@Override
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		boolean canContinue = true;
		// prompt for saving dirty editors
		IFile[] unsavedFiles = getUnsavedFiles();
		if (unsavedFiles.length > 0) {
			canContinue = saveModifiedResourcesIfUserConfirms(unsavedFiles);
		}

		if (canContinue) {

			//final IResource[] resources = getSelectedResources();
			final List<IResource> resources = Arrays.asList(getSelectedResources());
			final List<IResource> identical = new ArrayList<IResource>();
			List<IResource> modified = new ArrayList<IResource>();
			final ClearCaseProvider provider = new ClearCaseProvider();

			if (resources.size() > 0) {
				// check for identical resources
				for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
					IResource iResource = (IResource) iterator.next();
					if(!provider.isDifferent(iResource.getLocation().toOSString())){
						identical.add(iResource);
					}
				}
				
				if(identical.size() > 0 && !ClearCasePreferences.isCheckinIdenticalAllowed()){
					//We have identical resources and we should not check them in.
					//Remove from list of resources
					modified = sortOutIdentical(resources,identical);
				}else{
					modified = resources;
				}
				
				if (ClearCasePreferences.isUseClearDlg()) {

					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor) throws CoreException {
							try {
								monitor.subTask("Executing ClearCase user interface...");
								ClearDlgHelper.checkin(resources.toArray(new IResource[resources.size()]));
							} finally {
								monitor.done();
								updateActionEnablement();
							}
						}
					};
					executeInBackground(runnable, "Checking in ClearCase resources");
				} else {

					//ClearCaseProvider provider = new ClearCaseProvider();
					//CheckinWizard wizard = new CheckinWizard(resources, provider);
					//CheckinWizard wizard = new CheckinWizard(modified,identical,provider);
					//CheckinWizard wizard = new CheckinWizard(resources.toArray(new IResource[resources.size()]), identical, provider)
//					WizardDialog dialog = new WizardDialog(getShell(), wizard);
//					dialog.open();
				}
			}
		}
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
			if (!provider.isCheckedOut(resource))
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param resources
	 * @param identical
	 * @return modified resources
	 */
	private List<IResource> sortOutIdentical(List<IResource> resources,List<IResource>identical){
		resources.removeAll(identical);
		return resources;
	}

}
