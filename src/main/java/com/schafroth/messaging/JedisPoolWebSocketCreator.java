package com.schafroth.messaging;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import redis.clients.jedis.JedisPool;

public class JedisPoolWebSocketCreator implements WebSocketCreator {
	
	MultiWebSocketEndpoint endpoint;
	
	public JedisPoolWebSocketCreator(JedisPool pool) {
		endpoint = new MultiWebSocketEndpoint(pool);
	}

	@Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        for (String subprotocol : req.getSubProtocols()) {
            if ("binary".equals(subprotocol)) {
                return null;
            }
        }
        // Return singleton since it handles multiple endpoints
        return endpoint;
    }
}
