package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class RabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(RabbitConsumer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper(); //Konvertierung zwisachen JSON und Objekten

    private final String inputQueue = System.getenv().getOrDefault("GENAI_INPUT_QUEUE", "ocr.results"); //hörst auf diese Queue
    private final String outputQueue = System.getenv().getOrDefault("GENAI_OUTPUT_QUEUE", "genai.results"); //schickt results (Zusammenfassung also Output von Gemini) in diese Queue
    private final String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
    private final String user = System.getenv().getOrDefault("RABBITMQ_USER", "user");
    private final String pass = System.getenv().getOrDefault("RABBITMQ_PASS", "pass");
    private final String model = System.getenv().getOrDefault("GENAI_MODEL", "gemini-2.0-flash"); //Name des Modells, das an Gemini geschickt wird.

    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(user);
            factory.setPassword(pass);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(2000);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.queueDeclare(inputQueue, true, false, false, null);
                channel.queueDeclare(outputQueue, true, false, false, null);

                log.info("Waiting for OCR results on queue: {}", inputQueue);

                DeliverCallback deliver = (tag, delivery) -> {
                    String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    try {
                        ObjectNode msg = (ObjectNode) MAPPER.readTree(raw);
                        String docId = msg.path("documentId").asText(null);
                        String text = msg.path("text").asText(msg.path("textExcerpt").asText("")); // fallback
                        if (docId == null) throw new IllegalArgumentException("documentId missing");

                        log.info("OCR result received for {} ({} chars)", docId, text.length());

                        String summary = GeminiClient.summarize(text);
                        int tokensApprox = Math.max(1, summary.length() / 4);

                        ObjectNode out = MAPPER.createObjectNode();
                        out.put("documentId", docId);
                        out.put("summary", summary);
                        out.put("model", model);
                        out.put("tokens", tokensApprox);
                        out.put("createdAt", Instant.now().toString());
                        out.putNull("error");

                        channel.basicPublish(
                                "", // default exchange
                                outputQueue,
                                new AMQP.BasicProperties.Builder()
                                        .contentType("application/json")
                                        .deliveryMode(2) // persistent
                                        .build(),
                                out.toString().getBytes(StandardCharsets.UTF_8)
                        );
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        log.info("Published GenAI result to {}", outputQueue);

                    } catch (Exception e) {
                        log.error("Error while processing OCR message: {}", e.getMessage(), e);
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                };

                channel.basicQos(1);
                channel.basicConsume(inputQueue, false, deliver, tag -> {});
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            log.error("Fatal worker error", e);
        }
    }
}