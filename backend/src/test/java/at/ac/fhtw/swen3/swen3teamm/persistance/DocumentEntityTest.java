package at.ac.fhtw.swen3.swen3teamm.persistance;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentEntityTest {

    @Test
    void onCreate_shouldSetCreatedAndUpdatedAt() {
        DocumentEntity entity = new DocumentEntity();

        entity.onCreate(); // Aufruf der @PrePersist-Methode

        // Prüfen, ob die Timestamps gesetzt wurden
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt()); // Zeitpunkte müssen gleich sein
    }

    @Test
    void onUpdate_shouldUpdateUpdatedAtOnly() throws InterruptedException {
        DocumentEntity entity = new DocumentEntity();
        entity.onCreate();
        Instant createdAtBefore = entity.getCreatedAt();
        Instant updatedAtBefore = entity.getUpdatedAt();

        Thread.sleep(5); // kurze Pause
        entity.onUpdate(); // Aufruf der @PreUpdate-Methode

        // Prüfen, dass nur updatedAt geändert wurde
        assertThat(entity.getCreatedAt()).isEqualTo(createdAtBefore); // createdAt bleibt gleich
        assertThat(entity.getUpdatedAt()).isAfter(updatedAtBefore);   // updatedAt ist neuer
    }

    @Test
    void category_shouldBeAssignable() {
        DocumentEntity entity = new DocumentEntity();
        CategoryEntity category = new CategoryEntity("TestCat", "Beschreibung");

        entity.setCategory(category);

        assertThat(entity.getCategory()).isNotNull();
        assertThat(entity.getCategory().getName()).isEqualTo("TestCat");
        assertThat(entity.getCategory().getDescription()).isEqualTo("Beschreibung");
    }
}
