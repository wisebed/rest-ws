package eu.wisebed.restws.proxy;

import eu.wisebed.restws.jobs.JobObserver;

public interface WsnProxyServiceFactory {

	WsnProxyServiceImpl create(final JobObserver jobObserver,
						   final String experimentWsnInstanceEndpointUrl);

}
