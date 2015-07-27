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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path("/message")
@Produces(MediaType.APPLICATION_JSON)
public class JsonResource {
	private static Logger LOGGER = LoggerFactory.getLogger(JsonResource.class);  
	JedisPool pool; 
    public JsonResource(JedisPool jedisPool) {
    	pool = jedisPool; 
    }

    /* 
     * REST service to publish new message on a Redis PubSub channel
     */  
    @POST
    public JsonNode create(JsonNode json) {
    	return createUpdate(json, UUID.randomUUID().toString());
    }

    /* 
     * REST service to publish new/update message on a Redis PubSub channel
     */  
	@PUT
    @Path("{id}")
    public JsonNode update(JsonNode json, @PathParam("id") String id) {
		return createUpdate(json, id);
    }

    private JsonNode createUpdate(JsonNode json, String id) {
    	((ObjectNode)json).put("UUID", id);
    	((ObjectNode)json).put("time", System.currentTimeMillis());
    	
    	try (Jedis jedis = pool.getResource()) {
    		jedis.publish("message", json.toString());
    		System.out.println("Active: " + pool.getNumActive());
    		return json;
    	}
	}

    /* REST service to list persisted messages 
     *  
     */
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
    				String value = null;
    				try {
    					value = jedis.get(key);
    					LOGGER.info("Redis: " + key + "=" + value);
    					results.add(m.readTree(value));
    				} catch (JedisDataException e) {
    					LOGGER.error("Failed to parse key " + key + " or " + value, e);
    				} catch (JsonProcessingException e) {
    					LOGGER.error("Failed to JSon Process key " + key + " or " + value, e);
    				} catch (IOException e) {
    					LOGGER.error("IOException key " + key + " or " + value, e);
    				}
    			}
    		}
    		while (cursor != null && !cursor.equals("0"));
    	}
    	return results;
    }

}
