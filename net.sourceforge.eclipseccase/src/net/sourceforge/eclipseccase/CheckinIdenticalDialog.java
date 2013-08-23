package net.sourceforge.eclipseccase;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class CheckinIdenticalDialog implements Runnable {

	private IResource []identical;
	private Object [] selected = null;

	public CheckinIdenticalDialog(IResource [] resource) {
		this.identical = resource;
	}

	@Override
	public void run() {
		ElementListSelectionDialog dialog = 
				  new ElementListSelectionDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), new LabelProvider());
				dialog.setMultipleSelection(true);
				dialog.setElements(identical);
				dialog.setTitle("Select identical resources to checkin.");
				dialog.setMessage("You have unmodified changes. Do you want to check-in?");
				dialog.open();
				dialog.getResult();
				selected = dialog.getResult();
	}
	

	public IResource [] getResult() {
		List<IResource> resource = new ArrayList<IResource>();
		for (int i = 0; i < selected.length; i++) {
			IResource res = (IResource)selected[i];
			resource.add(res);
			
		}
		return resource.toArray(new IResource [resource.size()]);
	}
	
}
