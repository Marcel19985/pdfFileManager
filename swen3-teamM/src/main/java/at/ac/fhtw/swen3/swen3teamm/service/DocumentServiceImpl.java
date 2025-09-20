package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persiatance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persiatance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository repo;
    private final DocumentMapper mapper;

    @Override
    public DocumentDto upload(MultipartFile file, String title, String description) {
        // TODO: Datei speichern (Filesystem/Blob/S3) – vorerst nur Metadaten
        DocumentEntity document = new DocumentEntity();
        document.setTitle(title);
        document.setDescription(description);
        // createdAt/updatedAt via @PrePersist
        document = repo.save(document);
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
}
