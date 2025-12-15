package at.ac.fhtw.genaiworker;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class GeminiClientTest {

    HttpClient mockHttp;

    @SuppressWarnings("unchecked")
    HttpResponse<String> mockResponse =
            (HttpResponse<String>) Mockito.mock(HttpResponse.class);

    @BeforeEach
    void setup() {
        mockHttp = Mockito.mock(HttpClient.class);

        GeminiClient.httpClient = mockHttp;
        GeminiClient.apiKey = "test-key"; // 🔑 Test-API-Key
    }

    @Test
    void extractText_validResponse() throws Exception {
        String json = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "Kurze Zusammenfassung" }
                ]
              }
            }
          ]
        }
        """;

        String result = GeminiClient.extractText(json);

        assertEquals("Kurze Zusammenfassung", result);
    }

    @Test
    void summarize_successful() throws Exception {
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn("""
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "Zusammenfassung OK" }
                ]
              }
            }
          ]
        }
        """);

        Mockito.when(
                mockHttp.send(
                        Mockito.any(HttpRequest.class),
                        Mockito.<HttpResponse.BodyHandler<String>>any()
                )
        ).thenReturn(mockResponse);

        String result = GeminiClient.summarize("Testtext");

        assertEquals("Zusammenfassung OK", result);
    }

    @Test
    void classify_returnsCategory() throws Exception {
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn("""
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  { "text": "Rechnung" }
                ]
              }
            }
          ]
        }
        """);

        Mockito.when(
                mockHttp.send(
                        Mockito.any(HttpRequest.class),
                        Mockito.<HttpResponse.BodyHandler<String>>any()
                )
        ).thenReturn(mockResponse);

        String result = GeminiClient.classify(
                "Das ist eine Rechnung",
                List.of("Rechnung", "Vertrag", "Sonstiges")
        );

        assertEquals("Rechnung", result);
    }
}
