package eu.wisebed.restws;

import com.google.common.util.concurrent.TimeLimiter;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import eu.wisebed.restws.proxy.*;
import eu.wisebed.restws.util.Log4JTypeListener;

public class WisebedRestServerModule extends AbstractModule {

	private final WisebedRestServerConfig config;

	private final TimeLimiter timeLimiter;

	public WisebedRestServerModule(final WisebedRestServerConfig config, final TimeLimiter timeLimiter) {
		this.config = config;
		this.timeLimiter = timeLimiter;
	}

	@Override
	protected void configure() {

		bind(TimeLimiter.class).toInstance(timeLimiter);

		bind(WisebedRestServerConfig.class).toInstance(config);
		bind(WebServiceEndpointManager.class).to(WebServiceEndpointManagerImpl.class);
		
		bind(WsnProxyManagerService.class).to(WsnProxyManagerServiceImpl.class);

		install(new FactoryModuleBuilder().build(ControllerProxyServiceFactory.class));
		install(new FactoryModuleBuilder()
				.implement(WsnProxyService.class, WsnProxyServiceImpl.class)
				.build(WsnProxyServiceFactory.class));

		bindListener(Matchers.any(), new Log4JTypeListener());

		install(new WisebedRestServerServletModule(config));
	}
}
