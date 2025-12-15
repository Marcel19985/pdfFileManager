package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.CategoryRepository;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.dto.OcrJobDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.OCR_QUEUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock DocumentRepository repo;
    @Mock DocumentMapper mapper;
    @Mock RabbitTemplate rabbit;
    @Mock MinioService minio;
    @Mock ElasticsearchService elasticsearchService;
    @Mock CategoryRepository categoryRepo;


    @InjectMocks DocumentServiceImpl service;

    // --- Upload ---

    @Test
    void upload_success_persists_uploadsToMinio_and_publishesOcrJob() throws Exception {
        var bytes = new byte[]{1,2,3,4};
        var file = new MockMultipartFile("file", "test.pdf", "application/pdf", bytes);
        var entity = newEntity("Test Title", "Desc");
        when(repo.save(any(DocumentEntity.class))).thenReturn(entity);

        // Mapper gibt DTO zurück
        when(mapper.toDto(any(DocumentEntity.class))).thenAnswer(inv -> {
            DocumentEntity e = inv.getArgument(0);
            return new DocumentDto(
                    e.getId(),
                    e.getTitle(),
                    e.getDescription(),
                    e.getCreatedAt(),
                    "UPLOADED",
                    e.getCategory() != null ? e.getCategory().getId() : null,
                    e.getCategory() != null ? e.getCategory().getName() : null
            );
        });

        ArgumentCaptor<OcrJobDto> jobCaptor = ArgumentCaptor.forClass(OcrJobDto.class);

        var result = service.upload(file, "Test Title", "Desc");

        assertNotNull(result);
        assertEquals("Test Title", result.title());

        verify(repo, times(1)).save(any(DocumentEntity.class));
        verify(minio, times(1)).upload(eq(entity.getId() + ".pdf"), any(InputStream.class), eq((long) bytes.length));
        verify(rabbit, times(1)).convertAndSend(eq(""), eq(OCR_QUEUE), jobCaptor.capture());
        assertEquals(entity.getId(), jobCaptor.getValue().documentId());
        verify(mapper, times(1)).toDto(entity);
    }

    @Test
    void upload_withEmptyFile_throwsValidationException() {
        var empty = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(ValidationException.class, () -> service.upload(empty, "t", "d"));
        verifyNoInteractions(repo, minio, rabbit, mapper);
    }

    @Test
    void upload_minioFails_but_stillPublishes_and_returnsDto() throws Exception {
        var bytes = new byte[]{1,2};
        var file = new MockMultipartFile("file", "a.pdf", "application/pdf", bytes);
        var entity = newEntity("t", "d");
        when(repo.save(any())).thenReturn(entity);
        doThrow(new RuntimeException("minio down")).when(minio).upload(anyString(), any(InputStream.class), anyLong());

        when(mapper.toDto(any(DocumentEntity.class))).thenReturn(
                new DocumentDto(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getCreatedAt(), "UPLOADED", null, null)
        );

        assertDoesNotThrow(() -> service.upload(file, "t", "d"));
        verify(rabbit).convertAndSend(eq(""), eq(OCR_QUEUE), any(OcrJobDto.class));
    }

    @Test
    void upload_publishFails_throwsMessagingException() throws Exception {
        var file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
        var entity = newEntity("t", "d");
        when(repo.save(any())).thenReturn(entity);
        doNothing().when(minio).upload(anyString(), any(InputStream.class), anyLong());
        doThrow(new AmqpException("down")).when(rabbit).convertAndSend(eq(""), eq(OCR_QUEUE), any(OcrJobDto.class));

        assertThrows(MessagingException.class, () -> service.upload(file, "t", "d"));

        verify(repo).save(any());
        verify(minio).upload(eq(entity.getId() + ".pdf"), any(InputStream.class), eq(1L));
        verify(rabbit).convertAndSend(eq(""), eq(OCR_QUEUE), any(OcrJobDto.class));
        verifyNoInteractions(mapper);
    }

    // --- Read ---

    @Test
    void getAll_returnsListOfDtos() {
        var e = newEntity("Doc1", null);
        when(repo.findAll()).thenReturn(List.of(e));
        when(mapper.toDto(anyList())).thenReturn(
                List.of(new DocumentDto(e.getId(), e.getTitle(), e.getDescription(), e.getCreatedAt(), "UPLOADED", null, null))
        );

        var result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Doc1", result.get(0).title());
        verify(repo).findAll();
        verify(mapper).toDto(anyList());
    }

    @Test
    void getById_found_returnsDto() {
        var e = newEntity("DocX", null);
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(
                new DocumentDto(e.getId(), e.getTitle(), e.getDescription(), e.getCreatedAt(), "UPLOADED", null, null)
        );

        var dto = service.getById(e.getId());

        assertNotNull(dto);
        assertEquals("DocX", dto.title());
        verify(repo).findById(e.getId());
        verify(mapper).toDto(e);
    }

    @Test
    void getById_notFound_throwsDocumentNotFound() {
        var id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(DocumentNotFoundException.class, () -> service.getById(id));
    }

    // --- Delete ---

    @Test
    void deleteById_exists_deletes_entity_and_minioObject() {
        var id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteById(id));

        verify(minio).delete(eq(id + ".pdf"));
        verify(repo).deleteById(id);
    }

    @Test
    void deleteById_notExists_throwsIllegalArgument_and_noMinioCall() {
        var id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        var ex = assertThrows(IllegalArgumentException.class, () -> service.deleteById(id));
        assertTrue(ex.getMessage().contains("Document not found"));

        verifyNoInteractions(minio);
        verify(repo, never()).deleteById(any());
    }


    // --- Helper ---

    private static DocumentEntity newEntity(String title, String desc) {
        var e = new DocumentEntity();
        e.setId(UUID.randomUUID());
        e.setTitle(title);
        e.setDescription(desc);
        e.setCreatedAt(Instant.now());
        return e;
    }
}
