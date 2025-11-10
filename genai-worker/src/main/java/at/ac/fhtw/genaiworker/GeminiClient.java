package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = System.getenv().getOrDefault("GENAI_MODEL", "gemini-2.0-flash");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public static String summarize(String text) throws Exception {
        if (API_KEY == null || API_KEY.isBlank())
            throw new IllegalStateException("GEMINI_API_KEY not set!");

        // 1) Eingabetext begrenzen (optional, schützt vor sehr langen OCR-Texten)
        String clipped = text == null ? "" : text;
        if (clipped.length() > 20000) clipped = clipped.substring(0, 20000);

        // 2) Payload nach aktuellem v1-Schema
        ObjectNode root = MAPPER.createObjectNode();
        var contents = root.putArray("contents");
        var content0 = contents.addObject();
        content0.put("role", "user");
        var parts = content0.putArray("parts");
        parts.addObject().put("text", "Fasse den folgenden Text kurz und prägnant zusammen:\n\n" + clipped);

        String payload = MAPPER.writeValueAsString(root);

        // 3) v1-Endpoint verwenden
        URI uri = URI.create(
                "https://generativelanguage.googleapis.com/v1/models/"
                        + MODEL + ":generateContent?key=" + API_KEY
        );

        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        int attempts = 0;
        while (true) {
            attempts++;
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                return extractText(res.body());
            }
            if (res.statusCode() == 429 && attempts < 4) {
                long backoff = (long) Math.pow(2, attempts) * 500L;
                log.warn("Gemini 429, retrying in {} ms (attempt {}/{})", backoff, attempts, 3);
                Thread.sleep(backoff);
                continue;
            }
            throw new RuntimeException("Gemini API failed (" + res.statusCode() + "): " + res.body());
        }
    }

    private static String extractText(String body) throws Exception {
        JsonNode root = MAPPER.readTree(body);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray() && parts.size() > 0) {
            String txt = parts.get(0).path("text").asText("");
            if (!txt.isEmpty()) return txt.trim();
        }
        // fallback older shape
        JsonNode txt = root.at("/candidates/0/content/parts/0/text");
        if (!txt.isMissingNode()) return txt.asText().trim();
        return body; // last resort
    }
}
