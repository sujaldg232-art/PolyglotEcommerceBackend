package com.example.productService.dtos.product;

import com.example.productService.dtos.Category.CategoryDto;
import com.example.productService.dtos.Tag.TagDto;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;

@Builder
public record ProductDto(
        UUID id,
        String name,
        UUID sellerId,
        String description,
        BigDecimal price,
        String sku,
        Integer stockQuantity,
        CategoryDto category,
        HashSet<TagDto> tags
){}