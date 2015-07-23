package com.schafroth.messaging;

import com.bendb.dropwizard.redis.JedisFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.schafroth.messaging.core.Template;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Context;

import java.util.Collections;
import java.util.Map;

public class MessagingConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    
    @NotNull
    private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

    @NotNull
    @Context
    private JedisFactory jedisFactory = new JedisFactory();

    private WebSocketServletFactory webSocketServletFactory; 
    
    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Template buildTemplate() {
        return new Template(template, defaultName);
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty("viewRendererConfiguration")
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return viewRendererConfiguration;
    }

    @JsonProperty("redis")
    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

    @JsonProperty("redis")
    public void setRedisFactory(JedisFactory factory) {
        jedisFactory = factory;
    }
    
    @JsonProperty("websocket")
    public WebSocketServletFactory getWebSocketFactory() {
        return webSocketServletFactory;
    }

    @JsonProperty("websocket")
    public void setWebSocketFactory(WebSocketServletFactory factory) {
        this.webSocketServletFactory = factory;
    }

    @JsonProperty("viewRendererConfiguration")
    public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
        ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
        for (Map.Entry<String, Map<String, String>> entry : viewRendererConfiguration.entrySet()) {
            builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
        }
        this.viewRendererConfiguration = builder.build();
    }
}
