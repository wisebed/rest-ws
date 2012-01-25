package eu.wisebed.restws.dummy;

import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.WsnInstanceCache;

public class DummyWsnInstanceCache implements WsnInstanceCache {
	TimedCache<String, WSN> cache = new TimedCache<String, WSN>();

	@Override
	public WSN create(String experimentUrl) {
		DummyWsn wsn = new DummyWsn();
		cache.put(experimentUrl, wsn);
		return wsn;
	}

	@Override
	public WSN get(String experimentUrl) throws Exception {
		return cache.get(experimentUrl);
	}

	@Override
	public IWsnAsyncWrapper getAyncWrapper(String experimentUrl) throws Exception {
		return new DummyWsnAsync();
	}

}
