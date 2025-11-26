package at.ac.fhtw.swen3.swen3teamm.service.mapper;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import at.ac.fhtw.swen3.swen3teamm.service.dto.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDto toDto(CategoryEntity e) {
        return new CategoryDto(e.getId(), e.getName(), e.getDescription());
    }
}
