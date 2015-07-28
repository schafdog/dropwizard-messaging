package com.schafroth.messaging;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import redis.clients.jedis.JedisPool;

/* 
 * Handles multiple WebSocket connections using one RedisSubscriber 
 * and Map for open connections
 */

@WebSocket
public class MultiWebSocketEndpoint implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiWebSocketEndpoint.class);
    
    RedisSubscriber subscriber;
    Map<Session, Date> sessionMap = new ConcurrentHashMap<Session, Date>();
	
	public MultiWebSocketEndpoint(JedisPool pool)  {
		this.subscriber = new RedisSubscriber(pool, this);
		try {
			subscriber.start();
		} catch (Exception ex) {
			LOGGER.error("Failed to start subscriber thread!");
			throw new RuntimeException("Failed to start subscriber");
		}
	};

	@OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
		LOGGER.debug("Connect WebSocket " + session.getRemoteAddress());
		sessionMap.put(session, new Date());
	}
		
    public void onMessage(String channel, String message) throws IOException 
    {
    	for (Session session : sessionMap.keySet()) {
    		if(session.isOpen()) {
    			LOGGER.debug("Message: " + message + " on " + session.getRemoteAddress());
    			session.getRemote().sendStringByFuture(message);
    		}
    		else 
    			LOGGER.error("Session is closed: " + session.getRemoteAddress());
    	}
    }
    
	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
		LOGGER.info("Got Message on WebSocket: " + message + "(" + session.getRemoteAddress() + ")" + " Endpoint: " + this);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(message);
			((ObjectNode) rootNode).put("time", new Date().getTime());
			((ObjectNode) rootNode).put("from", session.getRemoteAddress().toString());
			onMessage("broadcast", rootNode.toString());  
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Failed to JSON parse message: " + message);
		}
	}

	@OnWebSocketClose
    public void onClosed(Session session, int statusCode, String reason) {
		Date date = sessionMap.remove(session);
		reason = (reason != null ? reason : "-");
		if (date != null)
			LOGGER.debug("Closing WebSocket (" + session.getRemoteAddress() +" " + statusCode  + " " + reason + ") after " + (new Date().getTime() - date.getTime())/1000 + " secs");
		else {
			LOGGER.error("onClose: Unable to find session in map: " + session.getRemoteAddress() + " Status code" + statusCode + " Reason: " + reason);
		}
	}
}