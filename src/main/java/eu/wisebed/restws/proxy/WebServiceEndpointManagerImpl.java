package eu.wisebed.restws.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.wisebed.api.common.KeyValuePair;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;
import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.dto.TestbedMap;
import eu.wisebed.restws.exceptions.UnknownTestbedIdException;
import eu.wisebed.restws.util.RSServiceHelper;
import eu.wisebed.restws.util.SNAAServiceHelper;
import eu.wisebed.restws.util.WSNServiceHelper;

import javax.xml.ws.Holder;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class WebServiceEndpointManagerImpl implements WebServiceEndpointManager {

	@Inject
	private WisebedRestServerConfig config;

	private Map<String, RS> rsMap = newHashMap();

	private Map<String, SessionManagement> smMap = newHashMap();

	private Map<String, SNAA> snaaMap = newHashMap();


	@Override
	public synchronized SNAA getSnaaEndpoint(final String testbedId) throws UnknownTestbedIdException {

		checkNotNull(testbedId);

		SNAA snaa = snaaMap.get(testbedId);
		if (snaa != null) {
			return snaa;
		}

		fillMaps(testbedId);

		return snaaMap.get(testbedId);
	}

	@Override
	public synchronized RS getRsEndpoint(final String testbedId) throws UnknownTestbedIdException {

		checkNotNull(testbedId);
		
		RS rs = rsMap.get(testbedId);
		if (rs != null) {
			return rs;
		}

		fillMaps(testbedId);

		return rsMap.get(testbedId);
	}

	@Override
	public synchronized SessionManagement getSmEndpoint(final String testbedId) throws UnknownTestbedIdException {

		checkNotNull(testbedId);

		SessionManagement sm = smMap.get(testbedId);
		if (sm != null) {
			return sm;
		}

		fillMaps(testbedId);

		return smMap.get(testbedId);
	}

	private void fillMaps(final String testbedId) throws UnknownTestbedIdException {
		
		final TestbedMap.Testbed testbed = config.testbedMap.testbedMap.get(testbedId);
		final SessionManagement sm;

		if (testbed == null) {
			throw new UnknownTestbedIdException(testbedId);
		}

		sm = WSNServiceHelper.getSessionManagementService(testbed.sessionManagementEndpointUrl);

		final Holder<String> rsEndpointUrlHolder = new Holder<String>();
		final Holder<String> snaaEndpointUrlHolder = new Holder<String>();
		final Holder<List<KeyValuePair>> optionsHolder = new Holder<List<KeyValuePair>>();

		sm.getConfiguration(rsEndpointUrlHolder, snaaEndpointUrlHolder, optionsHolder);

		smMap.put(testbedId, sm);
		rsMap.put(testbedId, RSServiceHelper.getRSService(rsEndpointUrlHolder.value));
		snaaMap.put(testbedId, SNAAServiceHelper.getSNAAService(snaaEndpointUrlHolder.value));
	}
}
