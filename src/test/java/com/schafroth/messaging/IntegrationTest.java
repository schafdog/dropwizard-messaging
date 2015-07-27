package com.schafroth.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.google.common.base.Optional;
import com.schafroth.messaging.core.Message;
import com.schafroth.messaging.core.Saying;

public class IntegrationTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");

    @ClassRule
    public static final DropwizardAppRule<MessagingConfiguration> RULE = new DropwizardAppRule<>(
            MessagingApplication.class, CONFIG_PATH);

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        
    }

    @Test
    public void testHelloWorld() throws Exception {
        final Optional<String> name = Optional.fromNullable("Dr. IntegrationTest");
        final Saying saying = client.target("http://localhost:" + RULE.getLocalPort() + "/hello-world")
                .queryParam("name", name.get())
                .request()
                .get(Saying.class);
        assertThat(saying.getContent()).isEqualTo(RULE.getConfiguration().buildTemplate().render(name));
    }

    @Test
    public void testPostMessage() throws Exception {
        final Message person = new Message("IntegrationTest", null);
        final Message newPerson = client.target("http://localhost:" + RULE.getLocalPort() + "/message")
                .request().post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Message.class);
        assertThat(newPerson.getId()).isNotNull();
        assertThat(newPerson.getPayload()).isEqualTo(person.getPayload());
        assertThat(newPerson.getTime() != null);
    }
}
