package com.thinkmart.dto;

import java.math.BigDecimal;

import com.thinkmart.model.OrderItem;

public record OrderItemDto(
        String category,
        String brand,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    public static OrderItemDto from(OrderItem oi) {
        return new OrderItemDto(oi.getCategory(), oi.getBrand(), oi.getUnitPrice(), oi.getQuantity(),
                oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity())));
    }
}
