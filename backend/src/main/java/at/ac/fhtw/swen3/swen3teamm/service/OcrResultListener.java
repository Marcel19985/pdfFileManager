package at.ac.fhtw.swen3.swen3teamm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

/*@Service
@RequiredArgsConstructor
public class OcrResultListener { //war eher zum Testen gedacht, nun hört der RabbitConsumer in genai-worker auf OCR-Ergebnisse

    public record OcrResultDto(String documentId, String status, String textExcerpt, String error, Instant processedAt) {}

    @RabbitListener(queues = "ocr.results")
    public void handle(OcrResultDto result) {
        // TODO: Status in DB setzen, optional Text speichern/loggen
        System.out.println("[OCR RESULT] " + result);
        // Beispiel:
        // repo.findById(UUID.fromString(result.documentId()))
        //     .ifPresent(doc -> { doc.setStatus(result.status()); repo.save(doc); });
    }
}*/
