# Introduction

The Dropwizard messaging application was developed based on Dropwizard-example 0.8.1, 
added some dropwizard frameworks (droptool and JSR-356 websocket framework). 
Some unused example code has been remove but some is prob. still remaining

# Overview
  
The application accept data via a REST service on http://falconsocial.schafroth.dk/message which then is publish on a Redis PubSub channel. 
This channel has one permanent subscriber (RedisSubscriber) which persist into Redis using PersistHandler.

In case a WebSocket client connects to ws://falconsocial.schafroth.dk/websocket a secondary subscriber is created via a MultiWebsocketEndpoint Handler, 
implementing Jetty native WebSocket Server API. The MultiWebsocketEndpoint is shared between all connected websocket client, 
and manage websocket sessions. 

Browsers with websocket support can browse incomming messages at http://falconsocial.schafroth.dk/assets/messaging.html

All persisted messages can be seen at the REST endpoint: http://falconsocial.schafroth.dk/message

# Running The Application

To test the example application run the following commands.

* To package the example run.

        mvn package

* The application requires an installed and running Redis server (2.8 has been tested) on localhost:6379

* To run the server run.

        java -jar target/dropwizard-messaging-0.8.1.jar server example.yml

* To post data into the application.

	curl -H "Content-Type: application/json" -X POST -d '{"fullName":"Other Person","jobTitle":"Other Title"}' http://falconsocial.schafroth.dk/message

The server will atttach a UUID and time stamp to the message before publishing it and returning it to the client.

A server process is monitoring part of the file system for changes and posting these in the messaging application.
	
* To list all persisted data in JSON format browse:

        http://falconsocial.schafroth.dk/message

* To connect a browser via a websocket browse to:
  
        http://falconsocial.schafroth.dk/assets/messaging.html 

* The client can also broadcast messages over the websocket to other listening websocket clients, but they are not persisted in Redis. 

The setup has been set up on my own machine. It has been configure with a apache in front to serve static content and 
proxies other requests to dropwizard application.

The code has been checked into 

https://github.com/schafdog/dropwizard-messaging


