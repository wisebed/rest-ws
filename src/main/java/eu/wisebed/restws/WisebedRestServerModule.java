package eu.wisebed.restws;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.restws.dummy.*;
import eu.wisebed.restws.proxy.ControllerProxyServiceFactory;
import eu.wisebed.restws.proxy.WsnProxyManager;
import eu.wisebed.restws.proxy.WsnProxyManagerImpl;
import eu.wisebed.restws.proxy.WsnProxyServiceFactory;
import eu.wisebed.restws.util.Log4JTypeListener;

public class WisebedRestServerModule extends AbstractModule {

	private final WisebedRestServerConfig wisebedRestServerConfig;

	private final SNAA snaa;

	private final RS rs;

	private final SessionManagement sm;

	public WisebedRestServerModule(final WisebedRestServerConfig wisebedRestServerConfig, final SNAA snaa, final RS rs,
								   final SessionManagement sm) {

		this.wisebedRestServerConfig = wisebedRestServerConfig;
		this.snaa = snaa;
		this.rs = rs;
		this.sm = sm;
	}

	@Override
	protected void configure() {

		bind(WisebedRestServerConfig.class).toInstance(wisebedRestServerConfig);

		bind(SNAA.class).toInstance(snaa);
		bind(RS.class).toInstance(rs);
		bind(SessionManagement.class).toInstance(sm);
		
		bind(WsnProxyManager.class).to(WsnProxyManagerImpl.class);

		install(new FactoryModuleBuilder().build(ControllerProxyServiceFactory.class));
		install(new FactoryModuleBuilder().build(WsnProxyServiceFactory.class));

		bindListener(Matchers.any(), new Log4JTypeListener());

		install(new WisebedRestServerServletModule());
	}
}
