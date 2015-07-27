package com.schafroth.messaging;

import redis.clients.jedis.JedisPool;

public class JedisPoolFactory {
	
	private static JedisPoolFactory instance;
	private JedisPool pool;

	public synchronized static JedisPoolFactory getInstance() {
		if (instance == null)
			instance = new JedisPoolFactory();
		return instance;
	}

	public void setPool(JedisPool pool) {
		this.pool = pool;
	}
	public JedisPool getPool() {
		return pool;
	}

}
