package eu.wisebed.restws.dummy;

import java.util.List;

import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.WSN;

public class DummyWsn implements WSN {

	@Override
	public void addController(String controllerEndpointUrl) {
	}

	@Override
	public String areNodesAlive(List<String> nodes) {
		return null;
	}

	@Override
	public String destroyVirtualLink(String sourceNode, String targetNode) {
		return null;
	}

	@Override
	public String disableNode(String node) {
		return null;
	}

	@Override
	public String disablePhysicalLink(String nodeA, String nodeB) {
		return null;
	}

	@Override
	public String enableNode(String node) {
		return null;
	}

	@Override
	public String enablePhysicalLink(String nodeA, String nodeB) {
		return null;
	}

	@Override
	public String flashPrograms(List<String> nodeIds, List<Integer> programIndices, List<Program> programs) {
		return null;
	}

	@Override
	public List<ChannelHandlerDescription> getSupportedChannelHandlers() {
		return null;
	}

	@Override
	public List<String> getFilters() {
		return null;
	}

	@Override
	public String getNetwork() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public void removeController(String controllerEndpointUrl) {
	}

	@Override
	public String resetNodes(List<String> nodes) {
		return null;
	}

	@Override
	public String send(List<String> nodeIds, Message message) {
		return null;
	}

	@Override
	public String setChannelPipeline(List<String> nodes, List<ChannelHandlerConfiguration> channelHandlerConfigurations) {
		return null;
	}

	@Override
	public String setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance, List<String> parameters, List<String> filters) {
		return null;
	}
}
