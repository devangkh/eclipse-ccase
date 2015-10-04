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

import java.util.ArrayList;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import net.sourceforge.eclipseccase.*;
import net.sourceforge.eclipseccase.ui.wizards.CheckinWizard;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;

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
            final List<IResource> selectedResources = createList((getSelectedResources()));
            List<IResource> modified = new ArrayList<IResource>();
            final ClearCaseProvider provider = new ClearCaseProvider();

            if (selectedResources.size() > 0) {

                if (ClearCasePreferences.isUseClearDlg()) {

                    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                        public void run(IProgressMonitor monitor) throws CoreException {
                            try {
                                monitor.subTask("Executing ClearCase user interface...");
                                ClearDlgHelper.checkin(selectedResources.toArray(new IResource[selectedResources.size()]));
                            } finally {
                                monitor.done();
                                updateActionEnablement();
                            }
                        }
                    };
                    executeInBackground(runnable, "Checking in ClearCase resources");
                } else {
                    CheckinWizard    wizard = new CheckinWizard(selectedResources.toArray(new IResource[modified.size()]), new IResource[0], provider);
                    WizardDialog dialog = new WizardDialog(getShell(), wizard);
                    dialog.open();
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
    private List<IResource> sortOutIdentical(List<IResource> resources, List<IResource> identical) {
        resources.removeAll(identical);
        return resources;
    }

    private List<IResource> createList(IResource [] resources){
        List<IResource> list = new ArrayList<IResource>();
        for (int i = 0; i < resources.length; i++) {
            IResource iResource = resources[i];
            list.add(iResource);

        }
        return list;
    }

}