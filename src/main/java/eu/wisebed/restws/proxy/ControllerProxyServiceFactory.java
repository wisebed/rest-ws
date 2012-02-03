package eu.wisebed.restws.proxy;

import eu.wisebed.restws.jobs.JobObserver;

public interface ControllerProxyServiceFactory {

	ControllerProxyService create(String experimentWsnInstanceEndpointUrl, JobObserver jobObserver);
}
