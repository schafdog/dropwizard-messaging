package com.schafroth.messaging.resources;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON)
public class JsonResource {
	JedisPool pool; 
    public JsonResource(JedisPool jedisPool) {
    	pool = jedisPool; 
    }

    @POST
    public JsonNode create(JsonNode json) {
    	((ObjectNode)json).put("UUID", UUID.randomUUID().toString());
    	((ObjectNode)json).put("time", System.currentTimeMillis());
    	
    	try (Jedis jedis = pool.getResource()) {
    		jedis.publish("person", json.toString());
    		System.out.println("Active: " + pool.getNumActive());
    		return json;
    	}
    }

    @PUT
    @Path("{id}")
    public JsonNode update(JsonNode json, @PathParam("id") String id) {
    	((ObjectNode)json).put("UUID", id);
    	((ObjectNode)json).put("time", System.currentTimeMillis());
    	
    	try (Jedis jedis = pool.getResource()) {
    		jedis.publish("person", json.toString());
    		System.out.println("Active: " + pool.getNumActive());
    		return json;
    	}
    }

    @GET
    public List<JsonNode> list() 
    {
    	List<JsonNode> results = new LinkedList<JsonNode>();
    	try (Jedis jedis = pool.getResource()) {
    		String cursor = "0";
    		ObjectMapper m = new ObjectMapper();
    		do {
    			ScanResult<String> scan = jedis.scan(cursor);
    			cursor = scan.getStringCursor();
    			for (String key : scan.getResult()) {
    				try {
    					String value = jedis.get(key);
    					results.add(m.readTree(value));
    				} catch (JsonProcessingException e) {
    					e.printStackTrace();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		while (cursor != null && !cursor.equals("0"));
    	}
    	return results;
    }

}
