package net.sourceforge.eclipseccase.autoconnect;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * An autochecking thread.
 * When this thread starts it will check a loaded projects (not with a ProjectLoader!)
 * and it tries to associate them with ClearCase plugin (if the auto-association flag is checked).
 * @author Z218543
 *
 */
public class AutoCheckingThread extends Thread {

	@Override
	public void run() {

		boolean allProjectsLoaded = true;
		do {

			// The list of projects.
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			if (projects.length == 0)
			{
				continue;
			}

			for (int i=0; i < projects.length; i++) {

				if (projects[i].isOpen())
				{
					allProjectsLoaded &= projects[i].isAccessible();
				}
			}

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		} while (!allProjectsLoaded);

		ProjectsAutoConnect.associateListeners();
		ProjectsAutoConnect.connectAllProjects();
	}



}
