package eu.wisebed.restws.dummy;

import com.google.common.util.concurrent.ListenableFuture;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.restws.jobs.JobType;
import eu.wisebed.restws.proxy.WsnProxy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DummyWsnProxy implements WsnProxy {

	@Override
	public ListenableFuture<Void> addController(String controllerEndpointUrl) {
		return new DummyListenableFuture<Void>(null);
	}

	@Override
	public ListenableFuture<Void> removeController(String controllerEndpointUrl) {
		return new DummyListenableFuture<Void>(null);
	}

	@Override
	public ListenableFuture<JobStatus> send(List<String> nodeIds, Message message, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(nodeIds, JobType.SEND));
	}

	@Override
	public ListenableFuture<JobStatus> setChannelPipeline(List<String> nodes,
														  List<ChannelHandlerConfiguration> channelHandlerConfigurations,
														  int timeout,
														  TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(nodes, JobType.SET_CHANNEL_PIPELINE));
	}

	@Override
	public ListenableFuture<String> getVersion() {
		return new DummyListenableFuture<String>("2.3");
	}

	@Override
	public ListenableFuture<JobStatus> areNodesAlive(List<String> nodes, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(nodes, JobType.ARE_NODES_ALIVE));
	}

	@Override
	public ListenableFuture<JobStatus> destroyVirtualLink(String sourceNode, String targetNode, int timeout,
														  TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(
				generateResults(Arrays.asList(sourceNode), JobType.DESTROY_VIRTUAL_LINK)
		);
	}

	@Override
	public ListenableFuture<JobStatus> disableNode(String node, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(Arrays.asList(node), JobType.DISABLE_NODE));
	}

	@Override
	public ListenableFuture<JobStatus> disablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(
				generateResults(Arrays.asList(nodeA), JobType.DISABLE_PHYSICAL_LINK)
		);
	}

	@Override
	public ListenableFuture<JobStatus> enableNode(String node, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(Arrays.asList(node), JobType.ENABLE_NODE));
	}

	@Override
	public ListenableFuture<JobStatus> enablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(
				generateResults(Arrays.asList(nodeA), JobType.ENABLE_PHYSICAL_LINK)
		);
	}

	@Override
	public ListenableFuture<JobStatus> flashPrograms(List<String> nodeIds, List<Integer> programIndices,
													 List<Program> programs,
													 int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(nodeIds, JobType.FLASH_PROGRAMS));
	}

	@Override
	public ListenableFuture<List<String>> getFilters() {
		return new DummyListenableFuture<List<String>>(Arrays.asList("keiner"));
	}

	@Override
	public ListenableFuture<String> getNetwork() {

		return new DummyListenableFuture<String>("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
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
				+ "</wiseml>\n"
		);
	}

	@Override
	public ListenableFuture<List<ChannelHandlerDescription>> getSupportedChannelHandlers() {
		List<ChannelHandlerDescription> list = Arrays.asList();
		return new DummyListenableFuture<List<ChannelHandlerDescription>>(list);
	}

	@Override
	public ListenableFuture<JobStatus> resetNodes(List<String> nodes, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(generateResults(nodes, JobType.RESET_NODES));
	}

	@Override
	public ListenableFuture<JobStatus> setVirtualLink(String sourceNode, String targetNode,
													  String remoteServiceInstance,
													  List<String> parameters,
													  List<String> filters, int timeout, TimeUnit timeUnit) {
		return new DummyListenableFuture<JobStatus>(
				generateResults(Arrays.asList(sourceNode), JobType.SET_VIRTUAL_LINK)
		);
	}

}
