package at.ac.fhtw.genaiworker;

import java.net.http.*;
import java.net.URI;

public class RestClient {
    private static final String BASE_URL = System.getenv().getOrDefault("BACKEND_BASE_URL", "http://backend:8081");

    public static void sendSummaryToBackend(String summaryJson) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/documents/summary"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(summaryJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("📤 Summary sent to backend: HTTP " + response.statusCode());
    }
}
