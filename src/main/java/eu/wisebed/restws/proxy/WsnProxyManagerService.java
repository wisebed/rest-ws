package eu.wisebed.restws.proxy;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Service;
import eu.wisebed.restws.jobs.Job;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface WsnProxyManagerService extends Service {

	void create(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration);

	@Nullable
	WsnProxyService get(@Nonnull String experimentWsnInstanceEndpointUrl);

	@Nullable
	String getControllerEndpointUrl(@Nonnull String experimentWsnInstanceEndpointUrl);

	@Nullable
	Job getJob(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull String requestId);

	@Nonnull
	EventBus getEventBus(@Nonnull String experimentWsnInstanceEndpointUrl);

}
