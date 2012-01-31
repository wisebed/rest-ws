package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;

public interface WsnProxyServiceFactory {

	WsnProxyService create(final AsyncJobObserver asyncJobObserver,
						   final String experimentWsnInstanceEndpointUrl,
						   final AsyncEventBus asyncEventBus);

}
