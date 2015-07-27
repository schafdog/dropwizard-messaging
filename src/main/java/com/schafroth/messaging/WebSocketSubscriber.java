package com.schafroth.messaging;


public class WebSocketSubscriber implements MessageHandler {
	private MessagingWebSocketEndpoint endpoint;
	public WebSocketSubscriber(MessagingWebSocketEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public void onMessage(String channel, String message) {
		try {
			endpoint.onMessage(channel, message);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to send message");
		}
	};
	
};
