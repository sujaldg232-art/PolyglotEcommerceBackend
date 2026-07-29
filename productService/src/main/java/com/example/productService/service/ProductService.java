package com.example.productService.service;

import com.example.productService.dtos.product.ProductDto;
import com.example.productService.dtos.product.ProductRequestDto;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.dtos.product.ProductUpdateDto;
import com.example.productService.entities.Category;
import com.example.productService.entities.Product;
import com.example.productService.entities.Tag;
import com.example.productService.mappers.ProductMapper;
import com.example.productService.mappers.TagMapper;
import com.example.productService.repo.CategoryRepo;
import com.example.productService.repo.ProductRepo;
import com.example.productService.repo.TagRepo;
import jakarta.persistence.EntityNotFoundException;
import org.example.grpc.OrderLineValidationOutPut;
import org.example.grpc.OrderlineValidationDto;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {
    private final ProductRepo productRepo;
    private final   CategoryRepo categoryRepo;
    private final   TagRepo tagRepo;
    private final  TagMapper tagMapper;
    private final  ProductMapper productMapper;

    public ProductService(ProductRepo productRepo,ProductMapper productMapper,TagRepo tagRepo,CategoryRepo categoryRepo,TagMapper tagMapper){
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.productMapper = productMapper;
        this.tagRepo = tagRepo;
        this.tagMapper = tagMapper;
    }


    public List<ProductDto> findBySellerId(UUID sellerId){
         List<Product> result = productRepo.findBySellerId(sellerId);
         return productMapper.entityToDto(result);
    }

    @Transactional
    public ProductDto createProduct(ProductRequestDto productRequestDto, UUID sellerId) {
        Product product = productMapper.requestToEntity(productRequestDto);
        product.setAvailable(true);
        product.setSellerId(sellerId);

        if (productRequestDto.tags() != null && !productRequestDto.tags().isEmpty()) {
            List<String> tagNames = productRequestDto.tags().stream()
                    .map(TagRequestDto::name)
                    .toList();

            List<Tag> existingTags = tagRepo.findByNameIn(tagNames);

            Set<String> existingTagNames = existingTags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());

            List<Tag> newTags = tagNames.stream()
                    .filter(name -> !existingTagNames.contains(name))
                    .map(name -> Tag.builder().name(name).build())
                    .toList();

            if (!newTags.isEmpty()) {
                tagRepo.saveAll(newTags);
            }

            Set<Tag> allProductTags = Stream.concat(existingTags.stream(), newTags.stream())
                    .collect(Collectors.toSet());

            product.setTags(allProductTags);
        }

        Product savedProduct = productRepo.save(product);
        return productMapper.entityToDto(savedProduct);
    }


    @Transactional
    public List<ProductDto> createMultipleProduct(List<ProductRequestDto> listOfProducts,UUID sellerId){
        List<ProductDto> response = new ArrayList<>();

        for (ProductRequestDto productRequestDto : listOfProducts) {
            ProductDto productDto = createProduct(productRequestDto, sellerId);
            response.add(productDto);
        }

        return response;
    }

    public Product findProductById(UUID productId){
        Product product = productRepo.findById(productId).orElse(null);

        if(product == null){
            throw new ResponseStatusException(HttpStatusCode.valueOf(404));
        }

        return product;
    }

    public void sellerAndUserIDVerfication(UUID userID,Product product){
        if(!product.getSellerId().equals(userID)){
            throw new ResponseStatusException(HttpStatusCode.valueOf(401),"Your Account Is Not Authorized To Change The Product");
        }
    }

    @Transactional
    public ProductDto changeCategory(UUID productId,Long categoryID,UUID currUserID){
        Product product = findProductById(productId);

        sellerAndUserIDVerfication(currUserID,product);

        Category category = categoryRepo.findById(categoryID).orElse(null);

        if(category == null) throw new ResponseStatusException(HttpStatusCode.valueOf(400));

        product.setCategory(category);

        return productMapper.entityToDto(productRepo.save(product));
    }

    @Transactional
    public ProductDto addTags(List<TagRequestDto> tagDtos,UUID productId,UUID currUserID){
        Product product = findProductById(productId);

        sellerAndUserIDVerfication(currUserID,product);

        List<Tag> tags = tagMapper.requestToEntity(tagDtos);

        for(Tag tag : tags){
            product.getTags().add(tag);
        }
        productRepo.save(product);

        return productMapper.entityToDto(product);
    }

    @Transactional
    public ProductDto removeTags(List<TagRequestDto> tagRequestDtos,UUID productId,UUID currUserID){
        Product product = findProductById(productId);

        sellerAndUserIDVerfication(currUserID,product);

        List<String> tagNamesToRemove = tagRequestDtos.stream()
                .map(TagRequestDto::name)
                .toList();

        product.getTags().removeIf(tag -> tagNamesToRemove.contains(tag.getName()));

        Product updatedProduct = productRepo.save(product);
        return productMapper.entityToDto(updatedProduct);
    }

    @Transactional
    public ProductDto updateProduct(UUID productId, ProductUpdateDto dto,UUID currUserId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product Id Is Not Valid"));

        sellerAndUserIDVerfication(currUserId,product);

        if (dto.name() != null) {
            product.setName(dto.name());
        }
        if (dto.description() != null) {
            product.setDescription(dto.description());
        }
        if (dto.price() != null) {
            product.setPrice(dto.price());
        }
        if (dto.stockQuantity() != null) {
            product.setStockQuantity(dto.stockQuantity());
        }
        if (dto.sku() != null) {
            product.setSku(dto.sku());
        }
        if (dto.available() != null) {
            product.setAvailable(dto.available());
        }

        Product updatedProduct = productRepo.save(product);

        return productMapper.entityToDto(updatedProduct);
    }

    public OrderLineValidationOutPut isValid(OrderlineValidationDto data) {
        try {
            UUID productID = UUID.fromString(data.getProductID());
            int quantity = data.getQuantity();
            Product product = productRepo.findById(productID).orElse(null);



            if (product == null) {
                return OrderLineValidationOutPut.newBuilder()
                        .setIsValid(false)
                        .setSellerID("")
                        .setPrice("-1")
                        .setError("Product Not Found")
                        .setErrorId(404)
                        .build();
            }

            if(product.isAvailable() == false){
                return OrderLineValidationOutPut.newBuilder()
                        .setIsValid(false)
                        .setSellerID("")
                        .setPrice("-1")
                        .setError("Product Not Available")
                        .setErrorId(404)
                        .build();
            }

            if (product.getStockQuantity() >= quantity) {
                return OrderLineValidationOutPut.newBuilder()
                        .setIsValid(true)
                        .setSellerID(product.getSellerId().toString())
                        .setPrice(product.getPrice().toString())
                        .setError("")
                        .setErrorId(-1)
                        .build();
            } else {
                return OrderLineValidationOutPut.newBuilder()
                        .setIsValid(false)
                        .setSellerID(product.getSellerId().toString())
                        .setPrice("-1")
                        .setError("Insufficient Stock")
                        .setErrorId(400)
                        .build();
            }

        } catch (IllegalArgumentException e) {
            return OrderLineValidationOutPut.newBuilder()
                    .setIsValid(false)
                    .setSellerID("")
                    .setPrice("-1")
                    .setError("Malformed Product ID")
                    .setErrorId(400)
                    .build();
        }
    }

    public List<OrderLineValidationOutPut> isValidBatch(List<OrderlineValidationDto> dataList) {
        List<OrderLineValidationOutPut> results = new ArrayList<>();
        List<UUID> validProductIds = new ArrayList<>();

        for (OrderlineValidationDto data : dataList) {
            try {
                validProductIds.add(UUID.fromString(data.getProductID()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<Product> products = productRepo.findAllById(validProductIds);
        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (OrderlineValidationDto data : dataList) {
            try {
                UUID productID = UUID.fromString(data.getProductID());
                int quantity = data.getQuantity();
                Product product = productMap.get(productID);


                if (product == null) {
                    results.add(OrderLineValidationOutPut.newBuilder()
                            .setIsValid(false)
                            .setSellerID("")
                            .setPrice("-1")
                            .setError("Product Not Found")
                            .setErrorId(404)
                            .build());
                }
                else if(product.isAvailable() == false){
                    results.add(OrderLineValidationOutPut.newBuilder()
                            .setIsValid(false)
                            .setSellerID("")
                            .setPrice("-1")
                            .setError("Product Not Available")
                            .setErrorId(404)
                            .build());
                } else if (product.getStockQuantity() >= quantity) {
                    results.add(OrderLineValidationOutPut.newBuilder()
                            .setIsValid(true)
                            .setSellerID(product.getSellerId().toString())
                            .setPrice(product.getPrice().toString())
                            .setError("")
                            .setErrorId(-1)
                            .build());
                } else {
                    results.add(OrderLineValidationOutPut.newBuilder()
                            .setIsValid(false)
                            .setSellerID(product.getSellerId().toString())
                            .setPrice("-1")
                            .setError("Insufficient Stock")
                            .setErrorId(400)
                            .build());
                }
            } catch (IllegalArgumentException e) {
                results.add(OrderLineValidationOutPut.newBuilder()
                        .setIsValid(false)
                        .setSellerID("")
                        .setPrice("-1")
                        .setError("Malformed Product ID")
                        .setErrorId(400)
                        .build());
            }
        }
        return results;
    }

}
