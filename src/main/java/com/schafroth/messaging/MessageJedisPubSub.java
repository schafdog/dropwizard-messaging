package com.schafroth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageJedisPubSub extends JedisPubSub {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private JedisPool pool;
	
	public MessageJedisPubSub(JedisPool pool) {
		this.pool = pool;
	}
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
	public void onMessage(String channel, String message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(message);
			try (Jedis jedis = pool.getResource()) {
				// Store message
				jedis.set(rootNode.path("UUID").asText(), message);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to parse JSON: " + message, ex);
		}
	};
	
};
