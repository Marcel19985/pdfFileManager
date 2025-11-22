package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.CategoryRepository;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.CategoryMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepo;
    private final CategoryMapper mapper;

    @GetMapping
    public List<?> getAll() {
        return categoryRepo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategoryEntity cat) {
        if (cat.getName() == null || cat.getName().isBlank()) {
            return ResponseEntity.badRequest().body("name required");
        }
        return ResponseEntity.ok(mapper.toDto(categoryRepo.save(cat)));
    }
}
