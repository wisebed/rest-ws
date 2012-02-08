package eu.wisebed.restws.proxy;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import de.uniluebeck.itm.tr.util.ExecutorUtils;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.wsn.ChannelHandlerConfiguration;
import eu.wisebed.api.wsn.ChannelHandlerDescription;
import eu.wisebed.api.wsn.Program;
import eu.wisebed.api.wsn.WSN;
import eu.wisebed.restws.event.DownstreamMessageEvent;
import eu.wisebed.restws.jobs.Job;
import eu.wisebed.restws.jobs.JobListener;
import eu.wisebed.restws.jobs.JobObserver;
import eu.wisebed.restws.jobs.JobType;
import eu.wisebed.restws.util.WSNServiceHelper;

public class WsnProxyServiceImpl extends AbstractService implements WsnProxyService {

	private final WsnProxyManagerService wsnProxyManagerService;

	private static class FutureJobListener implements JobListener {

		private final SettableFuture<Job> future;

		private FutureJobListener(final SettableFuture<Job> future) {
			this.future = future;
		}

		@Override
		public void onJobStatusChanged(final Job job) {
			// nothing to do
		}

		@Override
		public void onJobDone(final Job job) {
			future.set(job);
		}

		@Override
		public void onJobTimeout(final Job job) {
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

	private final JobObserver jobObserver;

	private final String experimentWsnInstanceEndpointUrl;

	private WSN wsn;

	private ListeningExecutorService executor;

	@Inject
	public WsnProxyServiceImpl(final WsnProxyManagerService wsnProxyManagerService,
							   @Assisted final JobObserver jobObserver,
							   @Assisted final String experimentWsnInstanceEndpointUrl) {

		checkNotNull(wsnProxyManagerService);
		checkNotNull(jobObserver);
		checkNotNull(experimentWsnInstanceEndpointUrl);

		this.jobObserver = jobObserver;
		this.experimentWsnInstanceEndpointUrl = experimentWsnInstanceEndpointUrl;
		this.wsnProxyManagerService = wsnProxyManagerService;
	}

	@Override
	protected void doStart() {
		try {

			wsn = WSNServiceHelper.getWSNService(experimentWsnInstanceEndpointUrl);
			executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

			getEventBus().register(this);

			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {

			getEventBus().unregister(this);
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
	public ListenableFuture<Job> send(final List<String> nodeIds, final Message message, final int timeout,
											final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.send(nodeIds, message), nodeIds, JobType.SEND);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> setChannelPipeline(final List<String> nodes,
														  final List<ChannelHandlerConfiguration> channelHandlerConfigurations,
														  final int timeout, final TimeUnit timeUnit) {

		final SettableFuture<Job> future = SettableFuture.create();
		final Job job = new Job(
				wsn.setChannelPipeline(nodes, channelHandlerConfigurations),
				nodes,
				JobType.SET_CHANNEL_PIPELINE
		);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
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
	public ListenableFuture<Job> areNodesAlive(final List<String> nodes, final int timeout,
													 final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.areNodesAlive(nodes), nodes, JobType.ARE_NODES_ALIVE);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> destroyVirtualLink(final String sourceNode, final String targetNode,
														  final int timeout,
														  final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.destroyVirtualLink(sourceNode, targetNode), sourceNode,
				JobType.DESTROY_VIRTUAL_LINK
		);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> disableNode(final String node, final int timeout, final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.disableNode(node), node, JobType.DISABLE_NODE);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> disablePhysicalLink(final String nodeA, final String nodeB, final int timeout,
														   final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.disablePhysicalLink(nodeA, nodeB), nodeA,
				JobType.DISABLE_PHYSICAL_LINK
		);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> enableNode(final String node, final int timeout, final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.enableNode(node), node, JobType.ENABLE_NODE);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> enablePhysicalLink(final String nodeA, final String nodeB, final int timeout,
														  final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.enablePhysicalLink(nodeA, nodeB), nodeA,
				JobType.ENABLE_PHYSICAL_LINK
		);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public String flashPrograms(final List<String> nodeIds, final List<Integer> programIndices,
													 final List<Program> programs, final int timeout,
													 final TimeUnit timeUnit) {

		String requestId = wsn.flashPrograms(nodeIds, programIndices, programs);

		Job job = new Job(requestId, nodeIds, JobType.FLASH_PROGRAMS);
		jobObserver.submit(job, timeout, timeUnit);

		return requestId;
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
	public ListenableFuture<Job> resetNodes(final List<String> nodes, final int timeout,
												  final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(wsn.resetNodes(nodes), nodes, JobType.RESET_NODES);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	@Override
	public ListenableFuture<Job> setVirtualLink(final String sourceNode, final String targetNode,
													  final String remoteServiceInstance, final List<String> parameters,
													  final List<String> filters, final int timeout,
													  final TimeUnit timeUnit) {

		SettableFuture<Job> future = SettableFuture.create();
		Job job = new Job(
				wsn.setVirtualLink(
						sourceNode,
						targetNode,
						remoteServiceInstance,
						parameters,
						filters
				),
				sourceNode,
				JobType.SET_VIRTUAL_LINK
		);
		job.addListener(new FutureJobListener(future));
		jobObserver.submit(job, timeout, timeUnit);
		return future;
	}

	private EventBus getEventBus() {
		return wsnProxyManagerService.getEventBus(experimentWsnInstanceEndpointUrl);
	}
}
