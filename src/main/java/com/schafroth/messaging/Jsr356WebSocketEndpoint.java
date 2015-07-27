package com.schafroth.messaging;

import java.io.IOException;
import java.util.Date;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;

/* Handles one WebSocket connection, requiring a thread */

@ServerEndpoint(value = "/jsr356")
public class Jsr356WebSocketEndpoint implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jsr356WebSocketEndpoint.class);
	RedisSubscriber subscriber;
	Session session = null;
	
	public Jsr356WebSocketEndpoint()  {
		this.subscriber = new RedisSubscriber(JedisPoolFactory.getInstance().getPool(), this);
		try {
			subscriber.start();
		} catch (Exception ex) {
			LOGGER.error("Failed to start subscriber thread!");
			throw new RuntimeException("Failed to start subscriber");
		}
	}

	/* Not used. There are no way to build custom endpoint in JSR356 */ 	
	public Jsr356WebSocketEndpoint(JedisPool pool)  {
		this.subscriber = new RedisSubscriber(pool, this);
		try {
			subscriber.start();
		} catch (Exception ex) {
			LOGGER.error("Failed to start subscriber thread!");
			throw new RuntimeException("Failed to start subscriber");
		}
	};

	@OnOpen
    public void onConnect(Session session) throws IOException {
		LOGGER.info("Connected from session: " + session);
		this.session = session;
	}

	@OnMessage
    public void onMessage(String message, Session session) throws IOException {
		LOGGER.info("Got message: " + message + " on JSR " + session);
		if (session.isOpen()) {
			session.getAsyncRemote().sendText("Server time: " + new Date().getTime());
		}

	}

	@OnClose
    public void onClose(Session session) throws IOException {
		LOGGER.info("closing session: " + session);
	}
	
	public void onMessage(String channel, String message) throws IOException 
    {
    	if (session.isOpen())
    		session.getAsyncRemote().sendText(message);
    	else 
    		throw new IOException("JSR Session is closed");
    }
}