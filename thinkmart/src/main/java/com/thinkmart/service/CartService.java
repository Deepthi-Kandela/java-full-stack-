package com.thinkmart.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkmart.dto.CartDto;
import com.thinkmart.dto.CartItemDto;
import com.thinkmart.exception.ApiException;
import com.thinkmart.model.CartItem;
import com.thinkmart.model.Product;
import com.thinkmart.model.User;
import com.thinkmart.repository.CartItemRepository;
import com.thinkmart.repository.ProductRepository;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CartDto getCart(User user) {
        return build(user);
    }

    @Transactional
    public CartDto add(User user, Long productId, int quantity) {
        Product p = activeProduct(productId);
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId).orElse(null);
        int newQty = quantity + (item == null ? 0 : item.getQuantity());
        checkStock(p, newQty);
        if (item == null) {
            item = new CartItem();
            item.setUser(user);
            item.setProduct(p);
        }
        item.setQuantity(newQty);
        cartItemRepository.save(item);
        return build(user);
    }

    @Transactional
    public CartDto setQuantity(User user, Long productId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item is not in your cart"));
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            checkStock(item.getProduct(), quantity);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return build(user);
    }

    @Transactional
    public CartDto remove(User user, Long productId) {
        cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .ifPresent(cartItemRepository::delete);
        return build(user);
    }

    private CartDto build(User user) {
        List<CartItemDto> items = cartItemRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .map(CartItemDto::from)
                .toList();
        return CartDto.of(items);
    }

    private Product activeProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!p.isActive()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return p;
    }

    private void checkStock(Product p, int wanted) {
        if (p.getStock() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, p.getBrand() + " is out of stock");
        }
        if (wanted > p.getStock()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only " + p.getStock() + " of " + p.getBrand() + " left in stock");
        }
    }
}
