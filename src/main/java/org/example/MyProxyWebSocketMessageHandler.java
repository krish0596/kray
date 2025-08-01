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
import io.github.cdimascio.dotenv.Dotenv;

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

    final String botToken = "7853228648:AAFEYyTJ0WrjHYyuTkIy8O4lJNEpxW24Z6Q";
    final String chatId = "-1002321540121";

    public MyProxyWebSocketMessageHandler(MontoyaApi api) {
        this.api = api;
        this.logging = api.logging();
    }
    //IF we PASS this API LOGGING AS THE OBJECT TO CLASS GEMINI MISTRAL THAT WILL BE CALLED CONSTRUCTOR INJECTON AKA DEPENDENCY INJECTION
    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        //logging.logToOutput("inside handleText");
        String jsonString="";
        if (interceptedTextMessage.payload() != null && interceptedTextMessage.payload().length() > 5 && interceptedTextMessage.direction()==Direction.SERVER_TO_CLIENT) {
            try {
                // Remove the first 2
                int firstBracketIndex = interceptedTextMessage.payload().indexOf("[");
                jsonString = interceptedTextMessage.payload().substring(firstBracketIndex);

                // Parse the JSON string into a JsonNode
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonString).get(0);

                // Log the rootNode to check its structure
                //logging.logToOutput("Root Node: " + rootNode.toString());
                // Check if the "data" node exists and log it

                JsonNode dataNode = rootNode.path("data");
                //logging.logToOutput("Data Node: " + dataNode.toString());
                if (!dataNode.isMissingNode()) {
                    //START ONGOING RESPONSE HERE
                    JsonNode ongoingNode = dataNode.path("ongoingQuestion");
                    if (!ongoingNode.isMissingNode()){
                        JsonNode answerExplanationNode = ongoingNode.path("answerExplanation");
                        logging.logToOutput("***");
                        logging.logToOutput(answerExplanationNode.toString());
                        logging.logToOutput("***");
                    }else{
                        logging.logToError("No ongoing question found");
                    }
                    //END ONGOING HERE
                    JsonNode mcqResponseNode = dataNode.path("mcqResponse");
                    //logging.logToOutput("mcqResponse Node: " + mcqResponseNode.toString());
                    if (!mcqResponseNode.isMissingNode()) {
                        JsonNode userResponsesNode = mcqResponseNode.path("userResponses");
                        //logging.logToOutput("userResponses Node: " + userResponsesNode.toString());
                        if (!userResponsesNode.isMissingNode() && userResponsesNode.isArray() && userResponsesNode.size() > 0) {
                            int reslength=userResponsesNode.size();
                            JsonNode correctOptionsNode = userResponsesNode.get(reslength-1).path("correctOptions");
                            JsonNode userSelectedOptionsNode = userResponsesNode.get(reslength - 1).path("userSelectedOptions");
                            //logging.logToOutput("CorrectOptions Node: " + correctOptionsNode.toString());
                            if (!correctOptionsNode.isMissingNode() && correctOptionsNode.isArray() && correctOptionsNode.size() > 0) {
                                // Successfully found the correctOptions, now log the data
                                int length = correctOptionsNode.size();//length to check mcq
                                logging.logToOutput("------ "+length);
                                StringBuilder sb =new StringBuilder();
                                Boolean sent=false;
                                TreeMap<String, String> optionIdtoAnswer = new TreeMap<>();
                                JsonNode questionString = userResponsesNode.get(reslength - 1).path("questionString");
                                String questionLowerCase = questionString.toString().toLowerCase();
                                Boolean isAMatchQuestion = false;
                                if(questionLowerCase.contains("match" ) || questionLowerCase.contains("arrange" )){
                                    int lengthUserSelectedOption = userSelectedOptionsNode.size();
                                    logging.logToOutput("match or arrange task");
                                    for(int j = 0; j < lengthUserSelectedOption; j ++){
                                        JsonNode optionStringNode = userSelectedOptionsNode.get(j).path("optionString");
                                        JsonNode optionIdNode = userSelectedOptionsNode.get(j).path("optionId");
                                        optionIdtoAnswer.put(optionIdNode.toString(), optionStringNode.toString());
                                    }
                                    StringBuilder stb = new StringBuilder();
                                    for(String s: optionIdtoAnswer.keySet()){
                                        stb.append(optionIdtoAnswer.get(s));
                                        stb.append(">>>");
                                    }
                                    if(stb.length() > 0)logging.logToOutput(stb.toString());
                                    optionIdtoAnswer.clear();
                                    isAMatchQuestion = true;
                                }
                                
                                if(!isAMatchQuestion){

                                for (int i = 0; i < length; i++) {                                    
                                  
                                    JsonNode JsonNodeCorrectOption = correctOptionsNode.get(i).path("optionString");
                                    //new line
                                    JsonNode JsonNodeCorrectImage = correctOptionsNode.get(i).path("optionSupportingMedia");
                                    if (!JsonNodeCorrectImage.isMissingNode() && JsonNodeCorrectImage.size() > 0) {
                                        JsonNode JsonNodeTrueImage = JsonNodeCorrectImage.path("thumbnailCfUrl");
                                        if (!JsonNodeTrueImage.isMissingNode()) {
                                            //sendToTelegram(JsonNodeTrueImage.toString());
                                            //sb.append(JsonNodeTrueImage.toString());
                                            //sb.append(" ");
                                            //sent = true;
                                            String images = convertToImgTag(JsonNodeTrueImage.asText(),"alt test",100,100);
                                            logging.logToOutput(images);
                                        }
                                    } else {
                                        sb.append(JsonNodeCorrectOption.asText());
                                    }
                                    if (i != length - 1)
                                        sb.append("::");
                                    }
                                }
                                logging.logToOutput(convertToh3Option(sb.toString()));
                                if(!sent)
                                sendToTelegram(sb.toString());
                            } else {
                                logging.logToError("Correct options not found or empty.");
                            }
                        } else {
                            logging.logToError("User responses not found or empty.");
                        }
                    } else {
                        logging.logToError("mcqResponse node is missing.");
                    }
                } else {
                    logging.logToError("Data node is missing.");
                }
            } catch (Exception e) {
                logging.logToError("Error processing message: " + e.getMessage());
            }
        }
        return TextMessageReceivedAction.continueWith(interceptedTextMessage);
    }
    private void sendToTelegram(String message) {
        message = message.replace("\"", "");
        if (message == null || message.trim().isEmpty()) {
            logging.logToError("Message is empty, not sending to Telegram.");
            return; // Do not proceed if the message is empty
        }
        try {
            // Build the Telegram API URL
            String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            // Create the JSON payload
            String payload = "{\"chat_id\":\"" + chatId + "\",\"text\":\"" + message + "\"}";

            // Set up the HTTP connection
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send the request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            // Log the response
            int responseCode = conn.getResponseCode();
            try (InputStream is = (responseCode == HttpURLConnection.HTTP_OK) ? conn.getInputStream() : conn.getErrorStream()) {
                // Read the response
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Log the full response from Telegram
                logging.logToError("Telegram API Response: " + response.toString());

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    logging.logToError("Message sent to Telegram successfully!");
                } else {
                    logging.logToError("Failed to send message to Telegram. Response code: " + responseCode);
                }
            }

        } catch (Exception e) {
            logging.logToError("Error sending message to Telegram: " + e.getMessage());
        }
    }

    private String createOptionsPayload(HashMap<String, String> optionsMap) {
        StringBuilder optionsPayload = new StringBuilder();
        for (Map.Entry<String, String> entry : optionsMap.entrySet()) {
            optionsPayload.append(entry.getValue()).append(" | ");
        }
        // Remove the trailin |
        if (!optionsPayload.isEmpty()) {
            optionsPayload.setLength(optionsPayload.length() - 3);
        }
        return optionsPayload.toString();
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

    public String convertToImgTag(String imageUrl, String altText, int width, int height) {
        return String.format("<img src=\"%s\" alt=\"%s\" width=\"%d\" height=\"%d\" />",
                imageUrl, altText, width, height);
    }
    public String convertToParaAnswerExp(String text) {
        return "<p>" + text + "</p>";
    }
    public String convertToh3Option(String text) {
        return "<h3>" + text + "</h3>";
    }

}
