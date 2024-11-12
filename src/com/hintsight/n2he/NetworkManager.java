package com.hintsight.n2he;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class NetworkManager {
    public static void postJSON(String uri, Map<String, Object> map) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper
//                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(map);

        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

//        return HttpClient.newHttpClient()
//                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(HttpResponse::statusCode)
//                .thenAccept(System.out::println);
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST request status code: " + response.statusCode());
    }

    public static HttpResponse<String> getJSON(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .header("Accept", "application/json")
                .build();

//        HttpResponse<String> response =

        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        return objectMapper.readValue(jsonData, UserEncryptedResult.class);
    }
}
