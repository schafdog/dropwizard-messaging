package com.schafroth.messaging;

import io.dropwizard.lifecycle.Managed;

import javax.ws.rs.core.Context;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import ch.qos.logback.classic.Logger;

public class RedisSubscriber implements Runnable, Managed {

	private JedisPool pool;
	private Jedis sub; 
	private boolean running = false;
	private Thread thread = new Thread(this, "RedisSubscriberThread");
	private Logger logger;
	RedisSubscriber(@Context JedisPool pool, Logger logger) {
		this.pool = pool;
		this.logger = logger;
		sub = pool.getResource();
	}
	static final long startMillis = System.currentTimeMillis();	

	private JedisPubSub setupSubscriber() {
		return new MessageJedisPubSub(pool);
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
