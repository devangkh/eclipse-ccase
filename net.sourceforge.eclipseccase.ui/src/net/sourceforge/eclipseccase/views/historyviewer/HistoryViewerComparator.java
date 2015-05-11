package net.sourceforge.eclipseccase.views.historyviewer;


import java.util.regex.Pattern;

import net.sourceforge.clearcase.ElementHistory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * 
 * @author Mattias Rundgren
 *
 */
public class HistoryViewerComparator extends ViewerComparator {
	private static final Pattern VERSION_NUMBER = Pattern.compile("\\d+");
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public HistoryViewerComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object E1, Object E2) {
		ElementHistory e1 = (ElementHistory) E1;
		ElementHistory e2 = (ElementHistory) E2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = e1.getDate().compareTo(e2.getDate());
			break;
		case 1:
			rc = e1.getuser().compareTo(e2.getuser());
			break;
		case 2:
			rc = compareVersion(e1.getVersion(), e2.getVersion());
			break;
		case 3:
			rc = e1.getLabel().compareTo(e2.getLabel());
			break;
		case 4:
			rc = e1.getComment().compareTo(e2.getComment());
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	private int compareVersion(String version1, String version2) {
		int rc = 0;
		
		if (version1 == null || version2 == null) {
			if (version1 != version2) {
				if (version1 == null) {
					rc = -1;
				}

				if (version2 == null) {
					rc = 1;
				}
			}
		} else if (version1.isEmpty() || version2.isEmpty()) {
			if (version1.length() == version2.length()) {
				// They are both empty
				return 0;
			}
			
			if (version1.isEmpty()) {
				rc = -1;
			}

			if (version2.isEmpty()) {
				rc = 1;
			}
		} else {
			// Handle platform dependent delimiter
			final String delim = version1.substring(0, 1);
			
			final int i1 = version1.lastIndexOf(delim);
			final int i2 = version2.lastIndexOf(delim);
			
			final int n1 = parseVersionNumber(version1.substring(i1 + 1));
			final int n2 = parseVersionNumber(version2.substring(i2 + 1));
			
			final String b1 = parseBranchName(version1, n1, i1);
			final String b2 = parseBranchName(version2, n2, i2);
			
			rc = b1.compareTo(b2);
			
			if (rc == 0) {
				rc = n1 - n2;
			}
		}
		
		return rc;
	}
	
	private int parseVersionNumber(String versionNumber) {
		if (VERSION_NUMBER.matcher(versionNumber).matches()) {
			return Integer.parseInt(versionNumber);
		} else {
			return -1;
		}
		
	}
	
	private String parseBranchName(String version, int number, int index) {
		if (number > -1) {
			return version.substring(0, index);
		} else {
			return version;
		}
	}
}



