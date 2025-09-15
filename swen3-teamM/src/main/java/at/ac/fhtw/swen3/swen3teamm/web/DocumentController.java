package at.ac.fhtw.swen3.swen3teamm.web;

import at.ac.fhtw.swen3.swen3teamm.dto.DocumentCreatedDto;
import at.ac.fhtw.swen3.swen3teamm.entity.Document;
import at.ac.fhtw.swen3.swen3teamm.repository.DocumentRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/documents") //http://localhost:8080/api/documents in browser
public class DocumentController {

    private final DocumentRepository repo;
    public DocumentController(DocumentRepository repo) {
        this.repo = repo;
    }

    //PostMapping: ist für HTTP POST Anfragen (HTTP Endpunkt); consumes = "multipart/form-data": erwartet Multipart also z.B. Datei + Metadaten
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentCreatedDto> upload(
            @RequestPart("file") MultipartFile file, //Multipart File: Dateiname, ContentType und Bytes
            @RequestPart("title") @NotBlank @Size(max = 200) String title, //mandatory
            @RequestPart(name = "description", required = false) @Size(max = 2000) String description //optional
    ) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setDescription(description);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());
        repo.save(doc);

        DocumentCreatedDto dto = new DocumentCreatedDto(doc.getId(), doc.getTitle(), "UPLOADED", doc.getCreatedAt());
        return ResponseEntity.created(URI.create("/api/documents/" + doc.getId())).body(dto);
    }
}
