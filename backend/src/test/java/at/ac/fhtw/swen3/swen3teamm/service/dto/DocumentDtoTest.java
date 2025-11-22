package at.ac.fhtw.swen3.swen3teamm.service.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentDtoTest {

    @Test
    void equalsAndHashCode_shouldWorkForSameValues() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // Zwei Dokumente mit exakt den gleichen Werten
        DocumentDto dto1 = new DocumentDto(
                id,
                "Doc",        // title
                null,         // description
                now,          // createdAt
                "UPLOADED",   // summary
                1L,           // categoryId
                "Category"    // categoryName
        );

        DocumentDto dto2 = new DocumentDto(
                id,
                "Doc",
                null,
                now,
                "UPLOADED",
                1L,
                "Category"
        );

        // Prüfen, dass equals und hashCode funktionieren
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void allGetters_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        DocumentDto dto = new DocumentDto(
                id,
                "Doc",
                "Description",
                now,
                "UPLOADED",
                2L,             // categoryId
                "CategoryName"  // categoryName
        );

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Doc");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.summary()).isEqualTo("UPLOADED");
        assertThat(dto.categoryId()).isEqualTo(2L);
        assertThat(dto.categoryName()).isEqualTo("CategoryName");
    }
}
