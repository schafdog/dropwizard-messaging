package com.schafroth.messaging.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.LinkedList;
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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;


/**
 * Unit tests for {@link PersonResource}.
 */

//@RunWith(MockitoJUnitRunner.class)
public class JsonResourceTest {
    private static final JedisPool pool = mock(JedisPool.class);
    private static final JsonResource jsonResource = new JsonResource(pool);
    @Captor
    private ArgumentCaptor<JsonNode> messageCaptor;
    private JsonNode message;
    String uuid = UUID.randomUUID().toString();
    String payload = UUID.randomUUID().toString();;

	
	private static final Jedis jedis = mock(Jedis.class);
    private static final JedisPool jedisPool = mock(JedisPool.class);
    @SuppressWarnings("unchecked")
	private static final ScanResult<String> scanResult = (ScanResult<String>) mock(ScanResult.class);
    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(jsonResource)
            //.setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .build();

    @Before
    public void setup() {
        ObjectMapper m = new ObjectMapper();
        message = m.createObjectNode();
        ((ObjectNode) message).put("UUID", uuid);
        ((ObjectNode) message).put("payload", payload);
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
    			return msgObject instanceof String && msgObject.equals(message.toString());
    		}
    	}))).thenReturn(1l);

        final Response response = RULE.client().target("/message")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        //JsonNode found = RULE.getJerseyTest().target("/message/1").request().get(JsonNode.class);
        JsonNode responseNode = response.readEntity(JsonNode.class);
        assertThat(responseNode.path("payload")).isEqualTo(message.path("payload"));
    }
	
    @Test
    public void create() throws JsonProcessingException {
    	String channel = "channel";
    	String stringmmsg = message.toString();
        when(pool.getResource()).thenReturn(jedis);
        when(jedis.publish(channel, stringmmsg)).thenReturn(1l);
        final Response response = RULE.client().target("/message")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        //verify(jsonResource).create(messageCaptor.capture());
        //assertThat(messageCaptor.getValue()).isEqualTo(message);
    }
	
    @Test
    public void list() throws Exception {
    	String stringMsg = message.toString();
        when(pool.getResource()).thenReturn(jedis);
    	List<String> stringList = new LinkedList<String>();
    	stringList.add(stringMsg);
    	when(jedis.scan("0")).thenReturn(scanResult);
    	when(scanResult.getStringCursor()).thenReturn("0");
    	when(scanResult.getResult()).thenReturn(stringList);
    	when(jedis.get(anyString())).thenReturn(stringMsg);

    	final ImmutableList<JsonNode> messageList = ImmutableList.of(message);

        final List<JsonNode> response = RULE.client().target("/message")
                .request().get(new GenericType<List<JsonNode>>() {});

        assertThat(response).containsAll(messageList);
    }


    @SuppressWarnings("unchecked")
	@Test
    public void postFailed() {
        when(jedis.publish(anyString(), anyString())).thenThrow(RuntimeException.class);
        final Response response = RULE.getJerseyTest().target("/people/2").request().get();

        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
