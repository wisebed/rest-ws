package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.util.WSNServiceHelper;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class WsnProxyService extends AbstractService implements WsnProxy {

	private static class FutureJobResultListener implements JobResultListener {

		private SettableFuture<JobStatus> future;

		private FutureJobResultListener(final SettableFuture<JobStatus> future) {
			this.future = future;
		}

		@Override
		public void receiveJobResult(final JobStatus status) {
			future.set(status);
		}

		@Override
		public void timeout() {
			future.setException(new TimeoutException());
		}
	}

	private static DatatypeFactory DATATYPE_FACTORY;

	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private final AsyncJobObserver asyncJobObserver;

	private final String experimentWsnInstanceEndpointUrl;

	private final AsyncEventBus asyncEventBus;

	private WSN wsn;

	private ListeningExecutorService executor;

	@Inject
	public WsnProxyService(@Assisted final AsyncJobObserver asyncJobObserver,
						   @Assisted final String experimentWsnInstanceEndpointUrl,
						   @Assisted final AsyncEventBus asyncEventBus) {

		checkNotNull(asyncJobObserver);
		checkNotNull(experimentWsnInstanceEndpointUrl);
		checkNotNull(asyncEventBus);

		this.asyncJobObserver = asyncJobObserver;
		this.experimentWsnInstanceEndpointUrl = experimentWsnInstanceEndpointUrl;
		this.asyncEventBus = asyncEventBus;
	}

	@Override
	protected void doStart() {
		try {

			wsn = WSNServiceHelper.getWSNService(experimentWsnInstanceEndpointUrl);
			executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

			asyncEventBus.register(this);

			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {

			asyncEventBus.unregister(this);
			ExecutorUtils.shutdown(executor, 10, TimeUnit.SECONDS);

			notifyStopped();

		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Subscribe
	public void onDownstreamMessageEvent(final DownstreamMessageEvent downstreamMessageEvent) {

		Message message = new Message();
		message.setBinaryData(downstreamMessageEvent.getMessageBytes());
		message.setSourceNodeId(downstreamMessageEvent.getTargetNodeUrn());
		message.setTimestamp(timestamp(downstreamMessageEvent));

		send(newArrayList(downstreamMessageEvent.getTargetNodeUrn()), message, 60, TimeUnit.SECONDS);
	}

	private XMLGregorianCalendar timestamp(final DownstreamMessageEvent downstreamMessageEvent) {
		return DATATYPE_FACTORY.newXMLGregorianCalendar(
				downstreamMessageEvent.getTimestamp().toGregorianCalendar()
		);
	}

	@Override
	public ListenableFuture<Void> addController(final String controllerEndpointUrl) {
		return executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				wsn.addController(controllerEndpointUrl);
				return null;
			}
		}
		);
	}

	@Override
	public ListenableFuture<Void> removeController(final String controllerEndpointUrl) {
		return executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				wsn.removeController(controllerEndpointUrl);
				return null;
			}
		}
		);
	}

	@Override
	public ListenableFuture<JobStatus> send(final List<String> nodeIds, final Message message, final int timeout,
											final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.send(nodeIds, message), nodeIds, Job.JobType.send);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> setChannelPipeline(final List<String> nodes,
														  final List<ChannelHandlerConfiguration> channelHandlerConfigurations,
														  final int timeout, final TimeUnit timeUnit) {

		final SettableFuture<JobStatus> future = SettableFuture.create();
		final Job job = new Job(
				wsn.setChannelPipeline(nodes, channelHandlerConfigurations),
				nodes,
				Job.JobType.setChannelPipeline
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<String> getVersion() {

		return executor.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return wsn.getVersion();
			}
		}
		);
	}

	@Override
	public ListenableFuture<JobStatus> areNodesAlive(final List<String> nodes, final int timeout,
													 final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.areNodesAlive(nodes), nodes, Job.JobType.areNodesAlive);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> destroyVirtualLink(final String sourceNode, final String targetNode,
														  final int timeout,
														  final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.destroyVirtualLink(sourceNode, targetNode), sourceNode,
				Job.JobType.destroyVirtualLink
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> disableNode(final String node, final int timeout, final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.disableNode(node), node, Job.JobType.disableNode);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> disablePhysicalLink(final String nodeA, final String nodeB, final int timeout,
														   final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.disablePhysicalLink(nodeA, nodeB), nodeA,
				Job.JobType.disablePhysicalLink
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> enableNode(final String node, final int timeout, final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.enableNode(node), node, Job.JobType.enableNode);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> enablePhysicalLink(final String nodeA, final String nodeB, final int timeout,
														  final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.enablePhysicalLink(nodeA, nodeB), nodeA,
				Job.JobType.enablePhysicalLink
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> flashPrograms(final List<String> nodeIds, final List<Integer> programIndices,
													 final List<Program> programs, final int timeout,
													 final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.flashPrograms(nodeIds, programIndices, programs), nodeIds,
				Job.JobType.flashPrograms
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<List<String>> getFilters() {

		return executor.submit(new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return wsn.getFilters();
			}
		}
		);
	}

	@Override
	public ListenableFuture<String> getNetwork() {

		return executor.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return wsn.getNetwork();
			}
		}
		);
	}

	@Override
	public ListenableFuture<List<ChannelHandlerDescription>> getSupportedChannelHandlers() {

		return executor.submit(new Callable<List<ChannelHandlerDescription>>() {
			@Override
			public List<ChannelHandlerDescription> call() throws Exception {
				return wsn.getSupportedChannelHandlers();
			}
		}
		);
	}

	@Override
	public ListenableFuture<JobStatus> resetNodes(final List<String> nodes, final int timeout,
												  final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(wsn.resetNodes(nodes), nodes, Job.JobType.resetNodes);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<JobStatus> setVirtualLink(final String sourceNode, final String targetNode,
													  final String remoteServiceInstance, final List<String> parameters,
													  final List<String> filters, final int timeout,
													  final TimeUnit timeUnit) {

		SettableFuture<JobStatus> future = SettableFuture.create();
		Job job = new Job(
				wsn.setVirtualLink(
						sourceNode,
						targetNode,
						remoteServiceInstance,
						parameters,
						filters
				),
				sourceNode,
				Job.JobType.setVirtualLink
		);
		job.addListener(new FutureJobResultListener(future));
		asyncJobObserver.submit(job, timeout, timeUnit);
		return future;
	}
}
