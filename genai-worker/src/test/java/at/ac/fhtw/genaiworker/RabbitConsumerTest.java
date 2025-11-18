package at.ac.fhtw.genaiworker;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests für RabbitConsumer, ohne die Klasse selbst zu verändern.
 *
 * Idee:
 * - new ConnectionFactory() wird per mockConstruction abgefangen
 * - Die darauf folgende Connection + Channel werden gemockt
 * - basicConsume(...) liefert uns den DeliverCallback, den wir manuell aufrufen
 * - GeminiClient.summarize(...) wird als statische Methode gemockt
 */
class RabbitConsumerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Happy Path:
     * - Nachricht mit documentId + text
     * - Gemini liefert eine Zusammenfassung
     * - Consumer published Ergebnis und acked die Nachricht
     */
    @Test
    void handleDelivery_publishesResultAndAcksOnSuccess() throws Exception {
        // Mock-Channel (kein echtes RabbitMQ)
        Channel channelMock = mock(Channel.class);

        // Eingangs-JSON wie aus der ocr.results-Queue
        String inputJson = """
                {
                  "documentId": "doc-123",
                  "text": "Das ist der volle OCR Text"
                }
                """;
        byte[] body = inputJson.getBytes(StandardCharsets.UTF_8);

        // Envelope mit DeliveryTag (wichtig für ack/nack)
        Envelope envelope = new Envelope(1L, false, "", "ocr.results");
        AMQP.BasicProperties props = new AMQP.BasicProperties();

        Delivery delivery = new Delivery(envelope, props, body);

        RabbitConsumer consumer = new RabbitConsumer();

        // Statisches Mock für GeminiClient.summarize(...)
        try (MockedStatic<GeminiClient> geminiMock = Mockito.mockStatic(GeminiClient.class)) {
            geminiMock.when(() -> GeminiClient.summarize("Das ist der volle OCR Text"))
                    .thenReturn("Das ist die Zusammenfassung");

            // Methode direkt testen
            consumer.handleDelivery(channelMock, delivery);

            // Sicherstellen, dass summarize mit dem Text aufgerufen wurde
            geminiMock.verify(() -> GeminiClient.summarize("Das ist der volle OCR Text"));

            // ArgumentCaptor, um das publizierte JSON aus basicPublish auszulesen
            ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);

            verify(channelMock).basicPublish(
                    eq(""),                       // default exchange
                    eq("genai.results"),          // Default-Output-Queue aus deiner Klasse
                    any(AMQP.BasicProperties.class),
                    bodyCaptor.capture()
            );

            String publishedJson = new String(bodyCaptor.getValue(), StandardCharsets.UTF_8);
            ObjectNode out = (ObjectNode) mapper.readTree(publishedJson);

            assertEquals("doc-123", out.path("documentId").asText());
            assertEquals("Das ist die Zusammenfassung", out.path("summary").asText());
            assertEquals("gemini-2.0-flash", out.path("model").asText()); // Default aus deinem Code

            int expectedTokens = Math.max(1, "Das ist die Zusammenfassung".length() / 4);
            assertEquals(expectedTokens, out.path("tokens").asInt());

            assertFalse(out.path("createdAt").asText().isEmpty());
            assertTrue(out.path("error").isNull());

            // Ack muss mit dem richtigen DeliveryTag aufgerufen werden
            verify(channelMock).basicAck(1L, false);
        }
    }

    /**
     * Fehlerfall:
     * - Gemini wirft eine Exception
     * - Consumer loggt den Fehler und ruft basicNack mit requeue=true auf
     */
    @Test
    void handleDelivery_nacksOnGeminiError() throws Exception {
        Channel channelMock = mock(Channel.class);

        String inputJson = """
                {
                  "documentId": "doc-999",
                  "text": "Fehlerhafter Text"
                }
                """;
        byte[] body = inputJson.getBytes(StandardCharsets.UTF_8);
        Envelope envelope = new Envelope(42L, false, "", "ocr.results");
        Delivery delivery = new Delivery(envelope, new AMQP.BasicProperties(), body);

        RabbitConsumer consumer = new RabbitConsumer();

        try (MockedStatic<GeminiClient> geminiMock = Mockito.mockStatic(GeminiClient.class)) {
            geminiMock.when(() -> GeminiClient.summarize("Fehlerhafter Text"))
                    .thenThrow(new RuntimeException("LLM kaputt"));

            consumer.handleDelivery(channelMock, delivery);

            // Es sollte ein Nack mit requeue=true erfolgen
            verify(channelMock).basicNack(42L, false, true);

            // Kein basicPublish / basicAck bei Fehler
            verify(channelMock, never()).basicPublish(anyString(), anyString(), any(), any());
            verify(channelMock, never()).basicAck(anyLong(), anyBoolean());
        }
    }

    /**
     * Fehlerfall:
     * - documentId fehlt komplett
     * - IllegalArgumentException wird geworfen
     * - Der catch-Block sorgt dafür, dass nacked wird
     */
    @Test
    void handleDelivery_nacksWhenDocumentIdMissing() throws Exception {
        Channel channelMock = mock(Channel.class);

        String inputJson = """
                {
                  "text": "Text ohne Dokument-ID"
                }
                """;
        byte[] body = inputJson.getBytes(StandardCharsets.UTF_8);
        Envelope envelope = new Envelope(7L, false, "", "ocr.results");
        Delivery delivery = new Delivery(envelope, new AMQP.BasicProperties(), body);

        RabbitConsumer consumer = new RabbitConsumer();

        try (MockedStatic<GeminiClient> ignored = Mockito.mockStatic(GeminiClient.class)) {
            consumer.handleDelivery(channelMock, delivery);

            // Es sollte wegen fehlender documentId ein Nack kommen
            verify(channelMock).basicNack(7L, false, true);

            // summarize sollte gar nicht aufgerufen werden
            ignored.verifyNoInteractions();
        }
    }
}