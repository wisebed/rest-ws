package eu.wisebed.restws.dummy;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

import de.uniluebeck.itm.tr.util.TimedCache;
import eu.wisebed.restws.jobs.Job;
import eu.wisebed.restws.proxy.WsnProxy;
import eu.wisebed.restws.proxy.WsnProxyManager;

public class DummyWsnProxyManager implements WsnProxyManager {

	private TimedCache<String, WsnProxy> cache = new TimedCache<String, WsnProxy>();

	public void create(String experimentWsnInstanceEndpointUrl, DateTime expiration) {
		DummyWsnProxy wsn = new DummyWsnProxy();
		cache.put(experimentWsnInstanceEndpointUrl, wsn);
	}

	@Override
	public WsnProxy get(@Nonnull final String experimentWsnInstanceEndpointUrl) {
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

}
