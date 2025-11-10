package at.ac.fhtw.genaiworker;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RabbitConsumer {
    private static final Logger log = LoggerFactory.getLogger(RabbitConsumer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String queueName = System.getenv().getOrDefault("GENAI_INPUT_QUEUE", "ocr.results");
    private final String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
    private final String user = System.getenv().getOrDefault("RABBITMQ_USER", "user");
    private final String pass = System.getenv().getOrDefault("RABBITMQ_PASS", "pass");
    private final String model = System.getenv().getOrDefault("GENAI_MODEL", "gemini-2.0-flash");

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

                channel.queueDeclare(queueName, true, false, false, null);
                log.info("Waiting for OCR results on queue: {}", queueName);

                DeliverCallback deliver = (tag, delivery) -> {
                    String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    try {
                        ObjectNode msg = (ObjectNode) MAPPER.readTree(raw);
                        String docId = msg.path("documentId").asText(null);
                        String text = msg.path("textExcerpt").asText(""); // falls du später Volltext lieferst: "text"
                        if (docId == null) throw new IllegalArgumentException("documentId missing in message");

                        log.info("OCR result received for {} ({} chars)", docId, text.length());

                        String summary = GeminiClient.summarize(text);
                        int tokensApprox = Math.max(1, summary.length() / 4); // simple approx

                        RestClient.sendSummaryToBackend(docId, summary, model, tokensApprox);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception e) {
                        log.error("Error while processing OCR message: {}", e.getMessage(), e);
                        // requeue once after a short delay; in real life use a DLQ
                        try { Thread.sleep(Duration.ofSeconds(2)); } catch (InterruptedException ignored) {}
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                };

                channel.basicConsume(queueName, false, deliver, tag -> {});
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            log.error("Fatal worker error", e);
        }
    }
}
