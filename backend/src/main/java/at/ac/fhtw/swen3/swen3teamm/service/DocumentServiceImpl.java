package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.dto.OcrJobDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.OCR_QUEUE; //in MessagingConfig definiert
import at.ac.fhtw.swen3.swen3teamm.service.MinioService;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository repo;
    private final DocumentMapper mapper;
    private final RabbitTemplate rabbit;
    private final MinioService minioService;

    @Override
    public DocumentDto upload(MultipartFile file, String title, String description) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File must not be empty");
        }

        // 1️⃣ Neues DocumentEntity anlegen
        DocumentEntity document = new DocumentEntity();
        document.setTitle(title);
        document.setDescription(description);

        // 2️⃣ Erst in DB speichern → ID wird generiert
        document = repo.save(document);
        log.info("Document persisted id={}", document.getId());

        // 3️⃣ Datei in MinIO hochladen
        try {
            String objectName = document.getId() + ".pdf";
            minioService.upload(objectName, file.getInputStream(), file.getSize());
            log.info("Uploaded file to MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO for document {}", document.getId(), e);
            //throw new StorageException("Failed to store file in MinIO", e);
        }

        // 4️⃣ OCR-Job vorbereiten & senden
        OcrJobDto job = new OcrJobDto(document.getId(), title, Instant.now());

        try {
            rabbit.convertAndSend("", OCR_QUEUE, job);
            log.info("Published OCR job docId={} queue={}", document.getId(), OCR_QUEUE);
        } catch (AmqpException ex) {
            log.error("Failed to publish OCR job docId={} queue={}", document.getId(), OCR_QUEUE, ex);
            throw new MessagingException("Failed to publish OCR job for doc " + document.getId(), ex);
        }

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
                .orElseThrow(() -> new DocumentNotFoundException(id.toString()));
    }

    @Override
    public void deleteById(UUID id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Document not found: " + id);
        }

        String objectName = id + ".pdf";
        try {
            minioService.delete(objectName);
            log.info("Deleted file from MinIO: {}", objectName);
        } catch (Exception e) {
            log.warn("Failed to delete file from MinIO: {}", objectName, e);
        }

        repo.deleteById(id);
        log.info("Document deleted id={}", id);
    }

    @Override
    public DocumentDto updateDocument(UUID id, String title, String description) {
        DocumentEntity document = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
        document.setTitle(title);
        document.setDescription(description);
        // updatedAt wird automatisch durch @PreUpdate gesetzt
        document = repo.save(document);
        log.info("Document updated id={}", id);
        return mapper.toDto(document);
    }

    @Override
    public InputStream downloadFromMinio(String objectName) {
        return minioService.download(objectName);
    }

}
