package org.example.actions;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.proxy.websocket.TextMessageReceivedAction;
import burp.api.montoya.websocket.Direction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.apiClients.TelegramClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class InspectWebSocketMessage {
    InterceptedTextMessage inspectMessage;
    Logging logging;


    public InspectWebSocketMessage(InterceptedTextMessage inspectMessage, Logging logging) {
        this.inspectMessage = inspectMessage;
        this.logging = logging;
    }

    public void getInspectedMessage(InterceptedTextMessage inspectMessage) {
        String jsonString = "";
        if (inspectMessage.payload() != null && inspectMessage.payload().length() > 5 && inspectMessage.direction() == Direction.SERVER_TO_CLIENT) {
            try {
                // Remove the first 2
                int firstBracketIndex = inspectMessage.payload().indexOf("[");
                jsonString = inspectMessage.payload().substring(firstBracketIndex);

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
                    if (!ongoingNode.isMissingNode()) {
                        JsonNode answerExplanationNode = ongoingNode.path("answerExplanation");
                        logging.logToOutput("***");
                        logging.logToOutput(answerExplanationNode.toString());
                        logging.logToOutput("***");
                    } else {
                        logging.logToError("No ongoing question found");
                    }
                    //END ONGOING HERE
                    JsonNode mcqResponseNode = dataNode.path("mcqResponse");
                    //logging.logToOutput("mcqResponse Node: " + mcqResponseNode.toString());
                    if (!mcqResponseNode.isMissingNode()) {
                        JsonNode userResponsesNode = mcqResponseNode.path("userResponses");
                        //logging.logToOutput("userResponses Node: " + userResponsesNode.toString());
                        if (!userResponsesNode.isMissingNode() && userResponsesNode.isArray() && userResponsesNode.size() > 0) {
                            int reslength = userResponsesNode.size();
                            JsonNode correctOptionsNode = userResponsesNode.get(reslength - 1).path("correctOptions");
                            JsonNode userSelectedOptionsNode = userResponsesNode.get(reslength - 1).path("userSelectedOptions");
                            //logging.logToOutput("CorrectOptions Node: " + correctOptionsNode.toString());
                            if (!correctOptionsNode.isMissingNode() && correctOptionsNode.isArray() && correctOptionsNode.size() > 0) {
                                // Successfully found the correctOptions, now log the data
                                int length = correctOptionsNode.size();//length to check mcq
                                logging.logToOutput("------ " + length);
                                StringBuilder sb = new StringBuilder();
                                Boolean sent = false;
                                TreeMap<String, String> optionIdtoAnswer = new TreeMap<>();
                                JsonNode questionString = userResponsesNode.get(reslength - 1).path("questionString");
                                String questionLowerCase = questionString.toString().toLowerCase();
                                Boolean isAMatchQuestion = false;
                                if (questionLowerCase.contains("match") || questionLowerCase.contains("arrange")) {
                                    int lengthUserSelectedOption = userSelectedOptionsNode.size();
                                    logging.logToOutput("match or arrange task");
                                    for (int j = 0; j < lengthUserSelectedOption; j++) {
                                        JsonNode optionStringNode = userSelectedOptionsNode.get(j).path("optionString");
                                        JsonNode optionIdNode = userSelectedOptionsNode.get(j).path("optionId");
                                        optionIdtoAnswer.put(optionIdNode.toString(), optionStringNode.toString());
                                    }
                                    StringBuilder stb = new StringBuilder();
                                    for (String s : optionIdtoAnswer.keySet()) {
                                        stb.append(optionIdtoAnswer.get(s));
                                        stb.append(">>>");
                                    }
                                    if (stb.length() > 0) logging.logToOutput(stb.toString());
                                    optionIdtoAnswer.clear();
                                    isAMatchQuestion = true;
                                }

                                if (!isAMatchQuestion) {

                                    for (int i = 0; i < length; i++) {

                                        JsonNode JsonNodeCorrectOption = correctOptionsNode.get(i).path("optionString");
                                        //new line
                                        JsonNode JsonNodeCorrectImage = correctOptionsNode.get(i).path("optionSupportingMedia");
                                        if (!JsonNodeCorrectImage.isMissingNode() && JsonNodeCorrectImage.size() > 0) {
                                            JsonNode JsonNodeTrueImage = JsonNodeCorrectImage.path("thumbnailCfUrl");
                                            if (!JsonNodeTrueImage.isMissingNode()) {
                                                TelegramClient tele = new TelegramClient(JsonNodeTrueImage.toString());
                                                tele.sendToTelegram(JsonNodeTrueImage.toString());
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
                                logging.logToOutput(sb.toString());
                                TelegramClient tel = new TelegramClient(sb.toString());
                                if (!sent)
                                    tel.sendToTelegram(sb.toString());
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
    }

}
