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
		return new DummyFuture<String>("<wiseml/>");

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
