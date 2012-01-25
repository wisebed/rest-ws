/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.wisebed.restws.ws;

import static com.google.common.base.Throwables.propagate;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;

import com.google.common.primitives.Ints;

import eu.wisebed.restws.util.InjectLogger;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {

	@InjectLogger
	private Logger log;

	private WebSocketServerHandshaker handshaker;

	private int experimentId;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, e);
		}
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

		if (e instanceof MessageEvent) {
			messageReceived(ctx, (MessageEvent) e);
		}

		// swallow all other events to fire them according to the websocket channel state
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {

		// Allow only GET methods.
		if (req.getMethod() != GET) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		String uriString = req.getUri();
		URI requestUri;
		try {
			requestUri = new URI(uriString);
		} catch (URISyntaxException e) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		String path = requestUri.getPath().startsWith("/") ? requestUri.getPath().substring(1) : requestUri.getPath();
		String[] splitPath = path.split("/");

		if (splitPath.length < 1 || !"experiments".equals(splitPath[0]) || Ints.tryParse(splitPath[1]) == null) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		experimentId = Integer.parseInt(splitPath[1]);

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(requestUri.toString(), null, false);
		this.handshaker = wsFactory.newHandshaker(req);
		if (this.handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
		} else {
			try {
				this.handshaker.performOpeningHandshake(ctx.getChannel(), req);
				try {
					ctx.getPipeline().addAfter("webSocketServerHandler", "webSocketServerWsnHandler", new WebSocketServerWsnHandler(experimentId));
				} catch (Exception e) {
					// TODO send some error message to the client and close channel afterwards
					throw new RuntimeException(e);
				}
				ctx.sendUpstream(new UpstreamChannelStateEvent(
						ctx.getChannel(),
						ChannelState.CONNECTED,
						ctx.getChannel().getRemoteAddress()
				)
				);
			} catch (Exception e) {
				throw propagate(e);
			}
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, MessageEvent e) {

		WebSocketFrame frame = (WebSocketFrame) e.getMessage();

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {

			this.handshaker.performClosingHandshake(ctx.getChannel(), (CloseWebSocketFrame) frame);
			ctx.sendUpstream(new UpstreamChannelStateEvent(ctx.getChannel(), ChannelState.CONNECTED, null));
			try {
				ctx.getPipeline().remove("webSocketServerWsnHandler");
			} catch (Exception e1) {
				log.error("Exception while remove WSN handler from pipeline: " + e1, e1);
			}
			return;

		} else if (frame instanceof PingWebSocketFrame) {

			ctx.sendDownstream(
					new DownstreamMessageEvent(
							ctx.getChannel(),
							new DefaultChannelFuture(ctx.getChannel(), true),
							new PongWebSocketFrame(frame.getBinaryData()),
							ctx.getChannel().getRemoteAddress()
					)
			);
			return;

		} else if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
					.getName()
			)
			);
		}

		ctx.sendUpstream(e);
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
		// Generate an error page if response status code is not OK (200).
		if (res.getStatus().getCode() != 200) {
			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
			setContentLength(res, res.getContent().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.getChannel().write(res);
		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace();
		e.getChannel().close();
		// TODO do some sensible exception handling
	}
}