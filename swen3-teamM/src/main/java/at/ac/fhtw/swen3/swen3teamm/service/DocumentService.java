package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentDto upload(MultipartFile file, String title, String description);
    List<DocumentDto> getAll();
    DocumentDto getById(UUID id);
    void deleteById(UUID id);
}
