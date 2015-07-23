package com.schafroth.messaging.resources;

import com.schafroth.messaging.core.Message;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {

	JedisPool jedisPool;
    public PeopleResource(JedisPool pool) {
        this.jedisPool = jedisPool;
    }

    @POST
    @UnitOfWork
    public Message createPerson(Message message) {
    	try (Jedis jedis = jedisPool.getResource()) {
    		jedis.set(String.valueOf(message.getId()), message.getPayload());
    	}
        //jedis.publish("person", newPerson.toString());
        return message;
    }

    @GET
    @UnitOfWork
    public List<Message> listPeople() {
    	// TODO 
    	return null;
    }

}
