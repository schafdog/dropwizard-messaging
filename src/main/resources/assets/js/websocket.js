
var wsUri = "ws://localhost:8080/websocket/";
var output = document.getElementById("output");

function init(wsUrl, element) {
	output = element;
	wsUri = wsUrl;
	return testWebSocket();
}

function testWebSocket() {
	websocket = new WebSocket(wsUri);
	websocket.onopen = function(evt) {
		onOpen(evt)
	};
	websocket.onclose = function(evt) {
		onClose(evt)
	};
	websocket.onmessage = function(evt) {
		onMessage(evt)
	};
	websocket.onerror = function(evt) {
		onError(evt)
	};
	return websocket;
}

function onOpen(evt) {
	writeToScreen("CONNECTED");
	doSend("WebSocket rocks");
}

function onClose(evt) {
	writeToScreen("DISCONNECTED");
}

function onMessage(evt) {
	//writeToScreen('<span style="color: blue;">Last Message: ' + evt.data + '</span>');
	output.innerHTML = evt.data;
	doSend("WebSocket rocks");
}

function onError(evt) {
	writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message) {
	writeToScreen("SENDING: " + message);
	websocket.send(message);
	writeToScreen("SENT: " + message);
}

function writeToScreen(message) {
	var pre = document.createElement("p");
	pre.style.wordWrap = "break-word";
	pre.innerHTML = message;
	output.appendChild(pre);
}