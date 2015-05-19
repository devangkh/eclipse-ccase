package net.sourceforge.eclipseccase.autoconnect;
import org.eclipse.core.internal.resources.Project;

import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.internal.events.ILifecycleListener;

@SuppressWarnings("restriction")
/**
 * This class is a listener for OPEN and CREATE events.
 * When any of these events occurs it will run the single project association with ClearCase.
 * @author Z218543
 *
 */
public class ProjectOpenCreateListener implements ILifecycleListener {

	public void handleEvent(LifecycleEvent event) throws CoreException {

		if (event == null) {
			return;
		}

		if (event.resource != null)
		{
			if (event.resource instanceof Project)
			{
				if (event.kind == LifecycleEvent.PRE_PROJECT_OPEN || event.kind == LifecycleEvent.PRE_PROJECT_CREATE)
				{
					ProjectsAutoConnect.connectSingleProject((Project) event.resource);
				}
			}
		}


	}

}

