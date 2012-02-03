package eu.wisebed.restws.proxy;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.controller.Controller;
import eu.wisebed.api.controller.RequestStatus;
import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.event.ExperimentEndedEvent;
import eu.wisebed.restws.event.NotificationsEvent;
import eu.wisebed.restws.event.UpstreamMessageEvent;
import eu.wisebed.restws.jobs.JobObserver;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.InjectLogger;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.util.List;

@WebService(serviceName = "ControllerService", targetNamespace = "urn:ControllerService",
		portName = "ControllerPort", endpointInterface = "eu.wisebed.api.controller.Controller")
public class ControllerProxyService extends AbstractService implements Controller {

	@InjectLogger
	private Logger log;

	private final WsnProxyManagerService wsnProxyManagerService;

	private final JobObserver jobObserver;

	private final WisebedRestServerConfig config;

	private final String experimentWsnInstanceEndpointUrl;

	private Endpoint endpoint;

	private String endpointUrl;

	@Inject
	public ControllerProxyService(final WisebedRestServerConfig config,
								  final WsnProxyManagerService wsnProxyManagerService,
								  @Assisted final JobObserver jobObserver,
								  @Assisted final String experimentWsnInstanceEndpointUrl) {

		this.config = config;
		this.wsnProxyManagerService = wsnProxyManagerService;
		this.jobObserver = jobObserver;
		this.experimentWsnInstanceEndpointUrl = experimentWsnInstanceEndpointUrl;
		this.endpointUrl = constructEndpointUrl();
	}

	@Override
	public void experimentEnded() {
		ExperimentEndedEvent event = new ExperimentEndedEvent(experimentWsnInstanceEndpointUrl);
		getEventBus().post(event);
	}

	private EventBus getEventBus() {
		return wsnProxyManagerService.getEventBus(experimentWsnInstanceEndpointUrl);
	}

	@Override
	public void receive(@WebParam(name = "msg", targetNamespace = "") final List<Message> messages) {
		for (Message message : messages) {
			getEventBus().post(new UpstreamMessageEvent(
					new DateTime(message.getTimestamp().toGregorianCalendar()),
					message.getSourceNodeId(),
					message.getBinaryData()
			)
			);
		}
	}

	@Override
	public void receiveNotification(@WebParam(name = "msg", targetNamespace = "") final List<String> notifications) {
		getEventBus().post(new NotificationsEvent(notifications));
	}

	@Override
	public void receiveStatus(
			@WebParam(name = "status", targetNamespace = "") final List<RequestStatus> requestStatuses) {
		jobObserver.process(requestStatuses);
	}

	@Override
	protected void doStart() {
		try {
			log.info("Starting SOAP controller endpoint on " + endpointUrl);
			endpoint = Endpoint.publish(endpointUrl, this);
			notifyStarted();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	@Override
	protected void doStop() {
		try {
			endpoint.stop();
			notifyStopped();
		} catch (Exception e) {
			notifyFailed(e);
		}
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	private String constructEndpointUrl() {
		return "http://" + config.webServerHostname + ":" + config.webServerPort + "/soap/controller/"
				+ Base64Helper.encode(experimentWsnInstanceEndpointUrl);
	}
}
