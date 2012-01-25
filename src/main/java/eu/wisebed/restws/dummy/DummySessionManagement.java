package eu.wisebed.restws.dummy;

import eu.wisebed.api.common.KeyValuePair;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SecretReservationKey;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

public class DummySessionManagement implements SessionManagement {

	@Override
	public String areNodesAlive(@WebParam(name = "nodes", targetNamespace = "") final List<String> nodes,
								@WebParam(name = "controllerEndpointUrl", targetNamespace = "") final
								String controllerEndpointUrl) {
		return null;  // TODO implement
	}

	@Override
	public void free(
			@WebParam(name = "secretReservationKey", targetNamespace = "") final
			List<SecretReservationKey> secretReservationKey)
			throws ExperimentNotRunningException_Exception, UnknownReservationIdException_Exception {
		// TODO implement
	}

	@Override
	public void getConfiguration(
			@WebParam(name = "rsEndpointUrl", targetNamespace = "", mode = WebParam.Mode.OUT) final
			Holder<String> rsEndpointUrl,
			@WebParam(name = "snaaEndpointUrl", targetNamespace = "", mode = WebParam.Mode.OUT) final
			Holder<String> snaaEndpointUrl,
			@WebParam(name = "options", targetNamespace = "", mode = WebParam.Mode.OUT) final
			Holder<List<KeyValuePair>> options) {
		// TODO implement
	}

	@Override
	public String getInstance(
			@WebParam(name = "secretReservationKey", targetNamespace = "") final
			List<SecretReservationKey> secretReservationKey,
			@WebParam(name = "controller", targetNamespace = "") final String controller)
			throws ExperimentNotRunningException_Exception, UnknownReservationIdException_Exception {
		return null;  // TODO implement
	}

	@Override
	public String getNetwork() {
		return null;  // TODO implement
	}
}
