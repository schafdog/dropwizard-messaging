package com.schafroth.messaging;

import io.dropwizard.lifecycle.Managed;

import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisSubscriber extends AbstractSubscriber implements Runnable, Managed {

	//private JedisPool pool;
	private Jedis sub; 
	private boolean running = false;
	private Thread thread = new Thread(this, "RedisSubscriberThread");
	private MessageHandler handler;
	private Logger logger = LoggerFactory.getLogger(RedisSubscriber.class);
	final long startMillis = System.currentTimeMillis();	

	RedisSubscriber(@Context JedisPool pool, MessageHandler handler) {
		//this.pool = pool;
		this.handler = handler;
		sub = pool.getResource();
	}
	
	public void setThreadName(String name) {
		thread.setName(name);
	}

	@Override
	public void run() {
		running = true;  
		while (running) {
			try {
				sub.subscribe(this, "message");
			} catch (Exception ex) {
				ex.printStackTrace();
				log("Exception in subscribe: " + ex.getMessage());
			}
		}
		log("Ending " + thread.getName());
	}

	@Override
	public void start() throws Exception {
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		running = false;
		try {
			sub.quit();
		} catch (Exception ex) {
			logger.warn("Caught Exception while shutting down. Ignoring: " + ex.getMessage());
		}
	}
	
	private void log(String string, Object... args) {
		long millisSinceStart = System.currentTimeMillis() - startMillis;
		logger.info(Thread.currentThread().getName() + " " + millisSinceStart + " " + String.format(string, args));
	}

	@Override
	public void onMessage(String channel, String message) 
	{
		try {
			handler.onMessage(channel, message);
		} catch (Exception ex) {
			logger.error("Failed to handle message " + message + " on channel " + channel);
		}
	}

}
