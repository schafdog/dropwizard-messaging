package com.schafroth.messaging.views;

import com.schafroth.messaging.core.Message;

import io.dropwizard.views.View;

public class MessageView extends View {
    private final Message message;
    public enum Template{
    	FREEMARKER("freemarker/message.ftl"),
    	MUSTACHE("mustache/message.mustache");
    	
    	private String templateName;
    	private Template(String templateName){
    		this.templateName = templateName;
    	}
    	
    	public String getTemplateName(){
    		return templateName;
    	}
    }

    public MessageView(MessageView.Template template, Message person) {
        super(template.getTemplateName());
        this.message = person;
    }

    public Message getMessage() {
        return message;
    }
}