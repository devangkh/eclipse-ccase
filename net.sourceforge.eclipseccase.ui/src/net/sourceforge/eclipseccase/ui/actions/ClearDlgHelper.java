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

package net.sourceforge.eclipseccase.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.clearcase.simple.ClearDlg;
import net.sourceforge.eclipseccase.ClearcasePlugin;
import net.sourceforge.eclipseccase.ClearcaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;

/**
 * This is a helper class for accesing the "ClearDlg" executable.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class ClearDlgHelper {

	/**
	 * "cleardlg" command line argument for preserving the time;
	 */
	private static final String PTIME = "/ptime"; //$NON-NLS-1$

	private static final String UNCHECKOUT = "/uncheckout"; //$NON-NLS-1$

	private static final String CHECKOUT = "/checkout"; //$NON-NLS-1$

	private static final String CHECKIN = "/checkin"; //$NON-NLS-1$

	private static final String ADDTOSRC = "/addtosrc"; //$NON-NLS-1$

	/**
	 * Adds the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void add(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(ADDTOSRC, resources);
	}

	/**
	 * Checkin the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void checkin(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(CHECKIN, resources);
	}

	/**
	 * Checkout the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void checkout(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(CHECKOUT, resources);
	}

	/**
	 * Uncheckout the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void uncheckout(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(UNCHECKOUT, resources);
	}

	/**
	 * Adds the specified resources.
	 * 
	 * @param operation
	 * @param resources
	 * @throws TeamException
	 */
	public static void perform(String operation, IResource[] resources) throws TeamException {
		// prepare command
		List command = new ArrayList(1 + resources.length);
		command.add(operation);

		// append files
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			command.add(resource.getLocation().toOSString());
		}

		// preserver time if enabled and allowed
		if (ClearcasePlugin.isPreserveTimes() && (CHECKOUT.equalsIgnoreCase(operation) || CHECKIN.equalsIgnoreCase(operation) || ADDTOSRC.equalsIgnoreCase(operation)))
			command.add(PTIME);

		// execute cleardlg
		ClearDlg clearDlg = new ClearDlg((String[]) command.toArray(new String[command.size()]));
		try {
			int success = clearDlg.execute();
			if (0 != success && success != resources.length)
				throw new TeamException("Execution of cleardlg " + operation + " failed (process returned: " + clearDlg.getExitValue() + ")!");
		} catch (IOException e) {
			throw new TeamException("Execution of cleardlg " + operation + " failed: " + e.getMessage(), e);
		}

		// refresh resources
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);

			// refresh resource
			provider.refresh(resource);

			// also invalidate state of parent container
			// (some operations my checkout parent)
			provider.refresh(resource.getParent());
		}
	}

	/**
	 * No instance necessary.
	 */
	private ClearDlgHelper() {
		super();
	}

}