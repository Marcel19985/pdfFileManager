package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.service.dto.GenAiResultDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.GENAI_RESULTS_QUEUE;

@Service
@RequiredArgsConstructor
public class GenAiResultListener {

    private static final Logger log = LoggerFactory.getLogger(GenAiResultListener.class);
    private final DocumentService documentService;

    @RabbitListener(queues = GENAI_RESULTS_QUEUE)
    public void handle(GenAiResultDto result) {
        try {
            if (result == null || result.documentId() == null) {
                log.warn("Invalid GenAI result: {}", result);
                return;
            }
            if (result.error() != null && !result.error().isBlank()) {
                log.error("GenAI error for {}: {}", result.documentId(), result.error());
                return; // optional: Statusfeld setzen, Retry-Strategie, DLQ etc.
            }
            documentService.updateSummary(
                    UUID.fromString(result.documentId()),
                    result.summary(),
                    result.model() != null ? result.model() : "gemini-2.0-flash",
                    result.tokens() != null ? result.tokens() : 0
            );
            log.info("Summary stored for document {}", result.documentId());
        } catch (Exception e) {
            log.error("Failed to store summary for {}: {}",
                    result != null ? result.documentId() : "null", e.getMessage(), e);
            // hier könntest du DLQ o. Retry per nack in einem manuell konfigurierten Container nutzen
        }
    }
}
