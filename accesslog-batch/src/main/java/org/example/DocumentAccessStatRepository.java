package org.example;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DocumentAccessStatRepository
        extends JpaRepository<DocumentAccessStat, Long> {

    Optional<DocumentAccessStat> findByDocumentIdAndAccessDateAndSourceSystem(
            UUID documentId, LocalDate accessDate, String sourceSystem
    );
}
