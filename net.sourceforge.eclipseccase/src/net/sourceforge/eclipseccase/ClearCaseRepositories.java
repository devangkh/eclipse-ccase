/**
 *
 */
package net.sourceforge.eclipseccase;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseException;

import org.eclipse.core.runtime.IProgressMonitor;



/**
 * Contains all the known clearcase repositories.
 *
 * @author mikael petterson
 *
 */
public class ClearCaseRepositories {

	private Map<String,IClearCaseRepositoryLocation> repositories = new HashMap<String,IClearCaseRepositoryLocation>();


	 /**
     * Return a list of the know repository locations
     */
    public IClearCaseRepositoryLocation[] getKnownRepositories(IProgressMonitor monitor) {
//        IProgressMonitor progress = Policy.monitorFor(monitor);
//    	IEclipsePreferences prefs = (IEclipsePreferences)ClearCaseRepositoryLocation.getParentPreferences();
//		try {
//			String[] keys = prefs.childrenNames();
//	        progress.beginTask(Policy.bind("SVNRepositories.refresh"), keys.length); //$NON-NLS-1$
//			for (int i = 0; i < keys.length; i++) {
//				progress.worked(1);
//				String key = keys[i];
//				try {
//					IEclipsePreferences node = (IEclipsePreferences) prefs.node(key);
//					String location = node.get(SVNRepositoryLocation.PREF_LOCATION, null);
//					if (location != null && !exactMatchExists(location)) {
//						ISVNRepositoryLocation repos = SVNRepositoryLocation.fromString(location);
//						try {
//							repos.validateConnection(new NullProgressMonitor());
//						} catch(SVNException swallow){}
//						addToRepositoriesCache(repos);
//					} else {
//						node.removeNode();
//						prefs.flush();
//					}
//				} catch (SVNException e) {
//					// Log and continue
//					SVNProviderPlugin.log(e);
//				}
//			}
//		} catch (BackingStoreException e) {
//			// Log and continue (although all repos will be missing)
//			SVNProviderPlugin.log(SVNException.wrapException(e));
//		}
//		progress.done();
		return (IClearCaseRepositoryLocation[])repositories.values().toArray(new IClearCaseRepositoryLocation[repositories.size()]);
    }




	public void startup() {
        loadState();
    }

    public void shutdown() {
        saveState();
    }

    /**
	 * Get the repository instance which matches the given String.
	 *
	 */
    public IClearCaseRepositoryLocation getRepository(String location) throws ClearCaseException {
    	Set<String> keys = repositories.keySet();
		for(Iterator<String> iter = keys.iterator();iter.hasNext();){
			String url = (String)iter.next();
			if (url.equals(location) || location.indexOf(url + "/") != -1){
			    return (IClearCaseRepositoryLocation) repositories.get(url);
			}
		}

       //else we couldn't find it, fall through to adding new repo.

		//IClearCaseRepositoryLocation repository = ClearCaseRepositoryLocation.fromString(location);
		//addToRepositoriesCache(repository);

		return null;
    }


    /**
     * Create a repository instance from the given properties.
     * The supported properties are:
     *
     *   user The username for the connection (optional)
     *   password The password used for the connection (optional)
     *   url The url where the repository resides
     *   rootUrl The root url of the subversion repository (optional)
     *
     * The created instance is not known by the provider and it's user information is not cached.
     * The purpose of the created location is to allow connection validation before adding the
     * location to the provider.
     *
     * This method will throw a SVNException if the location for the given configuration already
     * exists.
     */
    public IClearCaseRepositoryLocation createRepository(Properties props) throws ClearCaseException {
        // Create a new repository location
    	ClearCaseRepositoryLocation location = ClearCaseRepositoryLocation.fromProperties(props);

        // Check the cache for an equivalent instance and if there is one, throw an exception
        ClearCaseRepositoryLocation existingLocation = (ClearCaseRepositoryLocation)repositories.get(location.getLocation());
        if (existingLocation != null) {
        	ClearCase.error(ClearCase.ERROR_EXCEPTION, Messages.getString("ClearCaseRepositories.alreadyExists"));
        }

        return location;
    }

    /**
     * load the state of the plugin, ie the repositories locations
     *
     */
    private void loadState() {
    	//TODO: mike 20110318 add impl.
    }

    /**
     * Save the state of the plugin, ie the repositories locations
     */
    private void saveState() {
    	//TODO: mike 20110318 add impl.
    }

}
