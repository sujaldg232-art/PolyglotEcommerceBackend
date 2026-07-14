package com.example.productService.controller;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.dtos.product.ProductDto;
import com.example.productService.dtos.product.ProductRequestDto;
import com.example.productService.dtos.product.ProductUpdateDto;
import com.example.productService.mappers.ProductMapper;
import com.example.productService.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Product")
public class ProductController {


    ProductService productService;
    ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper){
        this.productMapper = productMapper;
        this.productService = productService;
    }


    @PostMapping("/updateProduct")
    public ResponseEntity<ProductDto> updateProduct(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestBody @Valid ProductUpdateDto productUpdateDto,
            @RequestParam UUID productId
            ){
        UUID userId = UUID.fromString(userIdStr);
        ProductDto productDto = productService.updateProduct(productId,productUpdateDto,userId);
        return ResponseEntity.ok(productDto);
    }


    @PostMapping("/AddTags")
    public ResponseEntity<ProductDto> addTags(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestBody List<TagRequestDto> tags,
            @RequestParam UUID productId

    ){
        UUID userID = UUID.fromString(userIdStr);
        return  ResponseEntity.ok(productService.addTags(tags,productId,userID));
    }

    @PostMapping("/RemoveTags")
    public ResponseEntity<ProductDto> removeTags(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestBody List<TagRequestDto> tags,
            @RequestParam UUID productId
    ){
        UUID userID = UUID.fromString(userIdStr);
        return  ResponseEntity.ok(productService.removeTags(tags,productId,userID));
    }

    @PostMapping("/ChangeCategory")
    public ResponseEntity<ProductDto> changeCategory(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestParam Long categoryId,
            @RequestParam UUID productId
    ){
        UUID userID = UUID.fromString(userIdStr);
        return  ResponseEntity.ok(productService.changeCategory(productId,categoryId,productId));
    }

    @PostMapping("/createProduct")
    public ResponseEntity<ProductDto> createProduct(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody ProductRequestDto productRequestDto
            ){
        if(userRole.equals("BUYER")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = java.util.UUID.fromString(userIdStr);

        ProductDto response = productService.createProduct(productRequestDto,userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/createMultipleProduct")
    public ResponseEntity<List<ProductDto>> createMultipleProducts(
            @RequestHeader("X-User-Id") String  userIdStr,
            @RequestHeader("X-User-Role") String userRole,
            @RequestBody List<ProductRequestDto> productRequestDto
    ){
        if(userRole.equals("BUYER")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = java.util.UUID.fromString(userIdStr);

        List<ProductDto> response = productService.createMultipleProduct(productRequestDto,userId);

        return ResponseEntity.ok(response);
    }
}
