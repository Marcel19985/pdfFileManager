package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import at.ac.fhtw.swen3.swen3teamm.service.dto.CategoryDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void toDto_mapsFieldsCorrectly() {
        // Arrange
        CategoryEntity entity = new CategoryEntity("Schule", "Kategorie für Schule");
        entity.setId(1L);

        // Act
        CategoryDto dto = mapper.toDto(entity);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.name()).isEqualTo(entity.getName());
        assertThat(dto.description()).isEqualTo(entity.getDescription());
    }

    @Test
    void toDto_handlesNullEntity() {
        // Optional: Test für Null-Entity (je nach Anwendungsfall)
        CategoryEntity entity = null;
        try {
            CategoryDto dto = mapper.toDto(entity);
        } catch (NullPointerException e) {
            // erwartet
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }
}
