package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import eu.wisebed.restws.util.InjectLogger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

@Singleton
public class WsnProxyManagerImpl implements WsnProxyManager {

	private static class ProxyCacheEntry {

		private final ExecutorService executorService;

		private final WsnProxyService wsnProxyService;

		private final AsyncJobObserver asyncJobObserver;

		private final ControllerProxyService controllerProxyService;

		private ProxyCacheEntry(final ControllerProxyService controllerProxyService,
								final ExecutorService executorService,
								final WsnProxyService wsnProxyService,
								final AsyncJobObserver asyncJobObserver) {

			this.controllerProxyService = controllerProxyService;
			this.executorService = executorService;
			this.wsnProxyService = wsnProxyService;
			this.asyncJobObserver = asyncJobObserver;
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

		public AsyncJobObserver getAsyncJobObserver() {
			return asyncJobObserver;
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
						log.error("{}", e);
					}

					try {
						entry.getControllerProxyService().stop().get();
					} catch (Exception e) {
						log.error("{}", e);
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

	@SuppressWarnings("unused")
	public WsnProxyManagerImpl() {
		proxyCache.setListener(experimentExpirationListener);
	}

	@Override
	@Nonnull
	public WsnProxy create(@Nonnull final String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration) {

		checkNotNull(experimentWsnInstanceEndpointUrl);
		checkNotNull(expiration);
		checkArgument(expiration.isAfter(DateTime.now()));

		ExecutorService executor = Executors.newCachedThreadPool();
		AsyncEventBus asyncEventBus = new AsyncEventBus(experimentWsnInstanceEndpointUrl, executor);
		AsyncJobObserver asyncJobObserver = new AsyncJobObserver();

		ControllerProxyService controllerProxyService = controllerProxyServiceFactory.create(
				experimentWsnInstanceEndpointUrl,
				asyncJobObserver,
				asyncEventBus
		);

		WsnProxyService wsnProxyService = wsnProxyServiceFactory.create(
				asyncJobObserver,
				experimentWsnInstanceEndpointUrl,
				asyncEventBus
		);

		try {
			controllerProxyService.start().get();
			wsnProxyService.start().get();
		} catch (Exception e) {
			throw propagate(e);
		}

		ProxyCacheEntry proxyCacheEntry = new ProxyCacheEntry(controllerProxyService, executor, wsnProxyService, asyncJobObserver);
		Duration d = new Duration(DateTime.now(), expiration);

		proxyCache.put(experimentWsnInstanceEndpointUrl, proxyCacheEntry, d.getMillis(), TimeUnit.MILLISECONDS);

		return wsnProxyService;
	}

	@Override
	@Nullable
	public WsnProxy get(@Nonnull final String experimentWsnInstanceEndpointUrl) {

		checkNotNull(experimentWsnInstanceEndpointUrl);

		ProxyCacheEntry proxyCacheEntry = proxyCache.get(experimentWsnInstanceEndpointUrl);
		return proxyCacheEntry == null ? null : proxyCacheEntry.getWsnProxyService();
	}

	@Override
	public JobStatus getStatus(@Nonnull final String experimentWsnInstanceEndpointUrl,
							   @Nonnull final String requestId) {

		checkNotNull(experimentWsnInstanceEndpointUrl);
		checkNotNull(requestId);

		ProxyCacheEntry proxyCacheEntry = proxyCache.get(experimentWsnInstanceEndpointUrl);
		if (proxyCacheEntry == null) {
			return null;
		}

		return proxyCacheEntry.getAsyncJobObserver().getStatus(requestId);
	}
}
