package com.example.helloworld;

import io.dropwizard.lifecycle.Managed;

import javax.ws.rs.core.Context;

import org.apache.commons.lang3.NotImplementedException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisSubscriber implements Runnable, Managed {

	private JedisPool pool;
	private Jedis sub; 
	private boolean running = false;
	private Thread thread = new Thread(this, "RedisSubscriberThread");
	
	RedisSubscriber(@Context JedisPool pool) {
		this.pool = pool;
		sub = pool.getResource();
	}
	static final long startMillis = System.currentTimeMillis();	

	private JedisPubSub setupSubscriber() {
		final JedisPubSub jedisPubSub = new JedisPubSub() {
			@Override
			public void onUnsubscribe(String channel, int subscribedChannels) {
				log("onUnsubscribe");
			}

			@Override
			public void onSubscribe(String channel, int subscribedChannels) {
				log("onSubscribe");
			}

			@Override
			public void onPUnsubscribe(String pattern, int subscribedChannels) {
				throw new NotImplementedException("onPUnsubscribe not implemented");
			}

			@Override
			public void onPSubscribe(String pattern, int subscribedChannels) {
				throw new NotImplementedException("onPSubscribe not implemented");
			}

			@Override
			public void onPMessage(String pattern, String channel, String message) {
				throw new NotImplementedException("onPMessage not implemented");
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
		return jedisPubSub;
	};
		
	@Override
	public void run() {
		running = true;  
		while (running) {
			try {
				sub.subscribe(setupSubscriber(), "person");
			} catch (Exception ex) {
				log("Exception in subscribe: " + ex.getMessage(), ex);
			}
		}
		log("Ending RedisSubscriber");
	}

	@Override
	public void start() throws Exception {
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		running = false;
	}
	
	private static void log(String string, Object... args) {
		long millisSinceStart = System.currentTimeMillis() - startMillis;
		System.out.printf("%20s %6d %s\n", Thread.currentThread().getName(), millisSinceStart,
				String.format(string, args));
	}

}
