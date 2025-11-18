package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GeminiClientTest {

    private HttpClient httpClientMock;
    private HttpResponse<String> httpResponseMock;

    @BeforeEach
    void setUp() throws Exception {
        // Einen HttpClient\-Mock erstellen, damit keine echten HTTP\-Requests abgesetzt werden
        httpClientMock = Mockito.mock(HttpClient.class);
        httpResponseMock = Mockito.mock(HttpResponse.class);

        // Statisches Feld `HTTP` in `GeminiClient` per Reflection auf unseren Mock setzen
        Field httpField = GeminiClient.class.getDeclaredField("HTTP");
        httpField.setAccessible(true);
        httpField.set(null, httpClientMock);

        // Statisches Feld `API_KEY` in `GeminiClient` per Reflection auf einen Dummy\-Wert setzen
        Field apiKeyField = GeminiClient.class.getDeclaredField("API_KEY");
        apiKeyField.setAccessible(true);
        apiKeyField.set(null, "DUMMY_TEST_KEY");
    }

    @Test
    void summarize_throwsIfApiKeyMissing() throws Exception {
        // API\_KEY auf null setzen, um das Verhalten ohne gesetzten Key zu testen
        Field apiKeyField = GeminiClient.class.getDeclaredField("API_KEY");
        apiKeyField.setAccessible(true);
        apiKeyField.set(null, null);

        // Erwartung: IllegalStateException wird geworfen
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> GeminiClient.summarize("Testinhalt")
        );
        assertTrue(ex.getMessage().contains("GEMINI_API_KEY"));
    }

    @Test
    void summarize_returnsExtractedTextOn200() throws Exception {
        // Dummy\-JSON\-Antwort vorbereiten, die von `extractText` korrekt gelesen werden kann
        String jsonResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "Zusammenfassung aus der API" }
                        ]
                      }
                    }
                  ]
                }
                """;

        // Mock so konfigurieren, dass bei send() Statuscode 200 und unsere JSON\-Antwort zurückgegeben werden
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn(jsonResponse);
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);

        // Methode aufrufen
        String result = GeminiClient.summarize("Irgendein Text");

        // Erwartung: der Text aus der JSON\-Antwort wird zurückgegeben
        assertEquals("Zusammenfassung aus der API", result);
    }

    @Test
    void summarize_retriesOn429AndThenSucceeds() throws Exception {
        // Erste Antwort: 429 (Too Many Requests)
        HttpResponse<String> first = Mockito.mock(HttpResponse.class);
        when(first.statusCode()).thenReturn(429);
        when(first.body()).thenReturn("Too Many Requests");

        // Zweite Antwort: 200 mit gültiger JSON\-Payload
        HttpResponse<String> second = Mockito.mock(HttpResponse.class);
        when(second.statusCode()).thenReturn(200);
        when(second.body()).thenReturn("""
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "Nach Retry erfolgreich" }
                        ]
                      }
                    }
                  ]
                }
                """);

        // HttpClient so konfigurieren, dass zuerst 429, dann 200 zurückkommt
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(first)
                .thenReturn(second);

        // Aufruf der Methode; hier wird intern einmal gewartet (Backoff) und ein zweites Mal gesendet
        String result = GeminiClient.summarize("Testinhalt für Retry");

        // Erwartung: Ergebnis aus der zweiten, erfolgreichen Antwort
        assertEquals("Nach Retry erfolgreich", result);

        // Sicherstellen, dass send() tatsächlich zweimal aufgerufen wurde
        verify(httpClientMock, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void summarize_throwsOnNon200Non429() throws Exception {
        // Eine 500er\-Antwort simulieren
        when(httpResponseMock.statusCode()).thenReturn(500);
        when(httpResponseMock.body()).thenReturn("Serverfehler");
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);

        // Erwartung: RuntimeException mit passender Fehlermeldung
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> GeminiClient.summarize("Fehlerfall")
        );
        assertTrue(ex.getMessage().contains("Gemini API failed (500)"));
    }
}