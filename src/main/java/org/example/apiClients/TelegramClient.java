package org.example.apiClients;

import burp.api.montoya.logging.Logging;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TelegramClient {
    String message;
    Logging logging;
    final String botToken = "7853228648:AAFEYyTJ0WrjHYyuTkIy8O4lJNEpxW24Z6Q";
    final String chatId = "-1002321540121";

    public TelegramClient(String message){
        this.message=message;
    }

    public void sendToTelegram(String message) {
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
}
