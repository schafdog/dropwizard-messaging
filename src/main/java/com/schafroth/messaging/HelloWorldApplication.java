package com.schafroth.messaging;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import java.util.Map;

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

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	
	RedisSubscriber subscriber; 
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
            new HibernateBundle<HelloWorldConfiguration>(Message.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
        
        bootstrap.addBundle(new JedisBundle<HelloWorldConfiguration>() {
            @Override
            public JedisFactory getJedisFactory(HelloWorldConfiguration configuration) {
                return configuration.getJedisFactory();
            }
        });
    }        

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
        final Template template = configuration.buildTemplate();
        final JedisPool pool = configuration.getJedisFactory().build(environment);
        subscriber = new RedisSubscriber(pool);

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

    }
}
