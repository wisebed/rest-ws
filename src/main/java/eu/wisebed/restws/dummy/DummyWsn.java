package eu.wisebed.restws.dummy;

import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.WSN;

import javax.jws.WebParam;
import java.util.List;

public class DummyWsn implements WSN {

	@Override
	public void addController(
			@WebParam(name = "controllerEndpointUrl", targetNamespace = "") final String controllerEndpointUrl) {
		// TODO implement
	}

	@Override
	public String areNodesAlive(@WebParam(name = "nodes", targetNamespace = "") final List<String> nodes) {
		return null;  // TODO implement
	}

	@Override
	public String destroyVirtualLink(@WebParam(name = "sourceNode", targetNamespace = "") final String sourceNode,
									 @WebParam(name = "targetNode", targetNamespace = "") final String targetNode) {
		return null;  // TODO implement
	}

	@Override
	public String disableNode(@WebParam(name = "node", targetNamespace = "") final String node) {
		return null;  // TODO implement
	}

	@Override
	public String disablePhysicalLink(@WebParam(name = "nodeA", targetNamespace = "") final String nodeA,
									  @WebParam(name = "nodeB", targetNamespace = "") final String nodeB) {
		return null;  // TODO implement
	}

	@Override
	public String enableNode(@WebParam(name = "node", targetNamespace = "") final String node) {
		return null;  // TODO implement
	}

	@Override
	public String enablePhysicalLink(@WebParam(name = "nodeA", targetNamespace = "") final String nodeA,
									 @WebParam(name = "nodeB", targetNamespace = "") final String nodeB) {
		return null;  // TODO implement
	}

	@Override
	public String flashPrograms(@WebParam(name = "nodeIds", targetNamespace = "") final List<String> nodeIds,
								@WebParam(name = "programIndices", targetNamespace = "")
								final List<Integer> programIndices,
								@WebParam(name = "programs", targetNamespace = "") final List<Program> programs) {
		return null;  // TODO implement
	}

	@Override
	public List<ChannelHandlerDescription> getSupportedChannelHandlers() {
		return null;  // TODO implement
	}

	@Override
	public List<String> getFilters() {
		return null;  // TODO implement
	}

	@Override
	public String getNetwork() {
		return null;  // TODO implement
	}

	@Override
	public String getVersion() {
		return null;  // TODO implement
	}

	@Override
	public void removeController(
			@WebParam(name = "controllerEndpointUrl", targetNamespace = "") final String controllerEndpointUrl) {
		// TODO implement
	}

	@Override
	public String resetNodes(@WebParam(name = "nodes", targetNamespace = "") final List<String> nodes) {
		return null;  // TODO implement
	}

	@Override
	public String send(@WebParam(name = "nodeIds", targetNamespace = "") final List<String> nodeIds,
					   @WebParam(name = "message", targetNamespace = "") final Message message) {
		return null;  // TODO implement
	}

	@Override
	public String setChannelPipeline(@WebParam(name = "nodes", targetNamespace = "") final List<String> nodes,
									 @WebParam(name = "channelHandlerConfigurations",
											 targetNamespace = "") final
									 List<ChannelHandlerConfiguration> channelHandlerConfigurations) {
		return null;  // TODO implement
	}

	@Override
	public String setVirtualLink(@WebParam(name = "sourceNode", targetNamespace = "") final String sourceNode,
								 @WebParam(name = "targetNode", targetNamespace = "") final String targetNode,
								 @WebParam(name = "remoteServiceInstance", targetNamespace = "") final
								 String remoteServiceInstance,
								 @WebParam(name = "parameters", targetNamespace = "") final List<String> parameters,
								 @WebParam(name = "filters", targetNamespace = "") final List<String> filters) {
		return null;  // TODO implement
	}
}
