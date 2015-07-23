package com.example.helloworld.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.UUID;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.example.helloworld.core.Person;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Unit tests for {@link PersonResource}.
 */
public class PersonResourceTest {
    private static final Jedis jedis = mock(Jedis.class);
    private static final JedisPool jedisPool = mock(JedisPool.class);
    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new JsonResource(jedisPool))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .build();
    private Person person;

    @Before
    public void setup() {
        person = new Person();
        person.setId(1L);
    }

    @After
    public void tearDown() {
        reset(jedisPool);
        reset(jedis);
    }

	@Test
    public void postSuccess() {
    	String channel = "channel";
    	final String message = UUID.randomUUID().toString();
    	when(jedisPool.getResource()).thenReturn(jedis);
    	when(jedis.publish(channel, (String) argThat(new ArgumentMatcher<String>() {
    		public boolean matches(Object msgObject) {
    			return msgObject instanceof String && msgObject.equals(message);
    		}
    	}))).thenReturn(1l);

    	/*
        final Response response = RULE.client().target("/message")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));
        */
        JsonNode found = RULE.getJerseyTest().target("/message/1").request().get(JsonNode.class);

        assertThat(found.path("UUID")).isEqualTo(message);
        verify(jedis).get("1");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void postFailed() {
        when(jedis.publish(anyString(), anyString())).thenThrow(RuntimeException.class);
        final Response response = RULE.getJerseyTest().target("/people/2").request().get();

        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(jedis).get("2");
    }
}
