package org.example;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
//import burp.api.montoya.proxy.websocket.ProxyMessageHandler;
//import burp.api.montoya.websocket.WebSocket;
//import burp.api.montoya.websocket.WebSocketCreated;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreationHandler;

public class MyProxyWebSocketCreationHandler implements ProxyWebSocketCreationHandler {
    MontoyaApi api;
    Logging logging;

    public MyProxyWebSocketCreationHandler(MontoyaApi api) {
        // Save a reference to the MontoyaApi object
        this.api = api;
        // api.logging() returns an object that we can use to print messages to stdout and stderr
        this.logging = api.logging();
    }
    @Override
    public void handleWebSocketCreation(ProxyWebSocketCreation webSocketCreation) {
        // Register a listener to handle bidirectional messages of the WebSocket
        logging.logToOutput("all working");
        webSocketCreation.proxyWebSocket().registerProxyMessageHandler(new MyProxyWebSocketMessageHandler(api));

    }
}
