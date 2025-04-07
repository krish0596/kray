package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

public class ProxyWebSocketHandlerExample implements BurpExtension {

    MontoyaApi api;
    Logging logging;

    @Override
    public void initialize(MontoyaApi api) {

        this.api=api;
        this.logging=api.logging();
        api.extension().setName("Montoya API tutorial - Websocket exp");
        this.logging.logToOutput("*** Montoya API tutorial - Webs loaded ***");
        api.proxy().registerWebSocketCreationHandler(new MyProxyWebSocketCreationHandler(api));
    }
}
