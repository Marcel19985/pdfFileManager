package at.ac.fhtw.genaiworker;

import java.net.http.*;
import java.net.URI;

public class GeminiClient {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = System.getenv().getOrDefault("GENAI_MODEL", "gemini-2.0-flash");

    public static String summarize(String text) throws Exception {
        if (API_KEY == null || API_KEY.isBlank())
            throw new IllegalStateException("GEMINI_API_KEY not set!");

        String json = """
        { "contents": [ { "parts": [ { "text": "%s" } ] } ] }
        """.formatted(text.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent"))
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Gemini API failed: " + response.body());

        return response.body();
    }
}
