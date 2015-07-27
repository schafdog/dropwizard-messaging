package com.schafroth.messaging;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import java.util.Map;

import javax.servlet.ServletRegistration;

import redis.clients.jedis.JedisPool;

import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import com.schafroth.messaging.auth.ExampleAuthenticator;
import com.schafroth.messaging.cli.RenderCommand;
import com.schafroth.messaging.core.Message;
import com.schafroth.messaging.core.Template;
import com.schafroth.messaging.core.User;
import com.schafroth.messaging.filter.DateRequiredFeature;
import com.schafroth.messaging.health.TemplateHealthCheck;
import com.schafroth.messaging.resources.FilteredResource;
import com.schafroth.messaging.resources.HelloWorldResource;
import com.schafroth.messaging.resources.JsonResource;
import com.schafroth.messaging.resources.PersonResource;
import com.schafroth.messaging.resources.ProtectedResource;
import com.schafroth.messaging.resources.ViewResource;

public class MessagingApplication extends Application<MessagingConfiguration> {
	
	RedisSubscriber subscriber; 
    public static void main(String[] args) throws Exception {
        new MessagingApplication().run(args);
    }

    private final HibernateBundle<MessagingConfiguration> hibernateBundle =
            new HibernateBundle<MessagingConfiguration>(Message.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(MessagingConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "messaging-app";
    }

    @Override
    public void initialize(Bootstrap<MessagingConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle("/assets", "/static"));

        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<MessagingConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(MessagingConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
        
        bootstrap.addBundle(new JedisBundle<MessagingConfiguration>() {
            @Override
            public JedisFactory getJedisFactory(MessagingConfiguration configuration) {
                return configuration.getJedisFactory();
            }
        });
    }        

    @Override
    public void run(MessagingConfiguration configuration, Environment environment) {
        final Template template = configuration.buildTemplate();
        final JedisPool pool = configuration.getJedisFactory().build(environment);
        JedisPoolFactory.getInstance().setPool(pool);
        subscriber = new RedisSubscriber(pool, new PersistHandler(pool));

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.jersey().register(DateRequiredFeature.class);

        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<>(new ExampleAuthenticator(),
                                                                 "SUPER SECRET STUFF",
                                                                 User.class)));
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new JsonResource(pool));
        environment.jersey().register(new PersonResource(pool));
        environment.jersey().register(new FilteredResource());
        environment.lifecycle().manage(subscriber);
        
        final ServletRegistration.Dynamic websocket = environment.servlets().addServlet(
                "websocket",
                new MessagingWebSocketServlet(pool));
        websocket.setAsyncSupported(true);
        websocket.addMapping("/websocket/*");


    }
}
