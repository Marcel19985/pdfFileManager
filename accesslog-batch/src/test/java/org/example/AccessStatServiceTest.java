package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessStatServiceTest {

    @Mock
    DocumentAccessStatRepository repo;

    @InjectMocks
    AccessStatService service;

    @Test
    void applyAccessLog_whenStatExists_overwritesCountAndSaves() {
        UUID docId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2025-12-15");
        String source = "pdfFileManager";

        AccessEntry entry = new AccessEntry();
        entry.setDocumentId(docId);
        entry.setCount(10);

        AccessLogFile file = new AccessLogFile();
        file.setDate(date.toString());
        file.setSourceSystem(source);
        file.getEntries().add(entry);

        DocumentAccessStat existing = new DocumentAccessStat();
        existing.setId(1L);
        existing.setDocumentId(docId);
        existing.setAccessDate(date);
        existing.setSourceSystem(source);
        existing.setAccessCount(3);

        when(repo.findByDocumentIdAndAccessDateAndSourceSystem(docId, date, source))
                .thenReturn(Optional.of(existing));

        service.applyAccessLog(file);

        // ÜBERSCHREIBEN (nicht addieren)
        assertEquals(10, existing.getAccessCount());
        verify(repo).save(existing);
    }

    @Test
    void applyAccessLog_whenStatMissing_createsNewAndSaves() {
        UUID docId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2025-12-15");
        String source = "pdfFileManager";

        AccessEntry entry = new AccessEntry();
        entry.setDocumentId(docId);
        entry.setCount(5);

        AccessLogFile file = new AccessLogFile();
        file.setDate(date.toString());
        file.setSourceSystem(source);
        file.getEntries().add(entry);

        when(repo.findByDocumentIdAndAccessDateAndSourceSystem(docId, date, source))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DocumentAccessStat> captor = ArgumentCaptor.forClass(DocumentAccessStat.class);

        service.applyAccessLog(file);

        verify(repo).save(captor.capture());
        DocumentAccessStat saved = captor.getValue();

        assertNull(saved.getId());
        assertEquals(docId, saved.getDocumentId());
        assertEquals(date, saved.getAccessDate());
        assertEquals(source, saved.getSourceSystem());
        assertEquals(5, saved.getAccessCount());
    }

    @Test
    void applyAccessLog_multipleEntries_savesEach() {
        LocalDate date = LocalDate.parse("2025-12-15");
        String source = "pdfFileManager";

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        AccessEntry e1 = new AccessEntry();
        e1.setDocumentId(id1);
        e1.setCount(1);

        AccessEntry e2 = new AccessEntry();
        e2.setDocumentId(id2);
        e2.setCount(2);

        AccessLogFile file = new AccessLogFile();
        file.setDate(date.toString());
        file.setSourceSystem(source);
        file.getEntries().add(e1);
        file.getEntries().add(e2);

        when(repo.findByDocumentIdAndAccessDateAndSourceSystem(any(), eq(date), eq(source)))
                .thenReturn(Optional.empty());

        service.applyAccessLog(file);

        verify(repo, times(2)).save(any(DocumentAccessStat.class));
    }
}
