package com.schafroth.messaging.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.databind.JsonNode;
import com.schafroth.messaging.core.Message;


/**
 * Unit tests for {@link PersonResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PersonResourceTest {
    private static final Jedis jedis = mock(Jedis.class);
    private static final JedisPool jedisPool = mock(JedisPool.class);
    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new JsonResource(jedisPool))
            //.setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .build();
    private Message message;

    @Before
    public void setup() {
        message = new Message();
        message.setId("1");
        message.setPayload("3.145");
    }

    @After
    public void tearDown() {
        reset(jedisPool);
        reset(jedis);
    }

	@Test
    public void postSuccess() {

    	when(jedisPool.getResource()).thenReturn(jedis);
    	when(jedis.publish(anyString(), (String) argThat(new ArgumentMatcher<String>() {
    		public boolean matches(Object msgObject) {
    			return msgObject instanceof String && msgObject.equals(message);
    		}
    	}))).thenReturn(1l);

        final Response response = RULE.client().target("/message")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));
        //JsonNode found = RULE.getJerseyTest().target("/message/").request().post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonNode found = response.readEntity(JsonNode.class);
        assertThat(found.path("payload").asText()).isEqualTo(message.getPayload());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void postFailed() {
        when(jedis.publish(anyString(), anyString())).thenThrow(RuntimeException.class);
        final Response response = RULE.getJerseyTest().target("/people/2").request().get();

        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
