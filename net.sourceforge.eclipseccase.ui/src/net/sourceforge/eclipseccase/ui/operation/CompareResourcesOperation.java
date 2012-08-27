package net.sourceforge.eclipseccase.ui.operation;


import net.sourceforge.eclipseccase.ClearCaseProvider;

import org.eclipse.core.resources.IResource;

import net.sourceforge.eclipseccase.ClearCasePreferences;



public class CompareResourcesOperation {
	
	
	private IResource resource;
	private String selectedVersion;
	private String comparableVersion;
	private ClearCaseProvider provider;
	
	
	public CompareResourcesOperation(IResource resource,String selectedVersion,String preVersion,ClearCaseProvider provider) {
		this.resource = resource;
		this.selectedVersion = selectedVersion;
		this.comparableVersion = preVersion;
		this.provider = provider;
		
		
	}
	
	public void compare(){
	
		if(ClearCasePreferences.isCompareExternal()){
			
			ExternalCompareOperation extCmpOp = new ExternalCompareOperation(resource,comparableVersion);
			extCmpOp.execute();
		}else{
				
			InternalCompareOperation intCmpOp = new InternalCompareOperation(resource,selectedVersion,comparableVersion,provider);
			intCmpOp.execute();
		}
	}
	
	
	

}
