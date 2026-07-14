package com.example.productService.service;

import com.example.productService.dtos.Category.CategoryDto;
import com.example.productService.dtos.Category.CategoryRequestDto;
import com.example.productService.entities.Category;
import com.example.productService.mappers.CategoryMapper;
import com.example.productService.repo.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CategoryService {
    CategoryRepo categoryRepo;
    CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(
            CategoryRepo categoryRepo,
            CategoryMapper categoryMapper
    ){
        this.categoryMapper = categoryMapper;
        this.categoryRepo = categoryRepo;
    }



    public CategoryDto findById(Long id){
        Category category = categoryRepo.findById(id).orElse(null);
        CategoryDto categoryDto = categoryMapper.entityToDto(category);
        return categoryDto;
    }

    @Transactional
    public CategoryDto saveByRequest(CategoryRequestDto categoryRequestDto){
        Category category = categoryRepo.save(categoryMapper.requestToEntity(categoryRequestDto));
        CategoryDto categoryDto = categoryMapper.entityToDto(category);
        return categoryDto;
    }
}
