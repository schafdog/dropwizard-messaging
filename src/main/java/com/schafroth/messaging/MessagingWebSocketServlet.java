package com.schafroth.messaging;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MessagingWebSocketServlet extends WebSocketServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9152808383213487609L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(MessagingWebSocketEndpoint.class);
	}
	
	
};
