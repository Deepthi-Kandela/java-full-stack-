package com.thinkmart.dto;

import java.math.BigDecimal;

import com.thinkmart.model.CartItem;

public record CartItemDto(
        Long productId,
        String category,
        String brand,
        BigDecimal price,
        String imageUrl,
        int stock,
        int quantity,
        BigDecimal subtotal) {

    public static CartItemDto from(CartItem ci) {
        var p = ci.getProduct();
        BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
        return new CartItemDto(p.getId(), p.getCategory(), p.getBrand(), p.getPrice(), p.getImageUrl(),
                p.getStock(), ci.getQuantity(), subtotal);
    }
}
