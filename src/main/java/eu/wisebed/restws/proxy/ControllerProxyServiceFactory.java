package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;

import eu.wisebed.restws.jobs.JobObserver;

public interface ControllerProxyServiceFactory {

	ControllerProxyService create(String experimentWsnInstanceEndpointUrl, JobObserver jobObserver,
								  AsyncEventBus asyncEventBus);
}
