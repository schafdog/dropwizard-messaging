package com.example.helloworld;

import javax.ws.rs.core.Context;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import io.dropwizard.lifecycle.Managed;

public class RedisSubscriber implements Runnable, Managed {

	private JedisPool pool;
	private Jedis sub; 
	private boolean running = false;
	private Thread thread;
	
	RedisSubscriber(@Context JedisPool pool) {
		this.pool = pool;
		sub = pool.getResource();
	}
	static final long startMillis = System.currentTimeMillis();	

	void setThread(Thread thread) {
		this.thread = thread;
	}
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
			}

			@Override
			public void onPSubscribe(String pattern, int subscribedChannels) {
			}

			@Override
			public void onPMessage(String pattern, String channel, String message) {
			}

			@Override
			public void onMessage(String channel, String message) {
				try (Jedis jedis = pool.getResource()) {
					// Store message 
					jedis.set(message, message);
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
				pool.getResource().subscribe(setupSubscriber(), "person");
			} catch (Exception ie) {
				
			}
		}
	}

	@Override
	public void start() throws Exception {
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		running = false;
		sub.quit();
		thread.join();
		pool.returnResource(sub);
	}
	
	private static void log(String string, Object... args) {
		long millisSinceStart = System.currentTimeMillis() - startMillis;
		System.out.printf("%20s %6d %s\n", Thread.currentThread().getName(), millisSinceStart,
				String.format(string, args));
	}

}
