package com.example.productService.mappers;

import com.example.productService.dtos.product.ProductDto;
import com.example.productService.dtos.product.ProductRequestDto;
import com.example.productService.entities.Category;
import com.example.productService.entities.Product;
import com.example.productService.repo.CategoryRepo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, TagMapper.class})
public abstract class ProductMapper {

    @Autowired
    protected CategoryRepo categoryRepository;

    public abstract ProductDto entityToDto(Product product);

    public abstract List<ProductDto> entityToDto(List<Product> productList);

    @Mapping(target = "category", source = "categoryId", qualifiedByName = "idToCategory")
    @Mapping(target = "tags", ignore = true)
    public abstract Product requestToEntity(ProductRequestDto productRequestDto);


    public abstract List<Product> requestToEntity(List<ProductRequestDto> productRequestDto);

    @Named("idToCategory")
    public Category idToCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
    }
}