package org.example;

import org.example.actions.InspectWebSocketMessage;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyMessageHandler;
import burp.api.montoya.proxy.websocket.*;
import java.util.function.Function;

public class MyProxyWebSocketMessageHandler implements ProxyMessageHandler {
    Logging logging;
    Function<InterceptedTextMessage, InspectWebSocketMessage> inspectorProvider;

    public MyProxyWebSocketMessageHandler(MontoyaApi api) {
        //this.logging = api.logging();
        this(api, (interceptedTextMessage)-> new InspectWebSocketMessage(api.logging()));
    }
    MyProxyWebSocketMessageHandler(MontoyaApi api, Function<InterceptedTextMessage, InspectWebSocketMessage> provider) {
        this.logging = api.logging();
        this.inspectorProvider = provider; // The test provides a fake recipe here.
    }

    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        InspectWebSocketMessage inspectWebSocketMessage = this.inspectorProvider.apply(interceptedTextMessage);
//        InspectWebSocketMessage inspectWebSocketMessage = new InspectWebSocketMessage(interceptedTextMessage,logging);
        inspectWebSocketMessage.getInspectedMessage(interceptedTextMessage);
        return TextMessageReceivedAction.continueWith(interceptedTextMessage);
    }

    @Override
    public TextMessageToBeSentAction handleTextMessageToBeSent(InterceptedTextMessage interceptedTextMessage) {
        return TextMessageToBeSentAction.continueWith(interceptedTextMessage);
    }

    @Override
    public BinaryMessageReceivedAction handleBinaryMessageReceived(InterceptedBinaryMessage interceptedBinaryMessage) {
        return BinaryMessageReceivedAction.continueWith(interceptedBinaryMessage);
    }

    @Override
    public BinaryMessageToBeSentAction handleBinaryMessageToBeSent(InterceptedBinaryMessage interceptedBinaryMessage) {
        return BinaryMessageToBeSentAction.continueWith(interceptedBinaryMessage);
    }
}
