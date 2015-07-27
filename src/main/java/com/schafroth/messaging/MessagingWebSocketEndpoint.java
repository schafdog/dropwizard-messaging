package com.schafroth.messaging;

import java.io.IOException;
import java.util.Date;

import javax.websocket.OnMessage;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;

/* Handles one WebSocket connection */

@WebSocket
public class MessagingWebSocketEndpoint implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingWebSocketEndpoint.class);
	RedisSubscriber subscriber;
	Session session = null;
	
	public MessagingWebSocketEndpoint()  {
		this.subscriber = new RedisSubscriber(JedisPoolFactory.getInstance().getPool(), this);
		try {
			//subscriber.start();
		} catch (Exception ex) {
			LOGGER.error("Failed to start subscriber thread!");
			throw new RuntimeException("Failed to start subscriber");
		}
	}

		
	public MessagingWebSocketEndpoint(JedisPool pool)  {
		this.subscriber = new RedisSubscriber(pool, this);
		try {
			//subscriber.start();
		} catch (Exception ex) {
			LOGGER.error("Failed to start subscriber thread!");
			throw new RuntimeException("Failed to start subscriber");
		}
	};

	@OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
		try {
			this.session = session;
			subscriber.start();
			//sessions.add(session);
		} catch (Exception ex) {
			throw new IOException("Failed to start subscriber");
		}
	}
		
    public void onMessage(String channel, String message) throws IOException 
    {
    	if( session.isOpen())
    		session.getRemote().sendStringByFuture(message);
    	else 
    		throw new IOException("Session is closed");
    		
    }
    
	@OnWebSocketMessage
    public void onText(Session session, String s) throws IOException {
		LOGGER.debug("Got Message on WebSocket: " + s + "(" + session + ")" + " Endpoint: " + this);
		if (session.isOpen()) {
			session.getRemote().sendString("Server time: " + new Date().getTime());
		}
    }

	@OnMessage
    public void onMessage(String s, Session session) throws IOException {
		LOGGER.debug("Got Message on WebSocket: " + s + "(" + session + ")");
		if (session.isOpen()) {
			session.getRemote().sendString("Server time: " + new Date().getTime());
		}
    }

	@OnWebSocketMessage
    public void onMessage(Session session, byte buf[], int offset, int length) throws IOException {
		
		LOGGER.debug("Got Message on WebSocket: " + buf + "(" + session + ")");
		//session.getRemote().sendString(buf);
    }

	@OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) throws IOException {
		LOGGER.debug("Closing WebSocket: " + this);
		session = null;
		try {
			subscriber.stop();
		} catch (Exception ex) {
			LOGGER.error("Failed to stop subscriber",ex);
		}
	}

	@OnWebSocketFrame
	public void onWebSocketFrame(Session session, Frame frame) 
	{
		LOGGER.debug("Got WebSocket Frame " + frame);
	}
}