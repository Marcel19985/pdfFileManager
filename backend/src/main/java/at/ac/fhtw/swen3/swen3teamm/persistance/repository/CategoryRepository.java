package at.ac.fhtw.swen3.swen3teamm.persistance.repository;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
