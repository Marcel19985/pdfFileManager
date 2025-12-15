package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the correct JPA mapping of {@link DocumentAccessStat} to the database
 * and ensures that repository queries and database constraints work as expected.
 *
 * This test uses an in-memory H2 database to validate the real interaction
 * between JPA, Hibernate and the database schema.
 */

@DataJpaTest
@ActiveProfiles("test")
class DocumentAccessStatRepositoryIT {

    @Autowired
    private DocumentAccessStatRepository repo;

    @Test
    void findByDocumentIdAndAccessDateAndSourceSystem_returnsMatch() {
        UUID docId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2025-12-15");
        String source = "pdfFileManager";

        DocumentAccessStat stat = new DocumentAccessStat();
        stat.setDocumentId(docId);
        stat.setAccessDate(date);
        stat.setSourceSystem(source);
        stat.setAccessCount(7);

        repo.saveAndFlush(stat);

        Optional<DocumentAccessStat> found =
                repo.findByDocumentIdAndAccessDateAndSourceSystem(docId, date, source);

        assertTrue(found.isPresent());
        assertEquals(7, found.get().getAccessCount());
    }

    @Test
    void savingDuplicate_violatesUniqueConstraint() {
        UUID docId = UUID.randomUUID();
        LocalDate date = LocalDate.parse("2025-12-15");
        String source = "pdfFileManager";

        DocumentAccessStat a = new DocumentAccessStat();
        a.setDocumentId(docId);
        a.setAccessDate(date);
        a.setSourceSystem(source);
        a.setAccessCount(1);

        DocumentAccessStat b = new DocumentAccessStat();
        b.setDocumentId(docId);
        b.setAccessDate(date);
        b.setSourceSystem(source);
        b.setAccessCount(2);

        repo.saveAndFlush(a);

        // Exception kann bei save oder flush kommen -> saveAndFlush ist am robustesten
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            repo.saveAndFlush(b);
        });
    }
}
