package org.example;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

//Entity Klasse: wird über Hibernate in DB Objekte umgewandelt
@Entity
@Table(
        name = "document_access_stats",
        uniqueConstraints = @UniqueConstraint( //unique -> es darf keine Einträge mit selber ID + Date + Source geben
                name = "uq_doc_date_source",
                columnNames = {"document_id", "access_date", "source_system"}
        )
)
@Getter
@Setter
public class DocumentAccessStat {

    @Id //Primärschlüssel, wird automatisch erzeugt
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    @Column(name = "access_count", nullable = false)
    private int accessCount;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;
}
