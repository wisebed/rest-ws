package eu.wisebed.restws.proxy;

import eu.wisebed.restws.jobs.Job;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface WsnProxyManager {

	@Nonnull
	WsnProxy create(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration);

	@Nullable
	WsnProxy get(@Nonnull String experimentWsnInstanceEndpointUrl);

	@Nullable
	Job getJob(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull String requestId);

}
