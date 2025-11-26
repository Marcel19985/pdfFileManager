package at.ac.fhtw.swen3.swen3teamm.persistance;

import at.ac.fhtw.swen3.swen3teamm.persistance.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//packt alles wichtige in die DB
@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        List<CategoryEntity> categories = List.of(
                new CategoryEntity("Schule", "Kategorie für Schule"),
                new CategoryEntity("Rechnung", "Kategorie für Rechnungen"),
                new CategoryEntity("Brief", "Kategorie für Briefe"),
                new CategoryEntity("Geschichte", "Kategorie für Geschichte"),
                new CategoryEntity("Wissenschaft", "Kategorie für Wissenschaft"),
                new CategoryEntity("Vertrag", "Kategorie für Verträge"),
                new CategoryEntity("Medizin", "Kategorie für Medizin"),
                new CategoryEntity("Technik", "Kategorie für Technik"),
                new CategoryEntity("Sonstiges", "Kategorie für sonstige Dokumente")
        );

        for (CategoryEntity cat : categories) {
            categoryRepository.findByNameIgnoreCase(cat.getName())
                    .orElseGet(() -> categoryRepository.save(cat));
        }

        System.out.println("Kategorien wurden beim Start eingetragen.");
    }
}
