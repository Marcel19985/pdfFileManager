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
    private MockMvc mockMvc; //simuliert Webserver

    @MockitoBean
    private DocumentService documentService; //Service wird gemobbt


    @Test
    void upload_shouldReturnCreatedDocument() throws Exception {
        //Daten vorbereiten
        UUID id = UUID.randomUUID();
        DocumentDto dto = new DocumentDto(id, "My Title", "UPLOADED", Instant.now());
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf",
                "application/pdf", "dummy content".getBytes());
        MockMultipartFile titlePart = new MockMultipartFile("title", "", "text/plain", "My Title".getBytes());
        MockMultipartFile descPart = new MockMultipartFile("description", "", "text/plain", "Desc".getBytes());

        //gibt dto zurück
        when(documentService.upload(any(), anyString(), anyString())).thenReturn(dto);

        //HTTP-Request simulieren und Response prüfen
        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .file(titlePart)
                        .file(descPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("My Title"))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }


    @Test
    void getAll_shouldReturnListOfDocuments() throws Exception {
        UUID id = UUID.randomUUID();
        when(documentService.getAll()).thenReturn(List.of(
                new DocumentDto(id, "Doc1", "NEW", Instant.now())
        ));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].title").value("Doc1"));
    }

    @Test
    void getById_shouldReturnDocumentIfFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(documentService.getById(id)).thenReturn(new DocumentDto(id, "DocX", "NEW", Instant.now()));

        mockMvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("DocX"));
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
}
