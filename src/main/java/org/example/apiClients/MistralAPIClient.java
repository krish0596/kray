package org.example.apiClients;

import burp.api.montoya.logging.Logging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class MistralAPIClient {
    Logging logging;
    String apiKey = "ZFxP7tFijiTTFne0Q4ONlBRooyFYOj1y";
    String agentId = "ag:a634815b:20250505:untitled-agent:ff4496a1";
    String apiUrl = "https://api.mistral.ai/v1/agents/completions";
    String content;

    public MistralAPIClient(String content) {
        this.content = content;
    }
    MistralAPIClient(String content, Logging logging) {
        this.content = content;
        this.logging = logging;
    }
    // MISTRAL CODELSTRA
    public CompletableFuture<String> getResponse() {
        // Create the JSON payload for the request
        String jsonPayload = """
            {
                "agent_id": "%s",
                "messages": [{
                        "role": "user",
                        "content": "%s"
                }]
            }
        """.formatted(agentId, content);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(responseBody -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(responseBody.body());
                        String text = root.path("choices")
                                .get(0)
                                .path("message")
                                .path("content")
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
