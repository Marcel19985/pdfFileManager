package at.ac.fhtw.genaiworker;

import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;

public class RabbitConsumer {
    private final String queueName = System.getenv().getOrDefault("GENAI_INPUT_QUEUE", "ocr.results");
    private final String host = System.getenv().getOrDefault("RABBITMQ_HOST", "rabbitmq");
    private final String user = System.getenv().getOrDefault("RABBITMQ_USER", "user");
    private final String pass = System.getenv().getOrDefault("RABBITMQ_PASS", "pass");

    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(user);
            factory.setPassword(pass);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.queueDeclare(queueName, true, false, false, null);
                System.out.println("📡 Waiting for OCR results on queue: " + queueName);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println("📄 Received OCR result: " + message.substring(0, Math.min(200, message.length())));

                    try {
                        // 1️⃣ Anfrage an Gemini
                        String summary = GeminiClient.summarize(message);

                        // 2️⃣ An Backend senden
                        RestClient.sendSummaryToBackend(summary);

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception e) {
                        System.err.println("❌ Error while processing: " + e.getMessage());
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                };

                channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});
                Thread.currentThread().join(); // keep running
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
