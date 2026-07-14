package com.example.productService;

import com.example.productService.dtos.Category.CategoryDto;
import com.example.productService.dtos.Category.CategoryRequestDto;
import com.example.productService.entities.Category;
import com.example.productService.mappers.CategoryMapper;
import com.example.productService.repo.CategoryRepo;
import com.example.productService.service.CategoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    CategoryRepo categoryRepo;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;

    CategoryRequestDto categoryRequestDto;
    CategoryDto categoryDto;
    Category category;

    @BeforeEach
    public void init() {
        categoryRequestDto = new CategoryRequestDto("Electronics", "electronics");
        categoryDto = new CategoryDto(1L, "Electronics", "electronics");
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .slug("electronics")
                .build();
    }

    @Nested
    class FindById {

        @Test
        void findByIdSuccess() {
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(categoryMapper.entityToDto(category)).thenReturn(categoryDto);

            CategoryDto res = categoryService.findById(1L);

            assertNotNull(res);
            assertEquals(categoryDto.id(), res.id());
            assertEquals(categoryDto.name(), res.name());
            assertEquals(categoryDto.slug(), res.slug());
            verify(categoryRepo).findById(1L);
            verify(categoryMapper).entityToDto(category);
        }

        @Test
        void findByIdFailure() {
            when(categoryRepo.findById(1L)).thenReturn(Optional.empty());
            when(categoryMapper.entityToDto(null)).thenReturn(null);

            CategoryDto res = categoryService.findById(1L);

            assertNull(res);
            verify(categoryRepo).findById(1L);
            verify(categoryMapper).entityToDto(null);
        }
    }

    @Nested
    class SaveByRequest {

        @Test
        void saveByRequestSuccess() {
            when(categoryMapper.requestToEntity(categoryRequestDto)).thenReturn(category);
            when(categoryRepo.save(category)).thenReturn(category);
            when(categoryMapper.entityToDto(category)).thenReturn(categoryDto);

            CategoryDto res = categoryService.saveByRequest(categoryRequestDto);

            assertNotNull(res);
            assertEquals(categoryDto.id(), res.id());
            assertEquals(categoryDto.name(), res.name());
            assertEquals(categoryDto.slug(), res.slug());
            verify(categoryMapper).requestToEntity(categoryRequestDto);
            verify(categoryRepo).save(category);
            verify(categoryMapper).entityToDto(category);
        }
    }
}