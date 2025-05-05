package org.example;

import java.util.concurrent.CompletableFuture;
//for testing purpose
public class APITest {
    public static void main(String[] args) {
        String content  = "capital of india?";

        MistralAPIClient client = new MistralAPIClient(content);
        GeminiAPIClient geminiClient = new GeminiAPIClient(content);
        CompletableFuture<String> futureResponse1 = geminiClient.getResponse();
        String result1 = futureResponse1.join();
        System.out.println("Final result from gemini: " + result1 + "\nTime taken: "  + "ms");
        long startTime = System.currentTimeMillis();
        CompletableFuture<String> futureResponse = client.getResponse();
        String result = futureResponse.join();  // .join() waits and returns the result
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("Final result: " + result + "\nTime taken: " + timeTaken + "ms");
    }
}
