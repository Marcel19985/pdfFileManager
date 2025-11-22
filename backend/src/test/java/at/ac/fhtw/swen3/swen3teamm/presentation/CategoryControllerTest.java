package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.persistance.CategoryEntity;
import at.ac.fhtw.swen3.swen3teamm.persistance.repository.CategoryRepository;
import at.ac.fhtw.swen3.swen3teamm.service.dto.CategoryDto;
import at.ac.fhtw.swen3.swen3teamm.service.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepo;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryController controller;

    @Test
    void getAll_returnsDtoList() {
        var cat1 = new CategoryEntity("Schule", "Kategorie Schule");
        var cat2 = new CategoryEntity("Rechnung", "Kategorie Rechnung");

        when(categoryRepo.findAll()).thenReturn(List.of(cat1, cat2));
        when(mapper.toDto(cat1)).thenReturn(new CategoryDto(cat1.getId(), cat1.getName(), cat1.getDescription()));
        when(mapper.toDto(cat2)).thenReturn(new CategoryDto(cat2.getId(), cat2.getName(), cat2.getDescription()));

        var result = controller.getAll();

        assertThat(result).hasSize(2);
        verify(categoryRepo).findAll();
        verify(mapper, times(1)).toDto(cat1);
        verify(mapper, times(1)).toDto(cat2);
    }

    @Test
    void create_validCategory_returnsDto() {
        var cat = new CategoryEntity("Vertrag", "Kategorie Vertrag");
        var dto = new CategoryDto(cat.getId(), cat.getName(), cat.getDescription());

        when(categoryRepo.save(cat)).thenReturn(cat);
        when(mapper.toDto(cat)).thenReturn(dto);

        ResponseEntity<?> response = controller.create(cat);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(categoryRepo).save(cat);
        verify(mapper).toDto(cat);
    }

    @Test
    void create_missingName_returnsBadRequest() {
        var cat = new CategoryEntity(null, "Kategorie ohne Name");

        ResponseEntity<?> response = controller.create(cat);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("name required");
        verifyNoInteractions(categoryRepo, mapper);
    }
}
