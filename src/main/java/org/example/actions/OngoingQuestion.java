package org.example.actions;

import burp.api.montoya.logging.Logging;
import com.fasterxml.jackson.databind.JsonNode;

public class OngoingQuestion {
    Logging logging;

    public OngoingQuestion(Logging logging){
        this.logging = logging;
    }

    public void process(JsonNode ongoingNode){
        if (!ongoingNode.isMissingNode()) {
            JsonNode answerExplanationNode = ongoingNode.path("answerExplanation");
            logging.logToOutput("***");
            logging.logToOutput(answerExplanationNode.toString());
            logging.logToOutput("***");
            // In the future, you might send this to Telegram here.
            // telegramClient.sendToTelegram(answerExplanationNode.toString());
        } else {
            logging.logToError("No ongoing question found");
        }
    }
}
