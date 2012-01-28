package eu.wisebed.restws;

import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.wsn.WSN;

public interface WsnInstanceCache {

	public WSN create(String experimentUrl);

	public WSN getOrCreate(String experimentUrl) throws Exception;

	public IWsnAsyncWrapper getAsyncWrapper(String experimentUrl) throws Exception;

}
