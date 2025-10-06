package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceImplTest {

    private DocumentRepository repo;
    private DocumentMapper mapper;
    private DocumentServiceImpl service;

    @BeforeEach
    void setUp() { //alles mocken
        repo = mock(DocumentRepository.class);
        mapper = mock(DocumentMapper.class);
        service = new DocumentServiceImpl(repo, mapper);
    }

    @Test
    void upload_shouldSaveDocumentAndReturnDto() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy".getBytes());
        DocumentEntity entity = new DocumentEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Test Title");
        entity.setDescription("Desc");
        entity.setCreatedAt(Instant.now());

        DocumentDto dto = new DocumentDto(entity.getId(), "Test Title", null, "UPLOADED", entity.getCreatedAt());

        when(repo.save(any(DocumentEntity.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        DocumentDto result = service.upload(file, "Test Title", "Desc");

        assertNotNull(result);
        assertEquals("Test Title", result.title());
        assertEquals("UPLOADED", result.status());
        verify(repo, times(1)).save(any(DocumentEntity.class));
        verify(mapper, times(1)).toDto(entity);
    }

    @Test
    void getAll_shouldReturnListOfDtos() {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("Doc1");
        entity.setCreatedAt(Instant.now());

        DocumentDto dto = new DocumentDto(entity.getId(), "Doc1", "null", "UPLOADED", entity.getCreatedAt());

        when(repo.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(List.of(entity))).thenReturn(List.of(dto));

        List<DocumentDto> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Doc1", result.get(0).title());
    }

    @Test
    void getById_shouldReturnDtoIfFound() {
        UUID id = UUID.randomUUID();
        DocumentEntity entity = new DocumentEntity();
        entity.setId(id);
        entity.setTitle("DocX");
        entity.setCreatedAt(Instant.now());

        DocumentDto dto = new DocumentDto(id, "DocX", "null", "UPLOADED", entity.getCreatedAt());

        when(repo.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        DocumentDto result = service.getById(id);

        assertNotNull(result);
        assertEquals("DocX", result.title());
    }

    @Test
    void getById_shouldReturnNullIfNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        DocumentDto result = service.getById(id);

        assertNull(result);
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(true);
        doNothing().when(repo).deleteById(id);

        assertDoesNotThrow(() -> service.deleteById(id));

        verify(repo, times(1)).deleteById(id);
    }

    @Test
    void deleteById_shouldThrowIfNotExists() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.deleteById(id));
        assertTrue(ex.getMessage().contains("Document not found"));
    }
}
