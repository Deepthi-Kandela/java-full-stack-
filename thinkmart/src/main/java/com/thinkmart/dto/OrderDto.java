package com.thinkmart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.thinkmart.model.CustomerOrder;

public record OrderDto(
        Long id,
        String status,
        BigDecimal totalAmount,
        String shippingAddress,
        LocalDateTime createdAt,
        String customerName,
        String customerEmail,
        List<OrderItemDto> items) {

    public static OrderDto from(CustomerOrder o) {
        var u = o.getUser();
        return new OrderDto(o.getId(), o.getStatus().name(), o.getTotalAmount(), o.getShippingAddress(),
                o.getCreatedAt(), u.getFirstName() + " " + u.getLastName(), u.getEmail(),
                o.getItems().stream().map(OrderItemDto::from).toList());
    }
}
