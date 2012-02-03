package eu.wisebed.restws.event;

public class ExperimentEndedEvent {

	private final String experimentWsnInstanceEndpointUrl;

	public ExperimentEndedEvent(final String experimentWsnInstanceEndpointUrl) {
		this.experimentWsnInstanceEndpointUrl = experimentWsnInstanceEndpointUrl;
	}

	public String getExperimentWsnInstanceEndpointUrl() {
		return experimentWsnInstanceEndpointUrl;
	}
}
