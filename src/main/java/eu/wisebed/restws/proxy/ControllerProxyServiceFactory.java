package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;

public interface ControllerProxyServiceFactory {

	ControllerProxyService create(String experimentWsnInstanceEndpointUrl, AsyncJobObserver asyncJobObserver,
								  AsyncEventBus asyncEventBus);
}
