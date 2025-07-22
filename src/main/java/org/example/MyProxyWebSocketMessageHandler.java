package org.example;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JacksonInject;
//import io.github.cdimascio.dotenv.Dotenv;
import org.example.actions.InspectWebSocketMessage;
import burp.api.montoya.websocket.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyMessageHandler;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.proxy.websocket.*;

import static burp.api.montoya.websocket.Direction.CLIENT_TO_SERVER;
public class MyProxyWebSocketMessageHandler implements ProxyMessageHandler {

    MontoyaApi api;
    Logging logging;

    public MyProxyWebSocketMessageHandler(MontoyaApi api) {
        this.api = api;
        this.logging = api.logging();
    }
    //IF we PASS this API LOGGING AS THE OBJECT TO CLASS GEMINI MISTRAL THAT WILL BE CALLED CONSTRUCTOR INJECTON AKA DEPENDENCY INJECTION
    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        InspectWebSocketMessage inspectWebSocketMessage = new InspectWebSocketMessage(interceptedTextMessage,logging);
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
