package eu.wisebed.restws.dummy;

import java.util.List;

import javax.jws.WebParam;
import javax.xml.ws.Holder;

import eu.wisebed.api.common.KeyValuePair;
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception;
import eu.wisebed.api.sm.SecretReservationKey;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.sm.UnknownReservationIdException_Exception;

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
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<wiseml version=\"1.0\" xmlns=\"http://wisebed.eu/ns/wiseml/1.0\">\n"
				+ "    <setup>\n"
				+ "        <origin>\n"
				+ "            <x>53.833836</x>\n"
				+ "            <y>10.704606</y>\n"
				+ "            <z>33.0</z>\n"
				+ "            <phi>-145.0</phi>\n"
				+ "            <theta>0.0</theta>\n"
				+ "        </origin>\n"
				+ "        <coordinateType>geographic</coordinateType>\n"
				+ "        <description>This is the description WiseML file of the UzL testbed in Luebeck, Germany containing 54 iSense, 54 telosB and 54 Pacemate sensor nodes.</description>\n"
				+ "        <node id=\"urn:wisebed:uzl1:0x221e\">\n"
				+ "            <position>\n"
				+ "                <x>10.0</x>\n"
				+ "                <y>1.0</y>\n"
				+ "                <z>1.0</z>\n"
				+ "            </position>\n"
				+ "            <gateway>true</gateway>\n"
				+ "            <nodeType>telosb</nodeType>\n"
				+ "            <description>Processor: MSP 430F1611 (Ram 10kB, Flash 48kB, op/sleep 22mA, 5myA) Radio: TI CC2420 IEEE 802.15.4(2,4 GHz)</description>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:temperature</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>degrees</unit>\n"
				+ "                <default>0</default>\n"
				+ "            </capability>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:light</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>lux</unit>\n"
				+ "                <default>0</default>\n"
				+ "            </capability>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:ir</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>lux</unit>\n"
				+ "                <default>0</default>\n"
				+ "            </capability>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:humidity</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>raw</unit>\n"
				+ "                <default>0</default>\n"
				+ "            </capability>\n"
				+ "        </node>\n"
				+ "        <node id=\"urn:wisebed:uzl1:0x211c\">\n"
				+ "            <position>\n"
				+ "                <x>10.0</x>\n"
				+ "                <y>1.0</y>\n"
				+ "                <z>1.0</z>\n"
				+ "            </position>\n"
				+ "            <gateway>true</gateway>\n"
				+ "            <nodeType>isense</nodeType>\n"
				+ "            <description>Processor: Jennic JN5148 (128kB RAM, 512kB Flash, 32 Bit RISC Controller, 4-32MHz) Radio: IEEE 802.15.4 compliant radio, 250kbit/s, hardware AES encryption, ToF ranging engine)</description>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:pir</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>raw</unit>\n"
				+ "                <default>0</default>\n"
				+ "            </capability>\n"
				+ "            <capability>\n"
				+ "                <name>urn:wisebed:node:capability:acc</name>\n"
				+ "                <datatype>integer</datatype>\n"
				+ "                <unit>raw</unit>\n"
				+ "                <default>[0,0,0]</default>\n"
				+ "            </capability>\n"
				+ "        </node>\n"
				+ "    </setup>\n"
				+ "</wiseml>\n";
	}
}
