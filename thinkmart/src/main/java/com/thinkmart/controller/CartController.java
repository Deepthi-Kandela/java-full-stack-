package com.thinkmart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.CartAddRequest;
import com.thinkmart.dto.CartDto;
import com.thinkmart.dto.QuantityRequest;
import com.thinkmart.model.User;
import com.thinkmart.security.AuthInterceptor;
import com.thinkmart.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto get(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return cartService.getCart(user);
    }

    @PostMapping
    public CartDto add(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                       @Valid @RequestBody CartAddRequest request) {
        int qty = request.quantity() == null ? 1 : request.quantity();
        return cartService.add(user, request.productId(), qty);
    }

    @PutMapping("/{productId}")
    public CartDto setQuantity(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                               @PathVariable Long productId,
                               @Valid @RequestBody QuantityRequest request) {
        return cartService.setQuantity(user, productId, request.quantity());
    }

    @DeleteMapping("/{productId}")
    public CartDto remove(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                          @PathVariable Long productId) {
        return cartService.remove(user, productId);
    }
}
