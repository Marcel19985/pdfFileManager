package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.service.DocumentService;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents") //http://localhost:8081/api/documents in browser
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentDto> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") @NotBlank @Size(max = 200) String title,
            @RequestPart(name = "description", required = false) @Size(max = 2000) String description
    ) {
        DocumentDto dto = service.upload(file, title, description);
        return ResponseEntity.created(URI.create("/api/documents/" + dto.id())).body(dto);
    }

    @GetMapping
    public List<DocumentDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getById(@PathVariable UUID id) {
        DocumentDto dto = service.getById(id);
        return (dto != null) ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
