package eu.wisebed.restws;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import eu.wisebed.restws.proxy.*;
import eu.wisebed.restws.util.Log4JTypeListener;

public class WisebedRestServerModule extends AbstractModule {

	private final WisebedRestServerConfig config;

	public WisebedRestServerModule(final WisebedRestServerConfig config) {
		this.config = config;
	}

	@Override
	protected void configure() {

		bind(WisebedRestServerConfig.class).toInstance(config);
		bind(WebServiceEndpointManager.class).to(WebServiceEndpointManagerImpl.class);
		
		bind(WsnProxyManagerService.class).to(WsnProxyManagerServiceImpl.class);

		install(new FactoryModuleBuilder().build(ControllerProxyServiceFactory.class));
		install(new FactoryModuleBuilder().build(WsnProxyServiceFactory.class));

		bindListener(Matchers.any(), new Log4JTypeListener());

		install(new WisebedRestServerServletModule());
	}
}
