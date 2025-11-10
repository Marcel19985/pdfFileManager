package at.ac.fhtw.swen3.swen3teamm.persistance;

import jakarta.persistence.*; //für annotations
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant; //Zeitpunkt auf Nanosekunden genau
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @UuidGenerator
    @GeneratedValue //Hibernate generiert automatisch eine UUID
    private UUID id;

    @Column(nullable = false, length = 200) //Pflichtfeld + maximal 200 Zeichen
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 64)
    private String summaryModel; //welches KI-Modell genutzt wurde

    private Integer summaryTokens; //Anzahl der Tokens der Zusammenfassung (Quasi Textlänge)

    private Instant summaryCreatedAt; //Wann summary erstellt wurde


    @PrePersist //wird aufgerufen bevor die Entität in die DB geschrieben wird
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate //wird aufgerufen bevor die Entität in der DB aktualisiert wird
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
