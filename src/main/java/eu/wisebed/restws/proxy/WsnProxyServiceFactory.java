package eu.wisebed.restws.proxy;

import eu.wisebed.restws.jobs.JobObserver;

public interface WsnProxyServiceFactory {

	WsnProxyService create(final JobObserver jobObserver,
						   final String experimentWsnInstanceEndpointUrl);

}
