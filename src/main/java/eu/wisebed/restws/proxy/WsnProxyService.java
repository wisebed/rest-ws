package eu.wisebed.restws.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.restws.jobs.Job;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface WsnProxyService extends Service {

	ListenableFuture<Void> addController(String controllerEndpointUrl);

	ListenableFuture<Void> removeController(String controllerEndpointUrl);

	ListenableFuture<Job> send(List<String> nodeIds, Message message, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> setChannelPipeline(List<String> nodes,
											 List<ChannelHandlerConfiguration> channelHandlerConfigurations,
											 int timeout, TimeUnit timeUnit);

	ListenableFuture<String> getVersion();

	ListenableFuture<Job> areNodesAlive(List<String> nodes, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> destroyVirtualLink(String sourceNode, String targetNode, int timeout,
											 TimeUnit timeUnit);

	ListenableFuture<Job> disableNode(String node, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> disablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> enableNode(String node, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> enablePhysicalLink(String nodeA, String nodeB, int timeout, TimeUnit timeUnit);

	String flashPrograms(List<String> nodeIds, List<Integer> programIndices, List<Program> programs,
						 int timeout, TimeUnit timeUnit);

	ListenableFuture<List<String>> getFilters();

	ListenableFuture<String> getNetwork();

	ListenableFuture<List<ChannelHandlerDescription>> getSupportedChannelHandlers();

	ListenableFuture<Job> resetNodes(List<String> nodes, int timeout, TimeUnit timeUnit);

	ListenableFuture<Job> setVirtualLink(String sourceNode, String targetNode, String remoteServiceInstance,
										 List<String> parameters, List<String> filters, int timeout,
										 TimeUnit timeUnit);

}
