package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;

import eu.wisebed.restws.jobs.JobObserver;

public interface WsnProxyServiceFactory {

	WsnProxyService create(final JobObserver jobObserver,
						   final String experimentWsnInstanceEndpointUrl,
						   final AsyncEventBus asyncEventBus);

}
