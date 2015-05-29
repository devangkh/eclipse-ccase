package net.sourceforge.eclipseccase.ui.actions;

import net.sourceforge.clearcase.ClearCase;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.eclipseccase.ClearCasePreferences;
import net.sourceforge.eclipseccase.views.historyviewer.JFHistoryViewer;
import net.sourceforge.eclipseccase.ClearCasePlugin;
import java.util.Vector;
import net.sourceforge.clearcase.*;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Pulls up the clearcase history
 */
public class HistoryAction extends ClearCaseWorkspaceAction {

	String recordLimit;
	IResource[] resources = null;
	String fileVersion = null;
	IResource forceResource = null;
	
	public void setResource(IResource Resource) {
		this.forceResource = Resource;
	}
	
	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	private JFHistoryViewer view = null;

	/**
	 * {@inheritDoc

	 */
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
		}
		return true;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void execute(IAction action) {
		
		if(forceResource != null)
		{
			resources = new IResource[1];
			resources[0] = forceResource;
		}
		else
		{
			resources = getSelectedResources();
		}
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
			if (provider == null) {
				return;
			}
		}
		try {
			view = (JFHistoryViewer) PlatformUI.getWorkbench().getActiveWorkbenchWindow().
					getActivePage().showView("net.sourceforge.eclipseccase.views.HistoryViewer.JFHistoryViewer");
		} catch (Exception e) {
			e.printStackTrace();
		}

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					if (resources != null && resources.length > 0) {
						IResource resource = resources[0];
						String path;
						
						if(fileVersion != null)
						{	
							path = fileVersion;
						}
						else
						{
							path = resource.getLocation().toOSString();
						}
						
												
						recordLimit = ClearCasePreferences.getHistoryRecord();
						ClearCaseInterface cci = ClearCasePlugin.getEngine();
						Vector<ElementHistory> result;
													
							HashMap<Integer, String> args = new HashMap<Integer, String>();
							if(recordLimit.startsWith(":")){
								//3. lshistory -long -since <date> <file>
								recordLimit = recordLimit.replace(":", "");
								args.put(Integer.valueOf(ClearCase.SINCE), recordLimit);
								result = cci.getElementHistory(path, ClearCase.SINCE, args);
							}else if(recordLimit.endsWith(":")){
								//2. lshistory -long -last # <file>
								recordLimit = recordLimit.replace(":", "");
								args.put(Integer.valueOf(ClearCase.LAST), recordLimit);
								result = cci.getElementHistory(path, ClearCase.LAST, args);
							}else if(recordLimit.indexOf(":") != -1){
								//4. lshistory -long -last # -since <date> <file>
								String [] types = recordLimit.split(":");		
								args.put(Integer.valueOf(ClearCase.LAST), types[0]);
								args.put(Integer.valueOf(ClearCase.SINCE), types[1]);
								result = cci.getElementHistory(path, ClearCase.LAST|ClearCase.SINCE, args);
							}else{
								//default
								//1. lshistory -long <file> (current version and should be the default in upgraded version)
								result = cci.getElementHistory(path,0,new HashMap<Integer, String>());
							}
							view.setHistoryInformation(resource, result);

					}
				} catch (Exception e) {
					System.out.println(e);
				} finally {
					monitor.done();
				}
			}
		};

		executeInBackground(runnable, "History");

	}

}
