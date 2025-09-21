package at.ac.fhtw.swen3.swen3teamm.persistance;

import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentEntityTest {

    @Test
    void onCreate_shouldSetCreatedAndUpdatedAt() {
        DocumentEntity entity = new DocumentEntity();

        entity.onCreate(); //aufrufen

        //Prüfen ob gesetzt ist
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void onUpdate_shouldUpdateUpdatedAtOnly() throws InterruptedException {
        DocumentEntity entity = new DocumentEntity();
        entity.onCreate();
        Instant createdAtBefore = entity.getCreatedAt();
        Instant updatedAtBefore = entity.getUpdatedAt();

        Thread.sleep(5); //kurze Pause
        entity.onUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(createdAtBefore); //Unverändert bleibt
        assertThat(entity.getUpdatedAt()).isAfter(updatedAtBefore); //nach dem anderem Wert
    }
}
