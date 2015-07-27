package com.schafroth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractSubscriber extends JedisPubSub {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		logger.debug("onUnsubscribe");
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		logger.debug("onSubscribe");
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.debug("onPUnsubscribe");
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.debug("onPSubscribe");
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		logger.debug("onPMessage");
	}

	@Override
	abstract public void onMessage(String channel, String message);

	public void logger(String channel, String message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.readTree(message);
			logger.debug("Channel " + channel + ": " + message);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to parse JSON: " + message, ex);
		}
	};
	
};
