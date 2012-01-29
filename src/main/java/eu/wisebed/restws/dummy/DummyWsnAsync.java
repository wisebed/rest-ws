package eu.wisebed.restws.dummy;

import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.Job.JobType;
import de.uniluebeck.itm.wisebed.cmdlineclient.jobs.JobResult;
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.IWsnAsyncWrapper;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DummyWsnAsync implements IWsnAsyncWrapper {

	@Override
	public Future<Void> addController(String controllerEndpointUrl) {
		return new DummyFuture<Void>(null);
	}

	@Override
	public Future<Void> removeController(String controllerEndpointUrl) {
		return new DummyFuture<Void>(null);
	}

	@Override
	public Future<JobResult> send(List<String> nodeIds, Message message, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(nodeIds, JobType.send));
	}

	@Override
	public Future<JobResult> setChannelPipeline(List<String> nodes,
												List<ChannelHandlerConfiguration> channelHandlerConfigurations,
												int timeout,
												TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(nodes, JobType.setChannelPipeline));
	}

	@Override
	public Future<String> getVersion() {
		return new DummyFuture<String>("2.3");
	}

	@Override
	public Future<JobResult> areNodesAlive(List<String> nodes, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(nodes, JobType.areNodesAlive));
	}

	@Override
	public Future<String> describeCapabilities(String capability) {
		return new DummyFuture<String>("mächtig viel");
	}

	@Override
	public Future<JobResult> destroyVirtualLink(String sourceNode, String targetNode, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(sourceNode), JobType.destroyVirtualLink));
	}

	@Override
	public Future<JobResult> disableNode(String node, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(node), JobType.disableNode));
	}

	@Override
	public Future<JobResult> disablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(nodeA), JobType.disablePhysicalLink));
	}

	@Override
	public Future<JobResult> enableNode(String node, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(node), JobType.enableNode));
	}

	@Override
	public Future<JobResult> enablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(nodeA), JobType.enablePhysicalLink));
	}

	@Override
	public Future<JobResult> flashPrograms(List<String> nodeIds, List<Integer> programIndices, List<Program> programs,
										   int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(nodeIds, JobType.flashPrograms));
	}

	@Override
	public Future<List<String>> getFilters() {
		return new DummyFuture<List<String>>(Arrays.asList("keiner"));
	}

	@Override
	public Future<String> getNetwork() {

		return new DummyFuture<String>("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
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
				+ "</wiseml>\n");
	}

	@Override
	public Future<List<ChannelHandlerDescription>> getSupportedChannelHandlers() {
		List<ChannelHandlerDescription> list = Arrays.asList();
		return new DummyFuture<List<ChannelHandlerDescription>>(list);
	}

	@Override
	public Future<JobResult> resetNodes(List<String> nodes, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(nodes, JobType.resetNodes));
	}

	@Override
	public Future<JobResult> setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance,
											List<String> parameters,
											List<String> filters, int timeout, TimeUnit timeUnit) {
		return new DummyFuture<JobResult>(generateResults(Arrays.asList(sourceNode), JobType.setVirtualLink));
	}

	private JobResult generateResults(List<String> nodeIds, JobType jobType) {
		JobResult result = new JobResult(jobType);
		Random r = new Random();
		for (String urn : nodeIds) {
			result.addResult(urn, r.nextBoolean(), "no message here");
		}
		return result;
	}

}
