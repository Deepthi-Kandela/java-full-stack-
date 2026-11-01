package com.thinkmart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.CheckoutRequest;
import com.thinkmart.dto.OrderDto;
import com.thinkmart.model.User;
import com.thinkmart.security.AuthInterceptor;
import com.thinkmart.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> checkout(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                                             @Valid @RequestBody(required = false) CheckoutRequest request) {
        String address = request == null ? null : request.shippingAddress();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(user, address));
    }

    @GetMapping("/my")
    public List<OrderDto> myOrders(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return orderService.myOrders(user);
    }
}
