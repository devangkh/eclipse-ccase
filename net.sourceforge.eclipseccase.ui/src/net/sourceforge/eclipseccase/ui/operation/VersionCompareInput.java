package net.sourceforge.eclipseccase.ui.operation;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.compare.CompareUI;

import net.sourceforge.eclipseccase.ui.compare.ClearCaseResourceNode;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * 
 * 
 * @author mikael petterson
 * 
 */
public class VersionCompareInput extends SaveableCompareEditorInput {

	private ITypedElement left;

	private ITypedElement right;

	private ClearCaseProvider provider;

	/**
	 * 
	 * @param configuration
	 * @param resource
	 * @param leftVersion
	 * @param rightVersion
	 * @param wp
	 * @param provider
	 */
	public VersionCompareInput(CompareConfiguration configuration, IFile resource, String leftVersion, String rightVersion, IWorkbenchPage wp, ClearCaseProvider provider, boolean differentView) {
		super(configuration, wp);

		if (provider.isCheckedOut(resource)) {
			// creates a local file and make it included into the save process
			// to
			// handle an eventual change. This is only valid for checked-out
			// file.
			left = createFileElement(resource);
		} else {
			left = new ClearCaseResourceNode(resource, leftVersion, provider);
		}
		// right is always a clearcase element
		right = new ClearCaseResourceNode(resource, rightVersion, provider, differentView);
		this.provider = provider;

		configuration.setLeftImage(CompareUI.getImage(resource));
		if (left != null) {
			configuration.setLeftLabel(leftVersion);
		}

		configuration.setRightImage(CompareUI.getImage(resource));
		if (right != null) {
			configuration.setRightLabel(rightVersion);
		}
	}

	public VersionCompareInput(CompareConfiguration configuration, IFile resource, String leftVersion, String rightVersion, IWorkbenchPage wp, ClearCaseProvider provider) {
		this(configuration, resource, leftVersion, rightVersion, wp, provider, false);
	}

	@Override
	protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return new DiffNode(null, Differencer.CHANGE, null, left, right);
	}

	@Override
	protected void fireInputChange() {
		// TODO Auto-generated method stub

	}
}
