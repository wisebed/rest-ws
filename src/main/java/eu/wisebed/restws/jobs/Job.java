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

import de.uniluebeck.itm.tr.util.ListenerManager;
import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
import eu.wisebed.api.controller.RequestStatus;
import eu.wisebed.api.controller.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class Job {

	private static final Logger log = LoggerFactory.getLogger(Job.class);

	private final JobType jobType;

	private final ListenerManager<JobListener> listenerManager = new ListenerManagerImpl<JobListener>();

	private final String requestId;

	// NodeUrn -> JobNodeStatus
	private final Map<String, JobNodeStatus> jobNodeStates = new HashMap<String, JobNodeStatus>();

	public Job(String requestId, List<String> nodeUrns, JobType jobType) {

		this.requestId = requestId;
		this.jobType = jobType;

		for (String nodeUrn : newHashSet(nodeUrns)) {
			jobNodeStates.put(nodeUrn, new JobNodeStatus(JobState.RUNNING, 0, "Starting..."));
		}
	}

	public Job(String requestId, String nodeId, JobType jobType) {
		this(requestId, newArrayList(nodeId), jobType);
	}

	public String getRequestId() {
		return requestId;
	}

	private boolean isDone(int statusCode) {

		if (jobType == JobType.ARE_NODES_ALIVE) {
			return statusCode == 1;
		} else if (jobType == JobType.RESET_NODES) {
			return statusCode == 1;
		} else if (jobType == JobType.SEND) {
			return statusCode == 1;
		} else if (jobType == JobType.FLASH_PROGRAMS) {
			return statusCode == 100;
		} else if (jobType == JobType.SET_VIRTUAL_LINK) {
			return statusCode == 1;
		} else if (jobType == JobType.DESTROY_VIRTUAL_LINK) {
			return statusCode == 1;
		} else if (jobType == JobType.DISABLE_NODE) {
			return statusCode == 1;
		} else if (jobType == JobType.ENABLE_NODE) {
			return statusCode == 1;
		} else if (jobType == JobType.DISABLE_PHYSICAL_LINK) {
			return statusCode == 1;
		} else if (jobType == JobType.ENABLE_PHYSICAL_LINK) {
			return statusCode == 1;
		} else if (jobType == JobType.SET_CHANNEL_PIPELINE) {
			return statusCode == 1;
		}

		return false;
	}

	private boolean isError(int value) {

		if (jobType == JobType.ARE_NODES_ALIVE) {
			return value <= 0;
		} else if (jobType == JobType.RESET_NODES) {
			return value == 0 || value == -1;
		} else if (jobType == JobType.SEND) {
			return value == 0 || value == -1;
		} else if (jobType == JobType.FLASH_PROGRAMS) {
			return value < 0;
		} else if (jobType == JobType.SET_VIRTUAL_LINK) {
			return value < 1;
		} else if (jobType == JobType.DESTROY_VIRTUAL_LINK) {
			return value < 1;
		} else if (jobType == JobType.DISABLE_NODE) {
			return value < 1;
		} else if (jobType == JobType.ENABLE_NODE) {
			return value < 1;
		} else if (jobType == JobType.DISABLE_PHYSICAL_LINK) {
			return value < 1;
		} else if (jobType == JobType.ENABLE_PHYSICAL_LINK) {
			return value < 1;
		} else if (jobType == JobType.SET_CHANNEL_PIPELINE) {
			return value < 1;
		}

		return false;
	}

	public JobState process(RequestStatus status) {

		log.debug("Processing request status update: " + status);
		
		for (Status s : status.getStatus()) {

			JobState nodeJobState = JobState.RUNNING;
			if (isDone(s.getValue())) {
				nodeJobState = JobState.SUCCESS;
			} else if (isError(s.getValue())) {
				nodeJobState = JobState.FAILED;
			}

			updateNodeState(s.getNodeId(), nodeJobState, s.getValue(), s.getMsg());
		}

		JobState jobJobState = determineJobState();
		if (jobJobState != JobState.RUNNING) {
			notifyListenersJobDone();
		} else {
			notifyListenersJobStateChanged();
		}

		return jobJobState;
	}

	public void addListener(JobListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeListener(JobListener listener) {
		listenerManager.removeListener(listener);
	}

	private void notifyListenersJobDone() {
		for (JobListener l : listenerManager.getListeners()) {
			l.onJobDone(this);
		}
	}

	private void notifyListenersJobStateChanged() {
		for (JobListener l : listenerManager.getListeners()) {
			l.onJobStatusChanged(this);
		}
	}

	public void notifyListenersTimeout() {
		for (JobListener l : listenerManager.getListeners()) {
			l.onJobTimeout(this);
		}
	}

	public JobType getJobType() {
		return jobType;
	}

	private JobState determineJobState() {

		int running = 0;
		int success = 0;

		for (JobNodeStatus jobNodeStatus : jobNodeStates.values()) {
			switch (jobNodeStatus.getStatus()) {
				case RUNNING:
					running++;
					break;
				case SUCCESS:
					success++;
					break;
			}
		}

		if (running > 0) {
			return JobState.RUNNING;
		} else if (success == jobNodeStates.size()) {
			return JobState.SUCCESS;
		} else {
			return JobState.FAILED;
		}
	}

	private void updateNodeState(String nodeUrn, JobState nodeJobState, int statusCode, @Nullable String message) {

		JobNodeStatus jobNodeStatus = jobNodeStates.get(nodeUrn);
		if (jobNodeStatus == null) {
			log.warn("Received status for unknown node URN " + nodeUrn + "!");
			return;
		}

		jobNodeStatus.setStatus(nodeJobState);
		jobNodeStatus.setStatusCode(statusCode);
		jobNodeStatus.setMessage(message);
	}

	public Map<String, JobNodeStatus> getJobNodeStates() {
		return jobNodeStates;
	}
}
