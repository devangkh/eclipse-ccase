/*******************************************************************************
 * Copyright (c) 2013 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     eraonel - inital API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.eclipseccase.ui.wizards;

import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.resources.IResource;

/**
 * 
 * Class holds data used by LabelWizard.
 * @author eraonel
 *
 */
public class LabelData {
	
	private IResource [] resource;
	private ClearCaseProvider provider;
	
	
	public IResource[] getResource() {
		return resource;
	}
	public void setResource(IResource[] resource) {
		this.resource = resource;
	}
	public ClearCaseProvider getProvider() {
		return provider;
	}
	public void setProvider(ClearCaseProvider provider) {
		this.provider = provider;
	}
	
	

}
