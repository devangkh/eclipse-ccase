/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui;

import net.sourceforge.eclipseccase.autoconnect.ProjectsAutoConnect;

import org.eclipse.ui.PlatformUI;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import java.util.*;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import net.sourceforge.eclipseccase.ui.preferences.ClearCaseUIPreferences;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * UI plugin for the ClearCase plugin.
 *
 * @author Gunnar Wagenknecht
 */
public class ClearCaseUI extends AbstractUIPlugin {

	// The shared instance.
	private static ClearCaseUI plugin;

	public static final String PLUGIN_ID = "net.sourceforge.eclipseccase.ui"; //$NON-NLS-1$

	/** debug option */
	private static final String DEBUG_OPTION_DECORATION = ClearCaseUI.PLUGIN_ID + "/debug/decoration"; //$NON-NLS-1$

	/** debug option */
	private static final String DEBUG_OPTION_VIEWPRIV = ClearCaseUI.PLUGIN_ID + "/debug/viewpriv"; //$NON-NLS-1$

	/** trace text */
	public static final String VIEWPRIV = "VP_View"; //$NON-NLS-1$

	/** debug option */
	private static final String DEBUG_OPTION_PLUGIN = ClearCaseUI.PLUGIN_ID + "/debug/plugin"; //$NON-NLS-1$

	/** indicates if debugging is enabled */
	public static boolean DEBUG = false;

	/**
	 * Configures debug settings.
	 */
	static void configureDebugOptions() {
		if (ClearCaseUI.getInstance().isDebugging()) {

			if (getDebugOption(DEBUG_OPTION_DECORATION)) {
				trace("debugging " + DEBUG_OPTION_DECORATION); //$NON-NLS-1$
				ClearCaseUI.DEBUG_DECORATION = true;
			}

			if (getDebugOption(DEBUG_OPTION_VIEWPRIV)) {
				trace("debugging " + DEBUG_OPTION_VIEWPRIV); //$NON-NLS-1$
				ClearCaseUI.DEBUG_VIEWPRIV = true;
			}

			if (getDebugOption(DEBUG_OPTION_PLUGIN)) {
				trace("debugging " + DEBUG_OPTION_PLUGIN); //$NON-NLS-1$
				ClearCaseUI.DEBUG = true;
			}
		}
	}

	/**
	 * Returns the value of the specified debug option.
	 *
	 * @param optionId
	 * @return <code>true</code> if the option is enabled
	 */
	static boolean getDebugOption(String optionId) {
		String option = Platform.getDebugOption(optionId);
		return option != null ? Boolean.valueOf(option).booleanValue() : false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (DEBUG) {
			trace("stop"); //$NON-NLS-1$
		}

		PlatformUI.getWorkbench().removeWindowListener(partListener);
	}

	/**
	 * Prints out a trace message.
	 *
	 * @param message
	 */
	public static void trace(String message) {
		System.out.println("**ClearCaseUI** " + message); //$NON-NLS-1$
	}

	/**
	 * Prints out a trace message.
	 *
	 * @param traceId
	 * @param message
	 */
	public static void trace(String traceId, String message) {
		trace("[" + traceId + "] " + message); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The constructor.
	 *
	 * @param descriptor
	 */
	public ClearCaseUI() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		configureDebugOptions();

		ClearCasePlugin.getDefault().setClearCaseModificationHandler(new ClearCaseUIModificationHandler());

		PlatformUI.getWorkbench().addWindowListener(partListener);

		// Running clearcase auto-connect.
		if (ClearCasePreferences.isAutoConnectEnabled()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ProjectsAutoConnect.doAutoConnect();
				}
			});
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the default instance
	 */
	public static ClearCaseUI getInstance() {
		return plugin;
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the
	 * identical content is presented in more than one editor, only one of those
	 * editor parts is part of the result.
	 *
	 * @return an array of all dirty editor parts.
	 */
	public static IEditorPart[] getDirtyEditors() {
		Set inputs = new HashSet();
		List result = new ArrayList(0);
		IWorkbench workbench = getInstance().getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorPart[] editors = pages[x].getDirtyEditors();
				for (int z = 0; z < editors.length; z++) {
					IEditorPart ep = editors[z];
					IEditorInput input = ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
	}

	/**
	 * Returns the preference value for
	 * <code>TEXT_PREFIX_VIEW_PRIVATE_ELEMENTS</code>.
	 *
	 * @return the preference value
	 */
	public static String getTextPrefixNew() {
		return getInstance().getPluginPreferences().getString(ClearCaseUIPreferences.TEXT_PREFIX_VIEW_PRIVATE_ELEMENTS);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_DIRTY_ELEMENTS</code>.
	 *
	 * @return the preference value
	 */
	public static String getTextPrefixDirty() {
		return getInstance().getPluginPreferences().getString(ClearCaseUIPreferences.TEXT_PREFIX_DIRTY_ELEMENTS);
	}

	/**
	 * Returns the preference value for
	 * <code>TEXT_PREFIX_UNKNOWN_ELEMENTS</code>.
	 *
	 * @return the preference value
	 */
	public static String getTextPrefixUnknown() {
		return getInstance().getPluginPreferences().getString(ClearCaseUIPreferences.TEXT_PREFIX_UNKNOWN_ELEMENTS);
	}

	/**
	 * Returns the preference value for
	 * <code>TEXT_PREFIX_HIJACKED_ELEMENTS</code>.
	 *
	 * @return the preference value
	 */
	public static String getTextPrefixHijacked() {
		return getInstance().getPluginPreferences().getString(ClearCaseUIPreferences.TEXT_PREFIX_HIJACKED_ELEMENTS);
	}

	/**
	 * Returns the preference value for <code>TEXT_PREFIX_EDITED_ELEMENTS</code>
	 * .
	 *
	 * @return the preference value
	 */
	public static String getTextPrefixEdited() {
		return getInstance().getPluginPreferences().getString(ClearCaseUIPreferences.TEXT_PREFIX_EDITED_ELEMENTS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse
	 * .jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);

		// objects
		createImageDescriptor(reg, ClearCaseImages.IMG_QUESTIONABLE_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_CHECKEDOUT_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_EDITED_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_UNKNOWN_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_DERIVEDOBJECT_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_LINK_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_LINK_WARNING_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_HIJACKED_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_DYNAMIC_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_SNAPSHOT_OVR);
		createImageDescriptor(reg, ClearCaseImages.IMG_REFRESH);
		createImageDescriptor(reg, ClearCaseImages.IMG_REFRESH_DISABLED);
		createImageDescriptor(reg, ClearCaseImages.IMG_ELEMENT_BG);
		createImageDescriptor(reg, ClearCaseImages.IMG_ELEM_CO);
		createImageDescriptor(reg, ClearCaseImages.IMG_ELEM_HJ);
		createImageDescriptor(reg, ClearCaseImages.IMG_ELEM_UNK);
		createImageDescriptor(reg, ClearCaseImages.IMG_CHECKED);
		createImageDescriptor(reg, ClearCaseImages.IMG_UNCHECKED);
	}

	private static void createImageDescriptor(ImageRegistry reg, String id) {
		ImageDescriptor desc = imageDescriptorFromPlugin(ClearCaseUI.PLUGIN_ID, ClearCaseImages.ICON_PATH + id);
		reg.put(id, null != desc ? desc : ImageDescriptor.getMissingImageDescriptor());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	@Override
	protected ImageRegistry createImageRegistry() {
		// to overcome SWT issues we create this inside the UI thread
		final ImageRegistry[] imageRegistries = new ImageRegistry[1];
		getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				imageRegistries[0] = new ImageRegistry(getWorkbench().getDisplay());
			}
		});
		return imageRegistries[0];
	}

	/** the listener for opened editors */
	private PartListener partListener = new PartListener();

	/** indicates if additional debug output should be printed */
	public static boolean DEBUG_DECORATION = false;
	public static boolean DEBUG_VIEWPRIV = false;

	/**
	 * Returns the workbench display
	 *
	 * @return the workbench display
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

}