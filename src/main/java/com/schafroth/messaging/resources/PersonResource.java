package com.schafroth.messaging.resources;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.JedisPool;

import com.google.common.base.Optional;
import com.schafroth.messaging.core.Message;
import com.schafroth.messaging.views.MessageView;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private JedisPool jedisPool; 
    public PersonResource(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @GET
    @UnitOfWork
    public Message getPerson(@PathParam("personId") LongParam personId) {
        return findSafely(personId.get());
    }

    @GET
    @Path("/view_freemarker")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public MessageView getPersonViewFreemarker(@PathParam("personId") LongParam personId) {
        return new MessageView(MessageView.Template.FREEMARKER, findSafely(personId.get()));
    }

    @GET
    @Path("/view_mustache")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public MessageView getPersonViewMustache(@PathParam("personId") LongParam personId) {
        return new MessageView(MessageView.Template.MUSTACHE, findSafely(personId.get()));
    }

    private Message findSafely(long personId) {
    	final Optional<Message> person = null; // = jedis.get(personId);
        if (!person.isPresent()) {
            throw new NotFoundException("No such user.");
        }
        return person.get();
    }
}
