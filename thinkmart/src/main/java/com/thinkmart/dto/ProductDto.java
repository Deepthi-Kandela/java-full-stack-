package com.thinkmart.dto;

import java.math.BigDecimal;

import com.thinkmart.model.Product;

public record ProductDto(
        Long id,
        String category,
        String brand,
        BigDecimal price,
        double rating,
        String description,
        String imageUrl,
        int stock,
        Long vendorId) {

    public static ProductDto from(Product p) {
        Long vendorId = p.getVendor() != null ? p.getVendor().getId() : null;
        return new ProductDto(p.getId(), p.getCategory(), p.getBrand(), p.getPrice(), p.getRating(),
                p.getDescription(), p.getImageUrl(), p.getStock(), vendorId);
    }
}
