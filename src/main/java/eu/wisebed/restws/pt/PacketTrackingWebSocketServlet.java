package eu.wisebed.restws.pt;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniluebeck.itm.tr.util.ExecutorUtils;
import de.uniluebeck.itm.tr.util.ForwardingScheduledExecutorService;
import eu.wisebed.restws.util.InjectLogger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class PacketTrackingWebSocketServlet extends WebSocketServlet {

	@InjectLogger
	private Logger log;

	private ScheduledExecutorService schedulerExecutor;

	private ExecutorService workerExecutor;

	private ForwardingScheduledExecutorService scheduler;

	private final PacketTrackingWebSocketFactory packetTrackingWebSocketFactory;

	@Inject
	public PacketTrackingWebSocketServlet(final PacketTrackingWebSocketFactory packetTrackingWebSocketFactory) {
		this.packetTrackingWebSocketFactory = packetTrackingWebSocketFactory;
	}

	@Override
	public WebSocket doWebSocketConnect(final HttpServletRequest request, final String protocol) {
		return packetTrackingWebSocketFactory.create(scheduler);
	}

	@Override
	public void init() throws ServletException {

		super.init();

		log.debug("PacketTrackingWebSocketServlet.init()");

		this.schedulerExecutor = Executors.newScheduledThreadPool(1);
		this.workerExecutor = Executors.newCachedThreadPool();
		this.scheduler = new ForwardingScheduledExecutorService(schedulerExecutor, workerExecutor);
	}

	@Override
	public void destroy() {

		log.debug("PacketTrackingWebSocketServlet.destroy()");

		ExecutorUtils.shutdown(this.schedulerExecutor, 1, TimeUnit.SECONDS);
		ExecutorUtils.shutdown(this.workerExecutor, 1, TimeUnit.SECONDS);
		ExecutorUtils.shutdown(this.scheduler, 1, TimeUnit.SECONDS);
	}
}
