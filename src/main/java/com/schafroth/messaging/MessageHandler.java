package com.schafroth.messaging;

import java.io.IOException;

public interface MessageHandler {

	void onMessage(String channel, String message) throws IOException;
	
}
