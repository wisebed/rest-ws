package eu.wisebed.restws.proxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;

import eu.wisebed.restws.jobs.Job;

public interface WsnProxyManager {

	void create(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration);

	@Nullable
	WsnProxy get(@Nonnull String experimentWsnInstanceEndpointUrl);
	
	@Nullable
	String getControllerEndpointUrl(@Nonnull String experimentWsnInstanceEndpointUrl);

	@Nullable
	Job getJob(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull String requestId);

}
