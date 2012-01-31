package eu.wisebed.restws.proxy;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface WsnProxyManager {

	@Nonnull
	WsnProxy create(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull DateTime expiration);

	@Nullable
	WsnProxy get(@Nonnull String experimentWsnInstanceEndpointUrl);
	
	@Nullable
	JobStatus getStatus(@Nonnull String experimentWsnInstanceEndpointUrl, @Nonnull String requestId);

}
