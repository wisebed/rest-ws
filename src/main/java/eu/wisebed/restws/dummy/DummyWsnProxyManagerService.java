package eu.wisebed.restws.dummy;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractService;
import de.uniluebeck.itm.tr.util.TimedCache;
import eu.wisebed.restws.jobs.Job;
import eu.wisebed.restws.proxy.WsnProxyManagerService;
import eu.wisebed.restws.proxy.WsnProxyService;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

public class DummyWsnProxyManagerService extends AbstractService implements WsnProxyManagerService {

	private TimedCache<String, WsnProxyService> cache = new TimedCache<String, WsnProxyService>();

	public void create(String experimentWsnInstanceEndpointUrl, DateTime expiration) {
		DummyWsnProxyService wsn = new DummyWsnProxyService();
		cache.put(experimentWsnInstanceEndpointUrl, wsn);
	}

	@Override
	public WsnProxyService get(@Nonnull final String experimentWsnInstanceEndpointUrl) {
		return cache.get(experimentWsnInstanceEndpointUrl);
	}

	@Override
	public String getControllerEndpointUrl(@Nonnull final String experimentWsnInstanceEndpointUrl) {
		return null;  // TODO implement
	}

	@Override
	public Job getJob(@Nonnull final String experimentWsnInstanceEndpointUrl, @Nonnull final String requestId) {
		return null;  // TODO implement
	}

	@Override
	public EventBus getEventBus(@Nonnull final String experimentWsnInstanceEndpointUrl) {
		return null;  // TODO implement
	}

	@Override
	protected void doStart() {
		notifyStarted();
	}

	@Override
	protected void doStop() {
		notifyStopped();
	}
}
