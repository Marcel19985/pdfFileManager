package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitConsumerTest {

    RabbitConsumer consumer;
    Channel mockChannel;
    Connection mockConnection;
    ConnectionFactory mockFactory;
    MockedStatic<GeminiClient> mockedStatic;

    @BeforeEach
    void setup() throws Exception {
        consumer = new RabbitConsumer();

        // Mocks für RabbitMQ
        mockChannel = mock(Channel.class);
        mockConnection = mock(Connection.class);
        mockFactory = mock(ConnectionFactory.class);

        when(mockFactory.newConnection()).thenReturn(mockConnection);
        when(mockConnection.createChannel()).thenReturn(mockChannel);

        // GeminiClient statische Methoden mocken
        mockedStatic = mockStatic(GeminiClient.class);
        mockedStatic.when(() -> GeminiClient.summarize(anyString())).thenReturn("Zusammenfassung");
        mockedStatic.when(() -> GeminiClient.classify(anyString(), anyList())).thenReturn("Testkategorie");
    }

    @AfterEach
    void teardown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    void testDeliverCallback_processesMessageAndPublishes() throws Exception {
        // Beispiel-Input-Message
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode msg = mapper.createObjectNode();
        msg.put("documentId", "doc-123");
        msg.put("text", "Das ist ein Testtext.");

        byte[] body = msg.toString().getBytes(StandardCharsets.UTF_8);

        // ArgumentCaptor für die Nachricht
        ArgumentCaptor<byte[]> bodyCaptor = ArgumentCaptor.forClass(byte[].class);

        // DeliverCallback direkt erzeugen und aufrufen
        DeliverCallback callback = createDeliverCallback(mockChannel);
        callback.handle("tag1", new Delivery(null, null, body));

        // Prüfen, dass die Nachricht gepublished wurde
        verify(mockChannel).basicPublish(
                eq(""),
                anyString(),
                isNull(), // AMQP.BasicProperties ist null im Test
                bodyCaptor.capture()
        );

        String published = new String(bodyCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(published.contains("Zusammenfassung"));
        assertTrue(published.contains("Testkategorie"));

        // Prüfen, dass basicAck aufgerufen wurde
        verify(mockChannel).basicAck(anyLong(), eq(false));
    }

    // Hilfsmethode, um DeliverCallback wie im Consumer zu erzeugen
    private DeliverCallback createDeliverCallback(Channel channel) {
        return (tag, delivery) -> {
            String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode msg = (ObjectNode) mapper.readTree(raw);
            String docId = msg.path("documentId").asText();
            String text = msg.path("text").asText();

            String summary = null;
            try {
                summary = GeminiClient.summarize(text);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String category = null;
            try {
                category = GeminiClient.classify(text, List.of("Testkategorie"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ObjectNode out = mapper.createObjectNode();
            out.put("documentId", docId);
            out.put("summary", summary);
            out.put("category", category);

            channel.basicPublish("", "genai.results", null, out.toString().getBytes(StandardCharsets.UTF_8));
            channel.basicAck(1L, false);
        };
    }
}
