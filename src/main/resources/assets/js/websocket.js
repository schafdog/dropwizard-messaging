var webSocket;
var output = document.getElementById("output");
var table  = document.getElementById("table");
var connectBtn = document.getElementById("connectBtn");
var sendBtn = document.getElementById("sendBtn");
var wsUri = "ws://localhost:8080/websocket";

function toogle() {
	// open the connection if one does not exist
	if (webSocket !== undefined
			&& webSocket.readyState == WebSocket.OPEN) {
			closeSocket();
            return;
	}
	// Create a websocket
	webSocket = new WebSocket(wsUri);

	webSocket.onopen = function(event) {
		updateOutput("Connected!");
		connectBtn.setAttribute('value', 'Disconnect');
		sendBtn.disabled = false;

	};

	webSocket.onmessage = function(event) {
		updateOutput(event.data);
	};

	webSocket.onclose = function(event) {
		updateOutput("Connection Closed");
		connectBtn.setAttribute('value', 'Connect');
		sendBtn.disabled = true;
	};
}

function send() {
	var text = document.getElementById("input").value;
	webSocket.send(text);
}

function closeSocket() {
	webSocket.close();
	webSocket.onclose();
}

function updateTable(text) {
	var row = table.insertRow(0);
	var cell1 = row.insertCell(0);
	var cell2 = row.insertCell(1);
	cell1.innerHTML = text.UUID;
	cell2.innerHTML = text; 
}

function updateOutput(text) {
	output.innerHTML = text
	updateTable(text);
}
 
function refreshView() {
	var date = new Date();
	for (var i = 0, row; row = table.rows[i]; i++) {
		if (row.cell[0]+60000 > date.getTime()) {
			table.deleteRow(i)
            i--;
        }
	}
}  
 
var myVar=setInterval(function () {refreshView()}, 1000);
toogle();
