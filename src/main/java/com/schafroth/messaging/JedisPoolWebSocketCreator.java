package com.schafroth.messaging;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import redis.clients.jedis.JedisPool;

public class JedisPoolWebSocketCreator implements WebSocketCreator {
	
	private JedisPool pool; 
	public JedisPoolWebSocketCreator(JedisPool pool) {
		this.pool = pool;
	}

	@Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        for (String subprotocol : req.getSubProtocols()) {
            if ("binary".equals(subprotocol)) {
                // resp.setAcceptedSubProtocol(subprotocol);
                return null;
            }
        }
        //resp.setAcceptedSubProtocol("text");
        return new MessagingWebSocketEndpoint(pool);
    }
}
