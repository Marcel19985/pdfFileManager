package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class); // Logger für Konsolen- bzw. Container-Logs
    private static final ObjectMapper MAPPER = new ObjectMapper(); // JSON (De-)Serialisierung mit Jackson

    static String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = System.getenv().getOrDefault("GENAI_MODEL", "gemini-2.0-flash");
    static HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)) //timeout 20 Sekunden
            .build();

    public static String summarize(String text) throws Exception { //Sendet den Dateiinhalt an die Gemini-API und gibt die erzeugte Zusammenfassung zurück
        if (API_KEY == null || API_KEY.isBlank()) // Prüfen, ob API-Key gesetzt wurde
            throw new IllegalStateException("GEMINI_API_KEY not set!");

        // Eingabetext begrenzen (schützt vor sehr langen OCR-Texten):
        String clipped = text == null ? "" : text;
        if (clipped.length() > 20000) clipped = clipped.substring(0, 20000);

        // JSON-Request-Body nach Gemini v1-Schema erstellen:
        ObjectNode root = MAPPER.createObjectNode();
        var contents = root.putArray("contents");
        var content0 = contents.addObject(); // Erstes Objekt mit Rolle "user"
        content0.put("role", "user");
        var parts = content0.putArray("parts"); // Enthält den eigentlichen Prompt
        parts.addObject().put("text", "Fasse den folgenden Text kurz und prägnant zusammen:\n\n" + clipped); //Anfragetext an Gemini

        String payload = MAPPER.writeValueAsString(root);

        // URI für den HTTP-Request aufbauen:
        URI uri = URI.create(
                "https://generativelanguage.googleapis.com/v1/models/"
                        + MODEL + ":generateContent?key=" + API_KEY
        );

        //HTTP-POST-Request vorbereiten:
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(60)) // maximal 60 s warten
                .header("Content-Type", "application/json") // JSON-Header
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        int attempts = 0;
        while (true) {
            attempts++;
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                return extractText(res.body());
            }
            // Falls zu viele Anfragen (429), exponentielles Backoff durchführen: je öfter request fehlschlägt, desto länger wird gewartet bis nächstes request
            if (res.statusCode() == 429 && attempts < 4) {
                long backoff = (long) Math.pow(2, attempts) * 500L; //attempts wird ^2 gerechnet und mit 500 (Millisekunden) multipliziert
                log.warn("Gemini 429, retrying in {} ms (attempt {}/{})", backoff, attempts, 3);
                Thread.sleep(backoff); // kurze Wartezeit vor erneutem Versuch
                continue;
            }
            throw new RuntimeException("Gemini API failed (" + res.statusCode() + "): " + res.body()); // Bei anderen Statuscodes (z. B. 400, 403, 500) Exception werfen
        }
    }

    private static String extractText(String body) throws Exception { //Extrahiert den eigentlichen Textinhalt ("summary") aus der Antwort der Gemini-API
        JsonNode root = MAPPER.readTree(body); // JSON einlesen
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray() && parts.size() > 0) {
            String txt = parts.get(0).path("text").asText("");
            if (!txt.isEmpty()) return txt.trim();
        }
        // Fallback für ältere API-Antworten mit anderem Format:
        JsonNode txt = root.at("/candidates/0/content/parts/0/text");
        if (!txt.isMissingNode()) return txt.asText().trim();
        return body; // last resort
    }

    //für neues Use Case :) Kategorisieren
    public static String classify(String text, java.util.List<String> categories) throws Exception {
        if (API_KEY == null || API_KEY.isBlank())
            throw new IllegalStateException("GEMINI_API_KEY not set!");

        String clipped = text == null ? "" : text;
        if (clipped.length() > 20000) clipped = clipped.substring(0, 20000);

        // Prompt für Klassifizierung:
        String prompt =
                "Ordne den folgenden Text in eine der folgenden Kategorien ein:\n" +
                        String.join(", ", categories) +
                        "\nGib NUR den Kategorienamen zurück.\n\n" +
                        clipped;

        // Request-Body wie bereits bei summarize():
        ObjectNode root = MAPPER.createObjectNode();
        var contents = root.putArray("contents");
        var content0 = contents.addObject();
        content0.put("role", "user");

        var parts = content0.putArray("parts");
        parts.addObject().put("text", prompt);

        String payload = MAPPER.writeValueAsString(root);

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
                String result = extractText(res.body());
                return result.trim();
            }

            if (res.statusCode() == 429 && attempts < 4) {
                long backoff = (long) Math.pow(2, attempts) * 500L;
                log.warn("Gemini 429 (classify), retrying in {} ms", backoff);
                Thread.sleep(backoff);
                continue;
            }

            throw new RuntimeException("Gemini API classify failed (" +
                    res.statusCode() + "): " + res.body());
        }
    }



}
