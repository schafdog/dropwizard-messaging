package com.schafroth.messaging;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import redis.clients.jedis.JedisPool;

public class MessagingWebSocketServlet extends WebSocketServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9152808383213487609L;
	private JedisPool pool;
	
	public MessagingWebSocketServlet(JedisPool pool) {
		this.pool = pool;
	}
	
	
	@Override
	public void configure(WebSocketServletFactory factory) {
		//factory.register(MessagingWebSocketEndpoint.class);		
		factory.setCreator(new JedisPoolWebSocketCreator(pool));
	}
	
	
};
