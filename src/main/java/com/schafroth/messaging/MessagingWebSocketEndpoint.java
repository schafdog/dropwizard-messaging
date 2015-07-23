package com.schafroth.messaging;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class MessagingWebSocketEndpoint {

	Session session; 
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingWebSocketEndpoint.class);
	RedisSubscriber subscriber;

	public MessagingWebSocketEndpoint() {
	}

	@OnWebSocketConnect
    public void onConncet(Session session) throws IOException {
		this.session = session;
		setupSubscriber("message");
	}
		
    private void setupSubscriber(String string) {
    	
	}

	@OnWebSocketMessage
    public void onMessage(Session session, String s) throws IOException {
		LOGGER.debug("Got Message on WebSocket: " + s + "(" + session + ")");
		session.getRemote().sendString("Returned; " + s);
    }

	@OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) throws IOException {
		LOGGER.debug("Closing WebSocket: " + this);
	}

}