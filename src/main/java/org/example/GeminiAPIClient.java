package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GeminiAPIClient {
    String apiKey = "AIzaSyCkWz2KCDytlt2H-E_KYRsFn59imGWQ0gs";
    String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b:generateContent?key=" + apiKey;
    String content;
    String context=" As u can see there are 2 options , select the appropirate correct option based on question and its answer explanation and only give the correct option dont give explanation, if the option contains an image link (URL) only say the relevant name from the answer Explnation..";


    GeminiAPIClient(String content) {
        this.content = content;
    }
    // MISTRAL CODELSTRA
    public CompletableFuture<String> getResponse() {
        // Create the JSON payload for the request
        String jsonPayload = """
                        {
                            "contents": [{
                                "parts": [{"text": "%s"}]
                            }]
                        }
                        """.formatted(content +" " +context);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(responseBody -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(responseBody.body());
                        String text = root.path("candidates")
                                .get(0)
                                .path("content")
                                .path("parts")
                                .get(0)
                                .path("text")
                                .asText();
                        return text;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Error";
                    }
                })
                .exceptionally(e -> {
                    // Log if there's an exception during the async call
                    System.err.println("Exception occurred: " + e.getMessage());
                    e.printStackTrace();
                    return "dd";
                });
    }
}
