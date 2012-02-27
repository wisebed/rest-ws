/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package eu.wisebed.restws.jobs;

import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import eu.wisebed.api.controller.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.collect.Sets.newHashSet;

public class JobObserver {

	private static final Logger log = LoggerFactory.getLogger(JobObserver.class);

	private final TimedCache<String, Job> jobCache = new TimedCache<String, Job>(60, TimeUnit.MINUTES);

	/**
	 * If a request status arrives before a job is submitted it is cached here. If the job is then submitted it will be
	 * first checked if the request status for the job is contained here and, in this case, the job will be assumed to be
	 * completed.
	 */
	private final TimedCache<String, List<RequestStatus>> unknownRequestStatusesCache =
			new TimedCache<String, List<RequestStatus>>(60, TimeUnit.MINUTES);

	private final Set<JobListener> listeners = newHashSet();

	private Lock lock = new ReentrantLock();

	public JobObserver() {
		jobCache.setListener(new TimedCacheListener<String, Job>() {
			@Override
			public Tuple<Long, TimeUnit> timeout(final String key, final Job value) {
				value.notifyListenersTimeout();
				return null;
			}
		}
		);
	}

	public void submit(Job job, int timeout, TimeUnit timeUnit) {

		log.trace("Submitted job with request ID {}", job.getRequestId());

		lock.lock();
		try {

			jobCache.put(job.getRequestId(), job, timeout, timeUnit);

			for (JobListener l : listeners) {
				job.addListener(l);
			}

			List<RequestStatus> unknownRequestStatusList = unknownRequestStatusesCache.get(job.getRequestId());

			if (unknownRequestStatusList != null) {
				log.trace("Found cached unknown request statuses");
				unknownRequestStatusesCache.remove(job.getRequestId());
				process(unknownRequestStatusList);
			}

		} finally {
			lock.unlock();
		}

	}

	public void process(final List<RequestStatus> requestStatusList) {

		lock.lock();

		try {

			for (RequestStatus status : requestStatusList) {

				final String requestId = status.getRequestId();
				final Job job = jobCache.get(requestId);

				if (job != null) {

					job.process(status);

				} else {

					List<RequestStatus> unknownRequestStatusesForRequestId =
							unknownRequestStatusesCache.get(requestId);

					if (unknownRequestStatusesForRequestId == null) {
						unknownRequestStatusesForRequestId = new LinkedList<RequestStatus>();
						unknownRequestStatusesCache.put(requestId, unknownRequestStatusesForRequestId);
					}
					unknownRequestStatusesForRequestId.add(status);
				}
			}

		} finally {
			lock.unlock();
		}
	}

	public void addListener(JobListener listener) {
		listeners.add(listener);
	}

	public void removeListener(JobListener listener) {
		listeners.remove(listener);
	}

	@Nullable
	public Job getJob(final String requestId) {
		return jobCache.get(requestId);
	}
}
