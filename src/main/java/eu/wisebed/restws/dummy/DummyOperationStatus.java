package eu.wisebed.restws.dummy;

import com.google.inject.Singleton;
import de.uniluebeck.itm.tr.util.TimedCache;
import eu.wisebed.api.controller.Status;
import eu.wisebed.restws.OperationStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class DummyOperationStatus implements OperationStatus {

	private final TimedCache<String, Status> statusCache =
			new TimedCache<String, Status>(10, TimeUnit.MINUTES);

	@Override
	public void put(@Nonnull final String requestId, @Nonnull final Status status) {
		checkNotNull(requestId);
		checkNotNull(status);
		statusCache.put(requestId, status);
	}

	@Override
	@Nullable
	public Status get(@Nonnull final String requestId) {
		checkNotNull(requestId);
		return statusCache.get(requestId);
	}
}
