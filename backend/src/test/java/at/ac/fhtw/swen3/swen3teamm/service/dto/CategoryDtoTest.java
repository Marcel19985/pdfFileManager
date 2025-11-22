package at.ac.fhtw.swen3.swen3teamm.service.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryDtoTest {

    @Test
    void constructorAndGetters_workCorrectly() {
        Long id = 1L;
        String name = "Schule";
        String description = "Kategorie für Schule";

        CategoryDto dto = new CategoryDto(id, name, description);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.description()).isEqualTo(description);
    }

    @Test
    void equality_andHashcode_workCorrectly() {
        CategoryDto dto1 = new CategoryDto(1L, "Schule", "Kategorie Schule");
        CategoryDto dto2 = new CategoryDto(1L, "Schule", "Kategorie Schule");
        CategoryDto dto3 = new CategoryDto(2L, "Rechnung", "Kategorie Rechnung");

        // equals
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);

        // hashCode
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void toString_containsValues() {
        CategoryDto dto = new CategoryDto(1L, "Schule", "Kategorie Schule");
        String str = dto.toString();

        assertThat(str).contains("1", "Schule", "Kategorie Schule");
    }
}
