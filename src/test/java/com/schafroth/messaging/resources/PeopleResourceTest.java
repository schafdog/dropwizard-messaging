package com.schafroth.messaging.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.schafroth.messaging.resources.JsonResource;

/**
 * Unit tests for {@link JsonResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleResourceTest {
    private static final JedisPool pool = mock(JedisPool.class);
    private static final Jedis jedis = mock(Jedis.class);
    private static final JsonResource jsonResource = new JsonResource(pool);
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(jsonResource)
            .build();
    @Captor
    private ArgumentCaptor<JsonNode> messageCaptor;
    private JsonNode message;
    String uuid = UUID.randomUUID().toString();
    String payload = UUID.randomUUID().toString();;
    @Before
    public void setUp() {
        ObjectMapper m = new ObjectMapper();
        message = m.createObjectNode();
        ((ObjectNode) message).put("UUID", uuid);
        ((ObjectNode) message).put("message", payload);
    }

    @After
    public void tearDown() {
        reset(pool);
    }

    @Test
    public void create() throws JsonProcessingException {
    	String channel = "channel";
    	String stringmmsg = message.asText();
        when(pool.getResource()).thenReturn(jedis);
        when(jedis.publish(channel, stringmmsg)).thenReturn(1l);
        final Response response = resources.client().target("/message")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(jsonResource).create(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo(message);
    }

    @Test
    public void list() throws Exception {
        final ImmutableList<JsonNode> messageList = ImmutableList.of(message);

        final List<JsonNode> response = resources.client().target("/message")
                .request().get(new GenericType<List<JsonNode>>() {});

        verify(jsonResource).list();
        assertThat(response).containsAll(messageList);
    }
}
