package eu.wisebed.restws.dummy;

import de.uniluebeck.itm.tr.util.TimedCache;
import eu.wisebed.restws.proxy.WsnProxy;
import eu.wisebed.restws.proxy.WsnProxyManager;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;

public class DummyWsnProxyManager implements WsnProxyManager {

	private TimedCache<String, WsnProxy> cache = new TimedCache<String, WsnProxy>();

	public WsnProxy create(String experimentWsnInstanceEndpointUrl, DateTime expiration) {
		DummyWsnProxy wsn = new DummyWsnProxy();
		cache.put(experimentWsnInstanceEndpointUrl, wsn);
		return wsn;
	}

	@Override
	public WsnProxy get(@Nonnull final String experimentWsnInstanceEndpointUrl) throws Exception {
		return cache.get(experimentWsnInstanceEndpointUrl);
	}

}
