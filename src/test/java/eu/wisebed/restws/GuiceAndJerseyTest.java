package eu.wisebed.restws;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import eu.wisebed.restws.resources.RootResource;

/**
 * This class must not be modified.
 */
public abstract class GuiceAndJerseyTest extends JerseyTest {

	@Override
	protected AppDescriptor configure() {

		Injector injector = Guice.createInjector(new WisebedRestServerModule());
		injector.injectMembers(this);

		setTestContainerFactory(new GuiceInMemoryTestContainerFactory(injector));

		return new LowLevelAppDescriptor.Builder(RootResource.class.getPackage().getName()).build();
	}
}