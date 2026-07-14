package com.example.productService.controller;

import com.example.productService.dtos.Category.CategoryDto;
import com.example.productService.dtos.Category.CategoryRequestDto;
import com.example.productService.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/category")
    public class CategoryController {

        CategoryService categoryService;

        @Autowired
        public CategoryController(CategoryService categoryService){
            this.categoryService = categoryService;
        }

        @GetMapping("/getById")
        public ResponseEntity<CategoryDto> findById(@RequestParam Long id ){
            return ResponseEntity.ok(categoryService.findById(id));
        }

        @PostMapping("/save")
        public ResponseEntity<CategoryDto> save(@RequestBody CategoryRequestDto categoryRequestDto){
            return ResponseEntity.ok(categoryService.saveByRequest(categoryRequestDto));
        }
}
