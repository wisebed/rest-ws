package eu.wisebed.restws.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface WsnProxy {

	ListenableFuture<Void> addController(String controllerEndpointUrl);

	ListenableFuture<Void> removeController(String controllerEndpointUrl);

	ListenableFuture<JobStatus> send(List<String> nodeIds, Message message, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> setChannelPipeline(List<String> nodes,
												   List<ChannelHandlerConfiguration> channelHandlerConfigurations,
												   int timeout, TimeUnit timeUnit);

	ListenableFuture<String> getVersion();

	ListenableFuture<JobStatus> areNodesAlive(List<String> nodes, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> destroyVirtualLink(String sourceNode, String targetNode, int timeout,
												   TimeUnit timeUnit);

	ListenableFuture<JobStatus> disableNode(String node, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> disablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> enableNode(String node, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> enablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit);

	String flashPrograms(List<String> nodeIds, List<Integer> programIndices, List<Program> programs,
						 int timeout, TimeUnit timeUnit);

	ListenableFuture<List<String>> getFilters();

	ListenableFuture<String> getNetwork();

	ListenableFuture<List<ChannelHandlerDescription>> getSupportedChannelHandlers();

	ListenableFuture<JobStatus> resetNodes(List<String> nodes, int timeout, TimeUnit timeUnit);

	ListenableFuture<JobStatus> setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance,
											   List<String> parameters, List<String> filters, int timeout,
											   TimeUnit timeUnit);

}
