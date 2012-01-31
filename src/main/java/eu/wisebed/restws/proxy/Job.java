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

package eu.wisebed.restws.proxy;

import eu.wisebed.api.controller.RequestStatus;
import eu.wisebed.api.controller.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class Job {

	private static final Logger log = LoggerFactory.getLogger(Job.class);

	private final JobType jobType;

	private final String requestId;

	private final Set<JobResultListener> listeners = new HashSet<JobResultListener>();


	public enum JobType {

		areNodesAlive,
		destroyVirtualLink,
		disableNode,
		disablePhysicalLink,
		enableNode,
		enablePhysicalLink,
		flashPrograms,
		resetNodes,
		send,
		setChannelPipeline,
		setVirtualLink,

	}

	// NodeUrn -> NodeStatus
	private final Map<String, NodeStatus> nodeStates = new HashMap<String, NodeStatus>();

	public void updateNodeState(String nodeUrn, State nodeState, int statusCode, @Nullable String message) {

		NodeStatus nodeStatus = nodeStates.get(nodeUrn);
		if (nodeStatus == null) {
			log.warn("Received status for unknown node URN " + nodeUrn + "!");
			return;
		}

		nodeStatus.state = nodeState;
		nodeStatus.statusCode = statusCode;
		nodeStatus.message = message;
	}

	public Job(String requestId, List<String> nodeUrns, JobType jobType) {

		this.requestId = requestId;
		this.jobType = jobType;

		for (String nodeUrn : newHashSet(nodeUrns)) {
			nodeStates.put(nodeUrn, new NodeStatus(State.RUNNING, 0, "Starting..."));
		}
	}

	public Job(String requestId, String nodeId, JobType jobType) {
		this(requestId, newArrayList(nodeId), jobType);
	}

	public String getRequestId() {
		return requestId;
	}

	public void timeout() {
		for (JobResultListener l : listeners) {
			l.timeout();
		}
	}

	private boolean isDone(int value) {

		if (jobType == JobType.areNodesAlive) {
			return value == 1;
		} else if (jobType == JobType.resetNodes) {
			return value == 1;
		} else if (jobType == JobType.send) {
			return value == 1;
		} else if (jobType == JobType.flashPrograms) {
			return value == 100;
		} else if (jobType == JobType.setVirtualLink) {
			return value == 1;
		} else if (jobType == JobType.destroyVirtualLink) {
			return value == 1;
		} else if (jobType == JobType.disableNode) {
			return value == 1;
		} else if (jobType == JobType.enableNode) {
			return value == 1;
		} else if (jobType == JobType.disablePhysicalLink) {
			return value == 1;
		} else if (jobType == JobType.enablePhysicalLink) {
			return value == 1;
		} else if (jobType == JobType.setChannelPipeline) {
			return value == 1;
		}

		return false;
	}

	private boolean isError(int value) {

		if (jobType == JobType.areNodesAlive) {
			return value <= 0;
		} else if (jobType == JobType.resetNodes) {
			return value == 0 || value == -1;
		} else if (jobType == JobType.send) {
			return value == 0 || value == -1;
		} else if (jobType == JobType.flashPrograms) {
			return value < 0;
		} else if (jobType == JobType.setVirtualLink) {
			return value < 1;
		} else if (jobType == JobType.destroyVirtualLink) {
			return value < 1;
		} else if (jobType == JobType.disableNode) {
			return value < 1;
		} else if (jobType == JobType.enableNode) {
			return value < 1;
		} else if (jobType == JobType.disablePhysicalLink) {
			return value < 1;
		} else if (jobType == JobType.enablePhysicalLink) {
			return value < 1;
		} else if (jobType == JobType.setChannelPipeline) {
			return value < 1;
		}

		return false;
	}

	public State receive(RequestStatus status) {

		for (Status s : status.getStatus()) {

			State nodeState = State.RUNNING;
			if (isDone(s.getValue())) {
				nodeState = State.SUCCESS;
			} else if (isError(s.getValue())) {
				nodeState = State.FAILED;
			}

			updateNodeState(s.getNodeId(), nodeState, s.getValue(), s.getMsg());
		}

		State jobState = determineJobState();
		if (jobState != State.RUNNING) {
			notifyListeners(jobState);
		}
		return jobState;
	}

	private State determineJobState() {

		int running = 0;
		int success = 0;

		for (NodeStatus nodeStatus : nodeStates.values()) {
			switch (nodeStatus.state) {
				case RUNNING:
					running++;
					break;
				case SUCCESS:
					success++;
					break;
			}
		}

		if (running > 0) {
			return State.RUNNING;
		} else if (success == nodeStates.size()) {
			return State.SUCCESS;
		} else {
			return State.FAILED;
		}
	}

	public void addListener(JobResultListener listener) {
		listeners.add(listener);
	}

	public void removeListener(JobResultListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(State status) {
		for (JobResultListener l : listeners) {
			l.receiveJobResult(status);
		}
	}

	public JobType getJobType() {
		return jobType;
	}
}
