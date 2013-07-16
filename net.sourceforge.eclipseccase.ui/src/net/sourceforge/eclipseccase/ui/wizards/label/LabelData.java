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
package net.sourceforge.eclipseccase.ui.wizards.label;

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
	private String labelName;
	private String comment;
	
	
	public LabelData(IResource[] resource,ClearCaseProvider provider){
		this.resource = resource;
		this.provider = provider;
	}
	
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

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	

}
