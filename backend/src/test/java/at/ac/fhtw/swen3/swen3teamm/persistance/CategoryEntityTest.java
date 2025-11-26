package at.ac.fhtw.swen3.swen3teamm.persistance;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryEntityTest {

    @Test
    void constructor_shouldSetFieldsCorrectly() {
        String name = "Schule";
        String description = "Kategorie für Schule";

        CategoryEntity category = new CategoryEntity(name, description);

        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
    }

    @Test
    void setters_shouldUpdateFields() {
        CategoryEntity category = new CategoryEntity();

        category.setName("Rechnung");
        category.setDescription("Kategorie für Rechnungen");

        assertThat(category.getName()).isEqualTo("Rechnung");
        assertThat(category.getDescription()).isEqualTo("Kategorie für Rechnungen");
    }

    @Test
    void defaultConstructor_shouldCreateEmptyCategory() {
        CategoryEntity category = new CategoryEntity();
        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
    }
}
