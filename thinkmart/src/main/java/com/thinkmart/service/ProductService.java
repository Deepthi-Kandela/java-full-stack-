package com.thinkmart.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkmart.dto.ProductDto;
import com.thinkmart.dto.ProductRequest;
import com.thinkmart.exception.ApiException;
import com.thinkmart.model.Product;
import com.thinkmart.model.Role;
import com.thinkmart.model.User;
import com.thinkmart.repository.CartItemRepository;
import com.thinkmart.repository.ProductRepository;
import com.thinkmart.security.Access;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> list(String category, String q) {
        final String cat = (category == null || category.isBlank()) ? null : category.trim();
        final String query = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        return productRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(p -> cat == null || p.getCategory().equalsIgnoreCase(cat))
                .filter(p -> query == null
                        || p.getBrand().toLowerCase(Locale.ROOT).contains(query)
                        || p.getCategory().toLowerCase(Locale.ROOT).contains(query))
                .map(ProductDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        return ProductDto.from(findActive(id));
    }

    /** Admin sees every product, a vendor sees only their own. */
    @Transactional(readOnly = true)
    public List<ProductDto> mine(User user) {
        Access.requireRole(user, Role.ADMIN, Role.VENDOR);
        List<Product> products = user.getRole() == Role.ADMIN
                ? productRepository.findByActiveTrueOrderByIdAsc()
                : productRepository.findByActiveTrueAndVendorIdOrderByIdAsc(user.getId());
        return products.stream().map(ProductDto::from).toList();
    }

    @Transactional
    public ProductDto create(User user, ProductRequest req) {
        Access.requireRole(user, Role.ADMIN, Role.VENDOR);
        Product p = new Product();
        apply(p, req);
        p.setVendor(user);
        return ProductDto.from(productRepository.save(p));
    }

    @Transactional
    public ProductDto update(User user, Long id, ProductRequest req) {
        Access.requireRole(user, Role.ADMIN, Role.VENDOR);
        Product p = findActive(id);
        checkOwner(user, p);
        apply(p, req);
        return ProductDto.from(productRepository.save(p));
    }

    @Transactional
    public void delete(User user, Long id) {
        Access.requireRole(user, Role.ADMIN, Role.VENDOR);
        Product p = findActive(id);
        checkOwner(user, p);
        p.setActive(false); // soft delete so old orders stay intact
        productRepository.save(p);
        cartItemRepository.deleteByProductId(id);
    }

    private Product findActive(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!p.isActive()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return p;
    }

    private void checkOwner(User user, Product p) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (p.getVendor() == null || !p.getVendor().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change your own products");
        }
    }

    private void apply(Product p, ProductRequest req) {
        p.setCategory(req.category().trim());
        p.setBrand(req.brand().trim());
        p.setPrice(req.price());
        p.setRating(req.rating() == null ? 0 : req.rating());
        p.setDescription(req.description());
        p.setImageUrl(req.imageUrl() == null || req.imageUrl().isBlank() ? null : req.imageUrl().trim());
        p.setStock(req.stock());
    }
}
