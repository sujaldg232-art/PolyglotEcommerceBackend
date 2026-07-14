        package com.example.productService.dtos.product;

        import jakarta.validation.constraints.Min;
        import jakarta.validation.constraints.NotBlank;
        import jakarta.validation.constraints.Positive;

        import java.math.BigDecimal;

        public record ProductUpdateDto(
                @NotBlank(message = "Product name cannot be blank")
                        String name,

                String description,

                @Positive(message = "Price must be strictly greater than zero")
                BigDecimal price,

                @Min(value = 0, message = "Stock cannot be negative")
                Integer stockQuantity,

                @NotBlank(message = "SKU is required")
                String sku,

                Boolean available
        ) {
        }
