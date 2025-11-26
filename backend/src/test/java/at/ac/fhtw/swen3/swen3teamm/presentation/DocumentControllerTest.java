package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.service.DocumentService;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    void upload_shouldReturnCreatedDocument() throws Exception {
        UUID id = UUID.randomUUID();

        DocumentDto dto = new DocumentDto(
                id,
                "My Title",
                null,
                Instant.now(),
                null,
                1L,           // categoryId
                "TestCat"     // categoryName
        );

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                "application/pdf", "dummy content".getBytes());
        MockMultipartFile titlePart = new MockMultipartFile("title", "", "text/plain", "My Title".getBytes());
        MockMultipartFile descPart = new MockMultipartFile("description", "", "text/plain", "Desc".getBytes());

        when(documentService.upload(any(), anyString(), anyString())).thenReturn(dto);

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .file(titlePart)
                        .file(descPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("My Title"))
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.categoryName").value("TestCat"));
    }

    @Test
    void getAll_shouldReturnListOfDocuments() throws Exception {
        UUID id = UUID.randomUUID();

        when(documentService.getAll()).thenReturn(List.of(
                new DocumentDto(id, "Doc1", "desc", Instant.now(), null, 2L, "Rechnung")
        ));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].title").value("Doc1"))
                .andExpect(jsonPath("$[0].categoryId").value(2L))
                .andExpect(jsonPath("$[0].categoryName").value("Rechnung"));
    }

    @Test
    void getById_shouldReturnDocumentIfFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(documentService.getById(id)).thenReturn(
                new DocumentDto(id, "DocX", null, Instant.now(), null, 3L, "Brief")
        );

        mockMvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("DocX"))
                .andExpect(jsonPath("$.categoryId").value(3L))
                .andExpect(jsonPath("$.categoryName").value("Brief"));
    }

    @Test
    void getById_shouldReturnNotFoundIfMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(documentService.getById(id)).thenReturn(null);

        mockMvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_shouldReturnNoContentIfDeleted() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(documentService).deleteById(id);

        mockMvc.perform(delete("/api/documents/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteById_shouldReturnNotFoundIfMissing() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new IllegalArgumentException("not found"))
                .when(documentService).deleteById(id);

        mockMvc.perform(delete("/api/documents/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_shouldReturnNoContentOnSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(documentService).updateCategory(id, "Schule");

        mockMvc.perform(patch("/api/documents/{id}/category", id)
                        .param("category", "Schule"))
                .andExpect(status().isNoContent());

        Mockito.verify(documentService).updateCategory(id, "Schule");
    }

    @Test
    void updateCategory_shouldReturnNotFoundIfDocumentMissing() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new IllegalArgumentException("Document not found"))
                .when(documentService).updateCategory(id, "Schule");

        mockMvc.perform(patch("/api/documents/{id}/category", id)
                        .param("category", "Schule"))
                .andExpect(status().isNotFound());
    }

}
