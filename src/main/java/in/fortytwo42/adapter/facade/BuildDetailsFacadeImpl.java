
package  in.fortytwo42.adapter.facade;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.fortytwo42.adapter.transferobj.BuildDetailsTO;

// TODO: Auto-generated Javadoc
/**
 * The Class BuildDetailsFacadeImpl.
 */
public class BuildDetailsFacadeImpl implements BuildDetailsFacadeIntf {

    /** The build details facade impl log. */
    private String BUILD_DETAILS_FACADE_IMPL_LOG = "<<<<< BuildDetailsFacadeImpl";

    private static Logger logger= LogManager.getLogger(BuildDetailsFacadeImpl.class);

    /**
     * The Class InstanceHolder.
     */
    private static final class InstanceHolder {
        
        /** The Constant INSTANCE. */
        private static final BuildDetailsFacadeImpl INSTANCE = new BuildDetailsFacadeImpl();

        /**
         * Instantiates a new instance holder.
         */
        private InstanceHolder() {
            super();
        }
    }

    /**
     * Gets the single instance of BuildDetailsFacadeImpl.
     *
     * @return single instance of BuildDetailsFacadeImpl
     */
    public static BuildDetailsFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Gets the builds the details.
     *
     * @return the builds the details
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public List<BuildDetailsTO> getBuildDetails() throws IOException{
        logger.log(Level.DEBUG, BUILD_DETAILS_FACADE_IMPL_LOG + " getBuildDetails : start");
        List<BuildDetailsTO> buildDetails = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            BuildDetailsTO buildDetail = new BuildDetailsTO();
            URL manifestUrl = resources.nextElement();
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes mainAttributes = manifest.getMainAttributes();
            String implementationTitle = mainAttributes.getValue("Implementation-Title");
            buildDetail.setTitle(implementationTitle);
            String implementationVersion = mainAttributes.getValue("Implementation-Version");
            buildDetail.setVersion(implementationVersion);
            String buildTime = mainAttributes.getValue("Build-Time");
            buildDetail.setBuildTime(buildTime);
            buildDetails.add(buildDetail);
        }
        logger.log(Level.DEBUG, BUILD_DETAILS_FACADE_IMPL_LOG + " getBuildDetails : start");
        return buildDetails;
    }

}
