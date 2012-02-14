package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import eu.wisebed.restws.jobs.Job;
import eu.wisebed.restws.jobs.JobObserver;
import eu.wisebed.restws.util.InjectLogger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class WsnProxyManagerServiceImpl extends AbstractService implements WsnProxyManagerService {

	private static class ProxyCacheEntry {

		private final ExecutorService executorService;

		private final WsnProxyService wsnProxyService;

		private final JobObserver jobObserver;

		private final ControllerProxyService controllerProxyService;

		private ProxyCacheEntry(final ControllerProxyService controllerProxyService,
								final ExecutorService executorService,
								final WsnProxyService wsnProxyService,
								final JobObserver jobObserver) {

			this.controllerProxyService = controllerProxyService;
			this.executorService = executorService;
			this.wsnProxyService = wsnProxyService;
			this.jobObserver = jobObserver;
		}

		public ControllerProxyService getControllerProxyService() {
			return controllerProxyService;
		}

		public ExecutorService getExecutorService() {
			return executorService;
		}

		public WsnProxyService getWsnProxyService() {
			return wsnProxyService;
		}

		public JobObserver getJobObserver() {
			return jobObserver;
		}
	}

	private final TimedCacheListener<String, ProxyCacheEntry> experimentExpirationListener =
			new TimedCacheListener<String, ProxyCacheEntry>() {
				@Override
				public Tuple<Long, TimeUnit> timeout(final String experimentWsnInstanceEndpointUrl,
													 final ProxyCacheEntry entry) {

					try {
						entry.getWsnProxyService().stop().get();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}

					try {
						entry.getControllerProxyService().stop().get();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}

					ExecutorUtils.shutdown(entry.getExecutorService(), 10, TimeUnit.SECONDS);

					return null;
				}
			};

	@InjectLogger
	private Logger log;

	private final TimedCache<String, ProxyCacheEntry> proxyCache = new TimedCache<String, ProxyCacheEntry>();

	@Inject
	private ControllerProxyServiceFactory controllerProxyServiceFactory;

	@Inject
	private WsnProxyServiceFactory wsnProxyServiceFactory;

	private final Map<String, AsyncEventBus> eventBusMap = newHashMap();

	private ExecutorService executor;

	public WsnProxyManagerServiceImpl() {
		proxyCache.setListener(experimentExpirationListener);
	}

	@Override
	protected void doStart() {
		log.debug("Starting WsnProxyManagerService");
		executor = Executors.newCachedThreadPool();
		notifyStarted();
	}

	@Override
	protected void doStop() {
		log.debug("Stopping WsnProxyManagerService");
		ExecutorUtils.shutdown(executor, 10, TimeUnit.SECONDS);
		notifyStopped();
	}

	@Override
	public void create(@Nonnull final String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration) {

		checkNotNull(experimentWsnInstanceEndpointUrl);
		checkNotNull(expiration);
		checkArgument(expiration.isAfter(DateTime.now()));

		ExecutorService executor = Executors.newCachedThreadPool();
		JobObserver jobObserver = new JobObserver();

		ControllerProxyService controllerProxyService = controllerProxyServiceFactory.create(
				experimentWsnInstanceEndpointUrl,
				jobObserver
		);

		WsnProxyService wsnProxyService = wsnProxyServiceFactory.create(
				jobObserver,
				experimentWsnInstanceEndpointUrl
		);

		try {
			controllerProxyService.start().get();
			wsnProxyService.start().get();
		} catch (Exception e) {
			throw propagate(e);
		}

		ProxyCacheEntry proxyCacheEntry = new ProxyCacheEntry(
				controllerProxyService,
				executor,
				wsnProxyService,
				jobObserver
		);

		Duration d = new Duration(DateTime.now(), expiration);

		proxyCache.put(experimentWsnInstanceEndpointUrl, proxyCacheEntry, d.getMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	@Nullable
	public WsnProxyService get(@Nonnull final String experimentWsnInstanceEndpointUrl) {

		checkNotNull(experimentWsnInstanceEndpointUrl);

		ProxyCacheEntry proxyCacheEntry = proxyCache.get(experimentWsnInstanceEndpointUrl);
		return proxyCacheEntry == null ? null : proxyCacheEntry.getWsnProxyService();
	}

	@Override
	public String getControllerEndpointUrl(@Nonnull final String experimentWsnInstanceEndpointUrl) {

		checkNotNull(experimentWsnInstanceEndpointUrl);

		ProxyCacheEntry proxyCacheEntry = proxyCache.get(experimentWsnInstanceEndpointUrl);
		return proxyCacheEntry == null ? null : proxyCacheEntry.getControllerProxyService().getEndpointUrl();
	}

	@Override
	@Nullable
	public Job getJob(@Nonnull final String experimentWsnInstanceEndpointUrl,
					  @Nonnull final String requestId) {

		checkNotNull(experimentWsnInstanceEndpointUrl);
		checkNotNull(requestId);

		ProxyCacheEntry proxyCacheEntry = proxyCache.get(experimentWsnInstanceEndpointUrl);
		if (proxyCacheEntry == null) {
			return null;
		}

		return proxyCacheEntry.getJobObserver().getJob(requestId);
	}

	@Override
	public synchronized AsyncEventBus getEventBus(@Nonnull final String experimentWsnInstanceEndpointUrl) {
		AsyncEventBus eventBus = eventBusMap.get(experimentWsnInstanceEndpointUrl);
		if (eventBus == null) {
			eventBus = new AsyncEventBus(experimentWsnInstanceEndpointUrl, executor);
			eventBusMap.put(experimentWsnInstanceEndpointUrl, eventBus);
		}
		return eventBus;
	}
}
