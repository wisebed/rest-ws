package eu.wisebed.restws;

import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.wsn.WSN;

public interface WsnInstanceCache {

	public WSN create(String experimentUrl);

	public WSN get(String experimentUrl) throws Exception;

	public IWsnAsyncWrapper getAyncWrapper(String experimentUrl) throws Exception;

}
