package eu.wisebed.restws;

import eu.wisebed.api.controller.Status;

public interface OperationStatus {

	void put(String requestId, Status status);
	
	Status get(String requestId);
}
