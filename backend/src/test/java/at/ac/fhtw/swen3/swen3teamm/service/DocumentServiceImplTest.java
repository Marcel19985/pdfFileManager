package at.ac.fhtw.swen3.swen3teamm.service;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.DocumentRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.DocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;

import static at.ac.fhtw.swen3.swen3teamm.config.MessagingConfig.OCR_QUEUE;
import static org.mockito.ArgumentMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock DocumentRepository repo;
    @Mock DocumentMapper mapper;
    @Mock RabbitTemplate rabbit;

    @InjectMocks DocumentServiceImpl service;

    @Test
    void upload_success_persists_and_publishes() {
        // arrange
        var file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{1,2,3});
        var id = UUID.randomUUID();
        var now = Instant.now();

        var entity = new DocumentEntity();
        entity.setId(id);
        entity.setTitle("Test Title");
        entity.setDescription("Desc");
        entity.setCreatedAt(now);

        var dto = new DocumentDto(id, "Test Title", "Desc", "UPLOADED", now);

        when(repo.save(any(DocumentEntity.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        // publish OK -> nichts weiter stubben

        // act
        var result = service.upload(file, "Test Title", "Desc");

        // assert
        assertNotNull(result);
        assertEquals("Test Title", result.title());
        verify(repo, times(1)).save(any(DocumentEntity.class));
        verify(rabbit, times(1))
                .convertAndSend(eq(""), eq(OCR_QUEUE), (Object) any());
        verify(mapper, times(1)).toDto(entity);
    }

    @Test
    void upload_withEmptyFile_throwsValidationException() {
        var empty = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(ValidationException.class, () -> service.upload(empty, "t", "d"));
    }

    @Test
    void upload_publishFails_throwsMessagingException() {
        var file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
        var entity = new DocumentEntity();
        entity.setId(UUID.randomUUID());
        when(repo.save(any())).thenReturn(entity);
        doThrow(new AmqpException("down"))
                .when(rabbit)
                .convertAndSend(eq(""), eq(OCR_QUEUE), (Object) any());

        assertThrows(MessagingException.class, () -> service.upload(file, "t", "d"));

        verify(repo).save(any());
        verify(rabbit)
                .convertAndSend(eq(""), eq(OCR_QUEUE), (Object) any());
    }

    @Test
    void getAll_returnsListOfDtos() {
        var e = new DocumentEntity();
        e.setId(UUID.randomUUID());
        e.setTitle("Doc1");
        e.setCreatedAt(Instant.now());
        when(repo.findAll()).thenReturn(List.of(e));
        when(mapper.toDto(anyList())).thenReturn(
                List.of(new DocumentDto(e.getId(), "Doc1", null, "UPLOADED", e.getCreatedAt()))
        );

        var result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Doc1", result.get(0).title());
    }

    @Test
    void getById_found_returnsDto() {
        var id = UUID.randomUUID();
        var e = new DocumentEntity();
        e.setId(id);
        e.setTitle("DocX");
        e.setCreatedAt(Instant.now());
        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(new DocumentDto(id, "DocX", null, "UPLOADED", e.getCreatedAt()));

        var dto = service.getById(id);

        assertNotNull(dto);
        assertEquals("DocX", dto.title());
    }

    @Test
    void getById_notFound_throwsDocumentNotFound() {
        var id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(DocumentNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void deleteById_exists_deletes() {
        var id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteById(id));
        verify(repo).deleteById(id);
    }

    @Test
    void deleteById_notExists_throwsIllegalArgument() {
        var id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        var ex = assertThrows(IllegalArgumentException.class, () -> service.deleteById(id));
        assertTrue(ex.getMessage().contains("Document not found"));
    }
}
