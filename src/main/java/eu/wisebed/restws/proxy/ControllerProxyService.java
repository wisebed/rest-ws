package eu.wisebed.restws.proxy;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import eu.wisebed.api.common.Message;
import eu.wisebed.api.controller.Controller;
import eu.wisebed.api.controller.RequestStatus;
import eu.wisebed.restws.WisebedRestServerConfig;
import eu.wisebed.restws.jobs.JobObserver;
import eu.wisebed.restws.util.Base64Helper;
import org.joda.time.DateTime;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.util.List;

@WebService(serviceName = "ControllerService", targetNamespace = "urn:ControllerService",
		portName = "ControllerPort", endpointInterface = "eu.wisebed.api.controller.Controller")
public class ControllerProxyService extends AbstractService implements Controller {

	private final JobObserver jobObserver;

	private final WisebedRestServerConfig config;

	private final AsyncEventBus asyncEventBus;

	private final String experimentWsnInstanceEndpointUrl;

	private Endpoint endpoint;

	private String endpointUrl;

	@Inject
	public ControllerProxyService(final WisebedRestServerConfig config,
								  @Assisted final JobObserver jobObserver,
								  @Assisted final String experimentWsnInstanceEndpointUrl,
								  @Assisted final AsyncEventBus asyncEventBus) {

		this.jobObserver = jobObserver;
		this.config = config;
		this.experimentWsnInstanceEndpointUrl = experimentWsnInstanceEndpointUrl;
		this.asyncEventBus = asyncEventBus;
		this.endpointUrl = constructEndpointUrl();
	}

	@Override
	public void experimentEnded() {
		asyncEventBus.post(new ExperimentEndedEvent());
	}

	@Override
	public void receive(@WebParam(name = "msg", targetNamespace = "") final List<Message> messages) {
		for (Message message : messages) {
			asyncEventBus.post(new UpstreamMessageEvent(
					new DateTime(message.getTimestamp().toGregorianCalendar()),
					message.getSourceNodeId(),
					message.getBinaryData()
			)
			);
		}
	}

	@Override
	public void receiveNotification(@WebParam(name = "msg", targetNamespace = "") final List<String> notifications) {
		asyncEventBus.post(new NotificationsEvent(notifications));
	}

	@Override
	public void receiveStatus(
			@WebParam(name = "status", targetNamespace = "") final List<RequestStatus> requestStatuses) {
		jobObserver.process(requestStatuses);
	}

	@Override
	protected void doStart() {
		try {
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
