package eu.wisebed.restws.dummy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DummyFuture<T> implements Future<T> {
	T dummyValue;

	boolean cancelled = false;

	public DummyFuture(T dummyValue) {
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

}
