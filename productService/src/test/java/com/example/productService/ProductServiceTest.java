package com.example.productService;

import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.dtos.product.ProductDto;
import com.example.productService.dtos.product.ProductRequestDto;
import com.example.productService.dtos.product.ProductUpdateDto;
import com.example.productService.entities.Category;
import com.example.productService.entities.Product;
import com.example.productService.entities.Tag;
import com.example.productService.mappers.ProductMapper;
import com.example.productService.mappers.TagMapper;
import com.example.productService.repo.CategoryRepo;
import com.example.productService.repo.ProductRepo;
import com.example.productService.repo.TagRepo;
import com.example.productService.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private TagRepo tagRepo;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void findBySellerId() {
        UUID sellerId = UUID.randomUUID();
        List<Product> products = List.of(new Product());
        List<ProductDto> dtos = List.of(ProductDto.builder().build());

        when(productRepo.findBySellerId(sellerId)).thenReturn(products);
        when(productMapper.entityToDto(products)).thenReturn(dtos);

        List<ProductDto> result = productService.findBySellerId(sellerId);

        assertEquals(dtos, result);
        verify(productRepo).findBySellerId(sellerId);
    }

    @Test
    void findProductById_Success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        Product result = productService.findProductById(productId);

        assertNotNull(result);
    }

    @Test
    void findProductById_ThrowsException() {
        UUID productId = UUID.randomUUID();
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.findProductById(productId));
        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void sellerAndUserIDVerfication_Success() {
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setSellerId(userId);

        assertDoesNotThrow(() -> productService.sellerAndUserIDVerfication(userId, product));
    }

    @Test
    void sellerAndUserIDVerfication_ThrowsException() {
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setSellerId(UUID.randomUUID());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productService.sellerAndUserIDVerfication(userId, product));
        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void changeCategory() {
        UUID productId = UUID.randomUUID();
        Long categoryId = 1L;
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setSellerId(userId);
        Category category = new Category();

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepo.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.changeCategory(productId, categoryId, userId);

        assertNotNull(result);
        verify(productRepo).save(product);
    }

    @Test
    void addTags() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setSellerId(userId);
        product.setTags(new HashSet<>());
        List<TagRequestDto> tagDtos = List.of(new TagRequestDto("NewTag"));
        List<Tag> tags = List.of(Tag.builder().name("NewTag").build());

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(tagMapper.requestToEntity(tagDtos)).thenReturn(tags);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.addTags(tagDtos, productId, userId);

        assertNotNull(result);
        assertEquals(1, product.getTags().size());
        verify(productRepo).save(product);
    }

    @Test
    void removeTags() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Product product = new Product();
        product.setSellerId(userId);

        Set<Tag> existingTags = new HashSet<>();
        existingTags.add(Tag.builder().name("TagToRemove").build());
        existingTags.add(Tag.builder().name("TagToKeep").build());
        product.setTags(existingTags);

        List<TagRequestDto> tagDtos = List.of(new TagRequestDto("TagToRemove"));

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.removeTags(tagDtos, productId, userId);

        assertNotNull(result);
        assertEquals(1, product.getTags().size());
        assertTrue(product.getTags().stream().anyMatch(t -> t.getName().equals("TagToKeep")));
        verify(productRepo).save(product);
    }

    @Test
    void updateProduct() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ProductUpdateDto updateDto = new ProductUpdateDto("New Name", "New Desc", BigDecimal.TEN, 100, "NEW-SKU", true);
        Product product = new Product();
        product.setSellerId(userId);

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.updateProduct(productId, updateDto, userId);

        assertNotNull(result);
        assertEquals("New Name", product.getName());
        assertEquals(BigDecimal.TEN, product.getPrice());
        verify(productRepo).save(product);
    }

    @Test
    void createProduct() {
        UUID sellerId = UUID.randomUUID();
        ProductRequestDto requestDto = new ProductRequestDto("Name", "Desc", BigDecimal.ONE, 10, "SKU", 1L, new ArrayList<>());
        Product product = new Product();

        when(productMapper.requestToEntity(requestDto)).thenReturn(product);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.createProduct(requestDto, sellerId);

        assertNotNull(result);
        assertTrue(product.isAvailable());
        assertEquals(sellerId, product.getSellerId());
        verify(productRepo).save(product);
    }

    @Test
    void createProduct_WithNewAndExistingTags() {
        UUID sellerId = UUID.randomUUID();
        List<TagRequestDto> tagRequests = List.of(
                new TagRequestDto("ExistingTag"),
                new TagRequestDto("NewTag")
        );
        ProductRequestDto requestDto = new ProductRequestDto(
                "Name", "Desc", BigDecimal.ONE, 10, "SKU", 1L, new ArrayList<>(tagRequests)
        );

        Product product = new Product();

        Tag existingTag = Tag.builder().name("ExistingTag").build();
        List<Tag> existingTags = List.of(existingTag);

        when(productMapper.requestToEntity(requestDto)).thenReturn(product);
        when(tagRepo.findByNameIn(List.of("ExistingTag", "NewTag"))).thenReturn(existingTags);
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(ProductDto.builder().build());

        ProductDto result = productService.createProduct(requestDto, sellerId);

        assertNotNull(result);
        verify(tagRepo).findByNameIn(List.of("ExistingTag", "NewTag"));
        verify(tagRepo).saveAll(anyList());
        assertEquals(2, product.getTags().size());
        verify(productRepo).save(product);
    }

    @Test
    void createMultipleProduct() {
        UUID sellerId = UUID.randomUUID();
        ProductRequestDto requestDto1 = new ProductRequestDto("Name1", "Desc", BigDecimal.ONE, 10, "SKU1", 1L, new ArrayList<>());
        ProductRequestDto requestDto2 = new ProductRequestDto("Name2", "Desc", BigDecimal.ONE, 10, "SKU2", 1L, new ArrayList<>());
        List<ProductRequestDto> requests = List.of(requestDto1, requestDto2);

        Product product1 = new Product();
        Product product2 = new Product();
        ProductDto dto = ProductDto.builder().build();

        when(productMapper.requestToEntity(any(ProductRequestDto.class))).thenReturn(product1, product2);
        when(productRepo.save(any(Product.class))).thenReturn(product1, product2);
        when(productMapper.entityToDto(any(Product.class))).thenReturn(dto, dto);

        List<ProductDto> results = productService.createMultipleProduct(requests, sellerId);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepo, times(2)).save(any(Product.class));
    }

    @Test
    void isValidForOrder_True() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setStockQuantity(10);

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        assertTrue(productService.isValidForOrder(productId, 5));
    }

    @Test
    void isValidForOrder_False() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setStockQuantity(2);

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));

        assertFalse(productService.isValidForOrder(productId, 5));
    }
}