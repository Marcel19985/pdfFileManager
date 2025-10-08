package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.dto.OcrJobDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.OCR_QUEUE;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository repo;
    private final DocumentMapper mapper;
    private final RabbitTemplate rabbit;

    @Override
    public DocumentDto upload(MultipartFile file, String title, String description) {
        // TODO: Datei (Multipart file) speichern – vorerst nur Metadaten
        DocumentEntity document = new DocumentEntity();
        document.setTitle(title);
        document.setDescription(description);
        // createdAt/updatedAt via @PrePersist
        document = repo.save(document);

        // einfache Payload (wird via Jackson2JsonMessageConverter zu JSON)
        var payload = java.util.Map.of(
                "documentId", document.getId(),
                "title", title,
                "createdAt", document.getCreatedAt()
        );
        rabbit.convertAndSend("", OCR_QUEUE, payload); // Default-Exchange, routingKey = Queue

        return mapper.toDto(document);
    }

    @Override
    public List<DocumentDto> getAll() {
        return mapper.toDto(repo.findAll());
    }

    @Override
    public DocumentDto getById(UUID id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public void deleteById(UUID id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Document not found: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public DocumentDto updateDocument(UUID id, String title, String description) {
        DocumentEntity document = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
        document.setTitle(title);
        document.setDescription(description);
        // updatedAt wird automatisch durch @PreUpdate gesetzt
        document = repo.save(document);
        return mapper.toDto(document);
    }
}
