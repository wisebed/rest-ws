package eu.wisebed.restws.dummy;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DummyListenableFuture<T> implements ListenableFuture<T> {

	T dummyValue;

	boolean cancelled = false;

	public DummyListenableFuture(T dummyValue) {
		super();
		this.dummyValue = dummyValue;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancelled = true;
		return true;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return dummyValue;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

	@Override
	public void addListener(final Runnable listener, final Executor executor) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				listener.run();
			}
		});
	}
}
