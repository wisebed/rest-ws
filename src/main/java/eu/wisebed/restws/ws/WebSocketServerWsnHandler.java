package eu.wisebed.restws.ws;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.SocketAddress;

public class WebSocketServerWsnHandler extends SimpleChannelUpstreamHandler implements LifeCycleAwareChannelHandler {

	private final int experimentId;

	public WebSocketServerWsnHandler(final int experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public void beforeAdd(final ChannelHandlerContext ctx) throws Exception {
		// TODO connect to WSN as Controller to receive messages or throw Exception if not possible
	}

	@Override
	public void afterAdd(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}

	@Override
	public void beforeRemove(final ChannelHandlerContext ctx) throws Exception {
		// TODO unregister from WSN to not receive messages anymore
	}

	@Override
	public void afterRemove(final ChannelHandlerContext ctx) throws Exception {
		// nothing to do
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		if (!(e.getMessage() instanceof TextWebSocketFrame)) {
			ctx.sendUpstream(e);
		}

		TextWebSocketFrame frame = (TextWebSocketFrame) e.getMessage();

		TextWebSocketFrame echoResponse = new TextWebSocketFrame("Echo=" + frame.getText());
		sendFrameDownstream(ctx, echoResponse);

		// TODO send message to sensor node
	}

	private void sendFrameDownstream(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) {
		Channel channel = ctx.getChannel();
		DefaultChannelFuture future = new DefaultChannelFuture(channel, true);
		SocketAddress remoteAddress = channel.getRemoteAddress();
		ctx.sendDownstream(new DownstreamMessageEvent(channel, future, frame, remoteAddress));
	}
}
