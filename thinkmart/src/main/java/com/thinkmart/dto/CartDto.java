package com.thinkmart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, BigDecimal total, int count) {

    public static CartDto of(List<CartItemDto> items) {
        BigDecimal total = items.stream().map(CartItemDto::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(CartItemDto::quantity).sum();
        return new CartDto(items, total, count);
    }
}
