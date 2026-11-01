package com.thinkmart.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.ProductDto;
import com.thinkmart.dto.ProductRequest;
import com.thinkmart.model.User;
import com.thinkmart.security.AuthInterceptor;
import com.thinkmart.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ----- public -----
    @GetMapping
    public List<ProductDto> list(@RequestParam(required = false) String category,
                                 @RequestParam(required = false) String q) {
        return productService.list(category, q);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return productService.categories();
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Long id) {
        return productService.get(id);
    }

    // ----- vendor / admin -----
    @GetMapping("/mine")
    public List<ProductDto> mine(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return productService.mine(user);
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                                             @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(user, request));
    }

    @PutMapping("/{id}")
    public ProductDto update(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                             @PathVariable Long id,
                             @Valid @RequestBody ProductRequest request) {
        return productService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                                      @PathVariable Long id) {
        productService.delete(user, id);
        return Map.of("message", "Product deleted");
    }
}
