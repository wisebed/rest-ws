package eu.wisebed.restws.dummy;

import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.WsnInstanceCache;

public class DummyWsnInstanceCache implements WsnInstanceCache {

	private TimedCache<String, WSN> cache = new TimedCache<String, WSN>();

	public WSN create(String experimentUrl) {
		DummyWsn wsn = new DummyWsn();
		cache.put(experimentUrl, wsn);
		return wsn;
	}

	public WSN getOrCreate(String experimentUrl) throws Exception {
		WSN experiment = cache.get(experimentUrl);
		if (experiment == null) {
			return create(experimentUrl);
		}
		return experiment;
	}

	public IWsnAsyncWrapper getAsyncWrapper(String experimentUrl) throws Exception {
		return new DummyWsnAsync();
	}

}
