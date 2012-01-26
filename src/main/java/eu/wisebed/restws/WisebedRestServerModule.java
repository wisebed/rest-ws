package eu.wisebed.restws;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.restws.dummy.DummyRS;
import eu.wisebed.restws.dummy.DummySessionManagement;
import eu.wisebed.restws.dummy.DummySnaa;
import eu.wisebed.restws.dummy.DummyWsnInstanceCache;
import eu.wisebed.restws.util.Log4JTypeListener;

public class WisebedRestServerModule extends AbstractModule {

	private final WisebedRestServerConfig wisebedRestServerConfig;

	public WisebedRestServerModule(final WisebedRestServerConfig wisebedRestServerConfig) {
		this.wisebedRestServerConfig = wisebedRestServerConfig;
	}

	@Override
	protected void configure() {

		bind(WisebedRestServerConfig.class).toInstance(wisebedRestServerConfig);

		bind(SNAA.class).to(DummySnaa.class);
		bind(RS.class).to(DummyRS.class);
		bind(SessionManagement.class).to(DummySessionManagement.class);
		bind(WsnInstanceCache.class).to(DummyWsnInstanceCache.class);

		bindListener(Matchers.any(), new Log4JTypeListener());

		install(new WisebedRestServerServletModule());
	}
}
