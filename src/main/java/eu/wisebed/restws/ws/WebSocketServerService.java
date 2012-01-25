package eu.wisebed.restws.ws;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;

import eu.wisebed.restws.CommandLineOptions;
import eu.wisebed.restws.util.InjectLogger;

public class WebSocketServerService extends AbstractService {

	@InjectLogger
	private Logger log;

	@Inject
	private CommandLineOptions commandLineOptions;

	private ExecutorService bossExecutor;

	private ExecutorService workerExecutor;

	private Channel serverChannel;

	@Override
	protected void doStart() {

		try {
			bossExecutor = Executors.newCachedThreadPool();
			workerExecutor = Executors.newCachedThreadPool();
			ServerBootstrap bootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(bossExecutor, workerExecutor)
			);
			bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());
			serverChannel = bootstrap.bind(new InetSocketAddress(commandLineOptions.webSocketPort));
		} catch (Exception e) {
			notifyFailed(e);
		}

		notifyStarted();
	}

	@Override
	protected void doStop() {

		try {
			serverChannel.close().await();
		} catch (Exception e) {
			log.error("{}", e);
		}

		ExecutorUtil.terminate(bossExecutor, workerExecutor);

		notifyStopped();
	}
}
