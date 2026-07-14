package com.example.productService.dtos.product;

import com.example.productService.dtos.Tag.TagRequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import java.math.BigDecimal;
import java.util.ArrayList;


public record ProductRequestDto(
        @NotBlank(message = "Product name cannot be blank")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be strictly greater than zero")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQuantity,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        ArrayList<TagRequestDto> tags
) {}