package at.ac.fhtw.swen3.swen3teamm.web;

import at.ac.fhtw.swen3.swen3teamm.dto.DocumentCreatedDto;
import at.ac.fhtw.swen3.swen3teamm.entity.Document;
import at.ac.fhtw.swen3.swen3teamm.repository.DocumentRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    //GET all Document
    @GetMapping
    public List<DocumentCreatedDto> getAll() {
        return repo.findAll().stream()
                .map(d -> new DocumentCreatedDto(d.getId(), d.getTitle(), "UPLOADED", d.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // GET document by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentCreatedDto> getById(@PathVariable UUID id) {
        Optional<Document> doc = repo.findById(id);
        return doc.map(d -> ResponseEntity.ok(
                        new DocumentCreatedDto(d.getId(), d.getTitle(), "UPLOADED", d.getCreatedAt())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE document by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.noContent().build(); // 204
        } else {
            return ResponseEntity.notFound().build(); // 404
        }
    }

}
