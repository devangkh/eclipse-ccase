/**
 * Created on Apr 10, 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Workbench>Preferences>Java>Templates.
 */
package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.compare.ResourceCompareInput;
import net.sourceforge.eclipseccase.compare.VersionExtendedFile;
import net.sourceforge.eclipseccase.compare.VersionExtendedFolder;
import net.sourceforge.eclipseccase.compare.VersionExtendedProject;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.actions.TeamAction;

/**
 *  Pulls up the clearcase version tree for the element
 */
public class CompareWithPredecessorInternalAction extends TeamAction
{

	private ResourceCompareInput fInput;

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException
	{
		IResource[] resources = getSelectedResources();
		if (resources.length != 1)
			return false;
		IResource resource = resources[0];
		ClearcaseProvider provider = ClearcaseProvider.getProvider(resource);
		if (provider == null || provider.isUnknownState(resource))
			return false;
		if (! provider.hasRemote(resource))
			return false;

		IResource predecessor = null;
		String version = provider.getPredecessorVersion(resource);
		if (version == null)
			return false;

		switch(resource.getType())
		{
			case IResource.FILE:
				predecessor = new VersionExtendedFile((IFile) resource, version);
				break;
			case IResource.FOLDER:
				predecessor = new VersionExtendedFolder((IFolder) resource, version);
				break;
			case IResource.PROJECT:
				predecessor = new VersionExtendedProject((IProject) resource, version);
				break;
			default:
				return false;
		}

		IResource[] comparables = new IResource[] {resource, predecessor};
		return fInput.setResources(comparables);
	}

	public void run(IAction action) {
		if (fInput != null)
		{
			fInput.initializeCompareConfiguration();
			CompareUI.openCompareEditorOnPage(fInput, getTargetPage());
			fInput= null;	// don't reuse this input!
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (fInput == null)
		{
			CompareConfiguration cc= new CompareConfiguration();
			// buffered merge mode: don't ask for confirmation
			// when switching between modified resources
			cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
						
			fInput= new ResourceCompareInput(cc);
		}
		super.selectionChanged(action, selection);
	}
}