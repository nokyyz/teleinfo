package org.openhab.binding.teleinfo.reader.context;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Service provider class for ApplicationContext. Sub-classes of ApplicationContextProvider
 * provide an implementation of ApplicationContext and associated classes.
 * Applications do not normally use this class. See provider() for how providers
 * are found and loaded.
 */
public abstract class ApplicationContextProvider {

	private static final Object lock = new Object();
    private static ApplicationContextProvider provider = null;
    
	protected ApplicationContextProvider() {

	}

	/**
	 * Creates a ApplicationContext from this provider
	 * @return
	 * @throws IOException
	 */
	public abstract ApplicationContext createApplicationContext() throws IOException;
	

    /**
     * Returns the system wide default ApplicationContextProvider for this invocation of
     * the Java virtual machine.
     *
     * <p> The first invocation of this method locates the default provider
     * object as follows: </p>
     *
     * <ol>
     *   <li><p> If the system property
     *   <tt>org.openhab.binding.teleinfo.reader.context.ApplicationContextProvider</tt> is defined then it is
     *   taken to be the fully-qualified name of a concrete provider class.
     *   The class is loaded and instantiated; if this process fails then an
     *   unspecified unchecked error or exception is thrown.  </p></li>
     *
     *   <li><p> If a provider class has been installed in a jar file that is
     *   visible to the system class loader, and that jar file contains a
     *   provider-configuration file named
     *   <tt>org.openhab.binding.teleinfo.reader.context.ApplicationContextProvider</tt> in the resource
     *   directory <tt>META-INF/services</tt>, then the first class name
     *   specified in that file is taken.  The class is loaded and
     *   instantiated; if this process fails then an unspecified unchecked error or exception is
     *   thrown.  </p></li>
     *
     *   <li><p> Finally, if no provider has been specified by any of the above
     *   means then the system-default provider class is instantiated and the
     *   result is returned.  </p></li>
     * </ol>
     *
     * <p> Subsequent invocations of this method return the provider that was
     * returned by the first invocation.  </p>
     *
     * @return  The system-wide default ApplicationContextProvider
     */
    public static ApplicationContextProvider getProvider() {
        synchronized (lock) {
            if (provider != null) {
                return provider;
            }

            return (ApplicationContextProvider) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            if (loadProviderFromProperty()) {
                            	return provider;
                            }
                                
                            if (loadProviderAsService()) {
                            	return provider;
                            }
                                
                            return null;
                        }
                    });
        }
    }
	
    private static boolean loadProviderFromProperty() {
        String cn = System.getProperty("org.openhab.binding.teleinfo.reader.context.ApplicationContextProvider");
        if (cn == null) {
            return false;
        }
        try {
            Class providerClass = Class.forName(cn, true, ClassLoader.getSystemClassLoader());
            provider = (ApplicationContextProvider) providerClass.newInstance();
            return true;
        } catch (Exception x) {
            throw new ServiceConfigurationError("An error occurred during ApplicationContextProvider instanciation", x);
        }
    }

    private static boolean loadProviderAsService() {
    	ServiceLoader<ApplicationContextProvider> providerLoader = ServiceLoader.load(ApplicationContextProvider.class);
    	
        Iterator<ApplicationContextProvider> it = providerLoader.iterator();
        for (;;) {
            try {
                if (!it.hasNext()) {
                    return false;                	
                }
                provider = (ApplicationContextProvider) it.next();
                return true;
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() instanceof SecurityException) {
                    // Ignore the security exception, try the next provider
                    continue;
                }
                throw sce;
            }
        }
    }
}
