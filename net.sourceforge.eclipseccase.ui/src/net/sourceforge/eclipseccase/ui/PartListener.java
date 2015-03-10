/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.*;

/**
 * A part listener listens to part changes and refreshes a resource state if an
 * editor is opened.
 *
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
class PartListener implements IPartListener2, IWindowListener {

	/**
	 * Refreshes the resource covered by the specified editor
	 *
	 * @param part
	 */
	private void refreshResource(IWorkbenchPart part) {
		if (null != part && part instanceof IEditorPart) {
			IResource resource = getResource(((IEditorPart) part).getEditorInput());
			refreshResource(resource);
		}
	}

	/**
	 * Refreshes the specified resource
	 *
	 * @param resource
	 */
	private void refreshResource(IResource resource) {
		if (null != resource) {
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (null != provider) {
				provider.refresh(resource);
			}
		}
	}

	/**
	 * Returns the resource for the specified input
	 *
	 * @param input
	 * @return
	 */
	private IResource getResource(IEditorInput input) {
		if (input instanceof IFileEditorInput)
			return ((IFileEditorInput) input).getFile();
		return (IResource) input.getAdapter(IResource.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference ref) {
		// TODO: refresh necessary? what for?
		// if this gets enabled, the Action enablement mechanism does not
		// work any more, because at the time the isEnabled() gets called
		// the element state is not yet known.
		// refreshResource(ref.getPart(false));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partDeactivated(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partOpened(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partHidden(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partVisible(IWorkbenchPartReference ref) {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	public void partInputChanged(IWorkbenchPartReference ref) {
		refreshResource(ref.getPart(false));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		window.getPartService().addPartListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow
	 * )
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow
	 * )
	 */
	public void windowOpened(IWorkbenchWindow window) {
		// nothing
	}

}
