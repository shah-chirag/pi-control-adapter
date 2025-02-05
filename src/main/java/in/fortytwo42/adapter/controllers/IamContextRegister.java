/**
 * 
 */

package in.fortytwo42.adapter.controllers;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;

import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;

import in.fortytwo42.adapter.util.GraphiteReporterUtil;

public class IamContextRegister extends ResourceConfig {

    public IamContextRegister() {
        register(new InstrumentedResourceMethodApplicationListener(GraphiteReporterUtil.getInstance().getMetricRegistry()));
        property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, Boolean.TRUE);
        packages("in.fortytwo42.adapter.filter,in.fortytwo42.adapter.webservice,com.fasterxml.jackson.jaxrs.json");
    }
}

