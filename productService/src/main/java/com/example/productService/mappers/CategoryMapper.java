package com.example.productService.mappers;

import com.example.productService.dtos.Category.CategoryDto;
import com.example.productService.dtos.Category.CategoryRequestDto;
import com.example.productService.entities.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto entityToDto(Category category);
    Category dtoToEntity(CategoryDto categoryDto);
    CategoryRequestDto entityToRequest(Category category);
    Category requestToEntity(CategoryRequestDto categoryRequest);
}
