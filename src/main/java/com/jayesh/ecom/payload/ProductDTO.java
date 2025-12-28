package com.jayesh.ecom.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @NotBlank
    @Size(min = 3, max = 50, message = "Product name must be of at-least 3 characters.")
    private String productName;
    private String image;

    @NotBlank
    @Size(min = 3, max = 300, message = "Product description must be of at-least 6 characters.")
    private String description;

    @NotBlank

    private Integer quantity;
    private Double price;
    private Double discount;
    private Double specialPrice;
}
