package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;

import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import redis.clients.jedis.Jedis;

import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {

    private final PersonDAO peopleDAO;

    public PeopleResource(PersonDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @UnitOfWork
    public Person createPerson(Person person, @Context Jedis jedis) {
    	return peopleDAO.create(person);
        //jedis.publish("person", newPerson.toString());
        //return person;
    }

    @GET
    @UnitOfWork
    public List<Person> listPeople() {
        return peopleDAO.findAll();
    }

}
