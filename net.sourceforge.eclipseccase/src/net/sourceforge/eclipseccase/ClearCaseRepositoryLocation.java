/**
 *
 */
package net.sourceforge.eclipseccase;



import java.util.Properties;

// TODO import org.eclipse.core.runtime.IAdaptable;

/**
 * @author mikael petterson
 *
 */
public class ClearCaseRepositoryLocation implements
		IClearCaseRepositoryLocation {

	private String location;

	/**
	 * The name of the preferences node in the CVS preferences that contains the
	 * known repositories as its children.
	 */
	public static final String PREF_REPOSITORIES_NODE = "repositories"; //$NON-NLS-1$

	/*
	 * The name of the node in the default scope that has the default settings
	 * for a repository.
	 */
	@SuppressWarnings("unused")
	private static final String DEFAULT_REPOSITORY_SETTINGS_NODE = "default_repository_settings"; //$NON-NLS-1$

	// Preference keys used to persist the state of the location
	public static final String PREF_LOCATION = "location"; //$NON-NLS-1$
	public static final String PREF_SERVER_ENCODING = "encoding"; //$NON-NLS-1$

	public ClearCaseRepositoryLocation(String location){
		this.location = location;
	}

	public static ClearCaseRepositoryLocation fromProperties(Properties props){
		String location = props.getProperty("location");
		if (location == null){
    		//throw new ClearCaseException(new Status(IStatus.ERROR, SVNProviderPlugin.ID, TeamException.UNABLE, Policy.bind("SVNRepositoryLocation.hostRequired"), null)); //$NON-NLS-1$
		}

		return new ClearCaseRepositoryLocation(location);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class arg0) {

		return null;
	}

	public String getLocation(){
		return location;
	}

}
