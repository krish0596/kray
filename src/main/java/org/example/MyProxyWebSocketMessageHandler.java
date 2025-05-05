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
import io.github.cdimascio.dotenv.Dotenv;

import burp.api.montoya.websocket.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyMessageHandler; // Import ProxyMessageHandler
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.proxy.websocket.*;

import static burp.api.montoya.websocket.Direction.CLIENT_TO_SERVER;

public class MyProxyWebSocketMessageHandler implements ProxyMessageHandler { // Change to implement ProxyMessageHandler

    MontoyaApi api;
    Logging logging;
    //for some reason i am not able to pull these secrets from env file please look into it
    final String botToken = "7853228648:AAFEYyTJ0WrjHYyuTkIy8O4lJNEpxW24Z6Q";
    final String chatId = "-1002321540121";


    public MyProxyWebSocketMessageHandler(MontoyaApi api) {
        // Save a reference to the MontoyaApi object
        this.api = api;
        // Save a reference to the logging object, to print messages to stdout and stderr
        this.logging = api.logging();
        //logging.logToOutput("all working2");
    }

    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        long timeTakenTogetAnswer = 0;
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
                //***************************
                //TODO fix the messy code and refactor into diff functions PLEASE GIVE A LOOK for refactor
                //***************************

                // Check if the "data" node exists and log it
                JsonNode dataNode = rootNode.path("data");
                //logging.logToOutput("Data Node: " + dataNode.toString());
                if (!dataNode.isMissingNode()) {
                    //START ONGOING RESPONSE HERE
                    JsonNode ongoingNode = dataNode.path("ongoingQuestion");
                    if (!ongoingNode.isMissingNode()){
                        HashMap<String,String> optionsMap = new HashMap<>();
                        JsonNode answerExplanationNode = ongoingNode.path("answerExplanation");
                        JsonNode questionStringNode = ongoingNode.path("questionString");
                        JsonNode optionsNode = ongoingNode.path("options");
                        //len of option node is 4
                        for(int k=0;k<optionsNode.size();k++){
                            JsonNode optionNode = optionsNode.get(k).path("optionString");
                            JsonNode optionIdNode = optionsNode.get(k).path("optionId");
                            optionsMap.put(optionIdNode.asText(),optionNode.asText()); // ID: TEXT VALUE
                            logging.logToError(optionIdNode.asText()+" - "+optionsMap.get(optionIdNode.asText()));
                        }
                        JsonNode fiftyNode = ongoingNode.path("fiftyFiftyRemoveOptionIds");
                        for(int k=0;k<fiftyNode.size();k++){
                            optionsMap.remove(fiftyNode.get(k).asText());
                        }
                        String questionString = questionStringNode.asText();
                        String optionsPayload = createOptionsPayload(optionsMap);

                        logging.logToError("OPTIONS: "+optionsPayload); //should contains 2 oiption
                        questionString.replace('"',' ');
                        optionsPayload.replace('"',' ');
                        String explanation = answerExplanationNode.asText().replace('"', ' ');
                        String finalText= "Question: " + questionString + " Options: "+ optionsPayload + " answerExplanation :" + explanation;
                        finalText = finalText.replace("\\", "\\\\").replace("\"", "\\\"");

                        //GeminiAPIClient geminiClient = new GeminiAPIClient(finalText);
                        MistralAPIClient mistralClient = new MistralAPIClient(finalText);
                        long startTime = System.currentTimeMillis();
//                        geminiClient.getResponse().thenAccept(resultGemini ->{
//                            long elapsed = System.currentTimeMillis() - startTime;
//                            logging.logToOutput("Gemini response: " + resultGemini + " took " + elapsed + " ms");
//                        });
                        mistralClient.getResponse().thenAccept(resultMistral ->{
                            long elapsed = System.currentTimeMillis() - startTime;
                            String boxedMessage = String.format("""
                                        ***************
                                        * %s
                                        * Took: %d ms
                                        ***************
                                        """, resultMistral, elapsed);
                            logging.logToOutput(boxedMessage);
                        });
                        //TODO testing of these API calls//
                        timeTakenTogetAnswer = System.currentTimeMillis();
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
                        // Check if "userResponses" exists
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
                                            sendToTelegram(JsonNodeTrueImage.toString());
                                            sb.append(JsonNodeTrueImage.toString());
                                            sb.append(" ");
                                            sent = true;
                                        }
                                    } else {
                                        sb.append(JsonNodeCorrectOption.toString());
                                    }
                                    if (i != length - 1)
                                        sb.append("::");
                                }
                            }
                                long endTime = System.currentTimeMillis();
                                long timeTaken = endTime - timeTakenTogetAnswer;
                                logging.logToOutput("Time taken for Correct answer: " + timeTaken + " ms");
                                logging.logToOutput(sb.toString());
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
}
