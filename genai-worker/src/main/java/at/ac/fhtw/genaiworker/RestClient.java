package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public class RestClient {
    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private static final String BASE_URL = System.getenv().getOrDefault("BACKEND_BASE_URL", "http://backend:8081");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void sendSummaryToBackend(String documentId, String summaryText, String model, int tokens) throws Exception {
        String url = BASE_URL + "/api/documents/" + documentId + "/summary";
        String body = MAPPER.writeValueAsString(Map.of(
                "summary", summaryText,
                "model", model,
                "tokens", tokens
        ));

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("Summary sent -> {} HTTP {}", url, res.statusCode());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("Backend summary failed: HTTP " + res.statusCode() + " body=" + res.body());
        }
    }
}
