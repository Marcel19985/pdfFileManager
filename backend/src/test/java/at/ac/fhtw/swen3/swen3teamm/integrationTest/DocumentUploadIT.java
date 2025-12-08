package at.ac.fhtw.swen3.swen3teamm.integrationTest;

import at.ac.fhtw.swen3.swen3teamm.Swen3TeamMApplication;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.ElasticsearchService;
import at.ac.fhtw.swen3.swen3teamm.service.MinioService;
import at.ac.fhtw.swen3.swen3teamm.service.dto.OcrJobDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.OCR_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für den Use Case „Document Upload“.
 * Dieser Test prüft:
 *   - HTTP-Verarbeitung eines multipart/form-data Uploads
 *   - Speicherung des Dokumentes in der Datenbank
 *   - korrekter Aufruf des MinIO-Services
 *   - Versand eines OCR-Job-Eintrags an RabbitMQ
 * Externe Systeme (MinIO, RabbitMQ, Elasticsearch) werden dabei gemockt.
 */

@SpringBootTest(classes = Swen3TeamMApplication.class) // Startet echten Spring-Boot-Kontext
@AutoConfigureMockMvc // Aktiviert MockMvc für HTTP-Tests
@Transactional // Jede Testmethode läuft in einer Transaktion (Rollback am Ende)
@TestPropertySource(properties = { // Konfiguration einer vollständigen In-Memory-H2-Datenbank für Tests
        // H2-In-Memory-DB für Tests
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // optional: Logging leiser
        "spring.jpa.show-sql=false"
})
public class DocumentUploadIT {

    @Autowired
    MockMvc mockMvc; // Simuliert echte HTTP-Requests ohne Webserver

    @Autowired
    DocumentRepository documentRepository;

    // Diese Beans werden im Test ersetzt, damit KEIN echtes MinIO / RabbitMQ / Elasticsearch gebraucht wird
    @MockitoBean
    MinioService minioService;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    // wird im Upload selbst nicht verwendet, aber als Bean vorhanden
    @MockitoBean
    ElasticsearchService elasticsearchService;

    @Test
    void uploadDocument_createsDocumentAndPublishesOcrJob() throws Exception {
        // --- Arrange: Testdaten vorbereiten ---
        byte[] pdfBytes = "DUMMY PDF CONTENT".getBytes(); // Inhalt des Test-PDF

        // Simuliert das "file"-Upload-Feld:
        MockMultipartFile filePart = new MockMultipartFile(
                "file",                 // muss "file" heißen -> @RequestPart("file")
                "test.pdf",
                "application/pdf",
                pdfBytes
        );

        // Simuliert das "title"-Feld:
        MockMultipartFile titlePart = new MockMultipartFile(
                "title",
                "",
                "text/plain",
                "Testdokument".getBytes()
        );

        // Simuliert das "description"-Feld:
        MockMultipartFile descPart = new MockMultipartFile(
                "description",
                "",
                "text/plain",
                "Beschreibung für das Testdokument".getBytes()
        );

        long beforeCount = documentRepository.count();

        // --- Act: HTTP-Request an /api/documents ausführen ---
        MvcResult result = mockMvc.perform(
                        multipart("/api/documents") // multipart/form-data POST
                                .file(filePart)
                                .file(titlePart)
                                .file(descPart)
                )
                .andExpect(status().isCreated()) // API muss 201 Created liefern
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/documents/.+")))
                .andExpect(jsonPath("$.id").exists()) // ID muss im JSON stehen
                .andExpect(jsonPath("$.title").value("Testdokument"))
                .andReturn();

        // --- Extract: erzeugte Dokument-ID aus dem Location-Header lesen ---
        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
        String idStr = location.substring(location.lastIndexOf('/') + 1);
        UUID documentId = UUID.fromString(idStr); // Validiert automatisch die UUID

        // --- Assert: Dokument ist wirklich in der Datenbank angekommen ---
        assertThat(documentRepository.count()).isEqualTo(beforeCount + 1);
        assertThat(documentRepository.findById(documentId)).isPresent();

        // --- Assert: Datei wurde an MinIO hochgeladen ---
        verify(minioService).upload(
                eq(documentId + ".pdf"), // Der Objektname muss der ID entsprechen
                any(), // InputStream wird nicht genauer geprüft
                eq((long) pdfBytes.length) // korrekte Dateigröße
        );

        // --- Assert: OCR-Job wurde korrekt an RabbitMQ publiziert ---

        // Prüft, dass convertAndSend("", "ocr.jobs", payload) aufgerufen wurde:
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq(""), eq(OCR_QUEUE), payloadCaptor.capture());

        Object payload = payloadCaptor.getValue();
        assertThat(payload).isInstanceOf(OcrJobDto.class); // richtige Objektklasse
        OcrJobDto job = (OcrJobDto) payload;
        assertThat(job.documentId()).isEqualTo(documentId); // Muss die erzeugte ID tragen
        assertThat(job.title()).isEqualTo("Testdokument"); // Muss den Titel aus dem Upload tragen
    }

}
