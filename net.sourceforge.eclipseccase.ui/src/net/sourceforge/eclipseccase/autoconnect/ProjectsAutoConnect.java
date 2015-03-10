package net.sourceforge.eclipseccase.autoconnect;

import net.sourceforge.eclipseccase.ui.actions.AssociateAllProjectsAction;

public class ProjectsAutoConnect {

	public static void doAutoConnect() {

		AssociateAllProjectsAction action = new AssociateAllProjectsAction();
		action.forceToExecute();
	}
}
