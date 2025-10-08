package at.ac.fhtw.swen3.swen3teamm.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String OCR_QUEUE = "ocr.jobs";
    @Bean Queue ocrQueue() { return new Queue(OCR_QUEUE, true); } // durable
}
