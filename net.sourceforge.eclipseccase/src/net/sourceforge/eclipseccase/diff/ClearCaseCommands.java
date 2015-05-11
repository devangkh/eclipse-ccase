package net.sourceforge.eclipseccase.diff;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
// TODO import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.eclipseccase.ClearCasePlugin;

/**
 * Class implements the commands for ClearCase to perform compare and merge
 * operations.
 *
 * @author mikael petterson
 *
 */
public class ClearCaseCommands extends AbstractExternalToolCommands {

	@Override
	public void twoWayDiff(String file1, String file2) {
		ClearCasePlugin.getEngine().compareWithVersion(file1, file2);

	}

	@Override
	public void threeWayDiff(String file1, String file2, String base) {
		// TODO Auto-generated method stub

	}

	@Override
	public Status twoWayMerge(String file1, String file2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status threeWayMerge(String file1, String file2, String base) {
		ClearCaseElementState state = ClearCasePlugin.getEngine().merge(file2,
					new String[] { file1 }, base, ClearCase.GRAPHICAL);

		if (state.isMerged()) {
			return new Status(IStatus.OK, ClearCasePlugin.PLUGIN_ID,
					Messages.getString("ClearCaseCommands.threeWayMerge.ok"));
		}

		return new Status(IStatus.ERROR,ClearCasePlugin.PLUGIN_ID,Messages.getString("ClearCaseCommands.threeWayMerge.fail.unknown"));

	}

}
