package eu.wisebed.restws.ws;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.CommandLineOptions;
import eu.wisebed.restws.dummy.DummyRS;
import eu.wisebed.restws.dummy.DummySessionManagement;
import eu.wisebed.restws.dummy.DummySnaa;
import eu.wisebed.restws.dummy.DummyWsn;
import eu.wisebed.restws.util.Log4JTypeListener;

public class WebSocketServerModule extends AbstractModule {

	private CommandLineOptions commandLineOptions;

	public WebSocketServerModule(final CommandLineOptions commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	}

	@Override
	protected void configure() {
		
		bind(SNAA.class).to(DummySnaa.class);
		bind(RS.class).to(DummyRS.class);
		bind(SessionManagement.class).to(DummySessionManagement.class);
		bind(WSN.class).to(DummyWsn.class);
		
		bind(CommandLineOptions.class).toInstance(commandLineOptions);

		bindListener(Matchers.any(), new Log4JTypeListener());
	}
}
