package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.DocumentEntity;
import at.ac.fhtw.swen3.swen3teamm.service.dto.DocumentDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    // Instanz zum Testen
    private final DocumentMapper mapper = new DocumentMapper();

    @Test
    void toDto_shouldReturnNullForNullEntity() {
        assertThat(mapper.toDto((DocumentEntity) null)).isNull();
    }

    @Test
    void toDto_shouldMapEntityToDto() {
        DocumentEntity entity = new DocumentEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setTitle("My Doc");
        entity.setCreatedAt(Instant.now());
        entity.setDescription("My description");
        entity.setSummary("UPLOADED");

        // Kategorie hinzufügen
        CategoryEntity category = new CategoryEntity("CategoryName", "desc");
        category.setId(5L);
        entity.setCategory(category);

        DocumentDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("My Doc");
        assertThat(dto.description()).isEqualTo("My description");
        assertThat(dto.summary()).isEqualTo("UPLOADED");
        assertThat(dto.categoryId()).isEqualTo(5L);
        assertThat(dto.categoryName()).isEqualTo("CategoryName");
        assertThat(dto.createdAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    void toDto_shouldHandleNullCategory() {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle("No Category Doc");
        entity.setCreatedAt(Instant.now());
        entity.setCategory(null);

        DocumentDto dto = mapper.toDto(entity);

        assertThat(dto.categoryId()).isNull();
        assertThat(dto.categoryName()).isNull();
    }

    @Test
    void ordering_shouldSortByCreatedAtDescAndTitle() {
        Instant now = Instant.now();

        DocumentDto older = new DocumentDto(UUID.randomUUID(), "B", null, now.minusSeconds(60), "UPLOADED", 1L, "Cat1");
        DocumentDto newer = new DocumentDto(UUID.randomUUID(), "A", null, now, "UPLOADED", 2L, "Cat2");

        List<DocumentDto> sorted = mapper.ordering(List.of(older, newer));

        assertThat(sorted).containsExactly(newer, older);
    }
}
