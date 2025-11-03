package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentDto upload(MultipartFile file, String title, String description);
    List<DocumentDto> getAll();
    DocumentDto getById(UUID id);
    void deleteById(UUID id);
    DocumentDto updateDocument(UUID id, String title, String description);
    InputStream downloadFromMinio(String objectName);
    void updateSummary(UUID id, String summary, String model, Integer tokens);
}
