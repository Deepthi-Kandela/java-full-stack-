package com.thinkmart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. Mobile, Laptop (shown as the card heading) */
    @Column(nullable = false, length = 60)
    private String category;

    /** e.g. "VIVO V29" */
    @Column(nullable = false, length = 120)
    private String brand;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private double rating;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private int stock;

    /** Soft delete flag: old orders keep pointing at the product. */
    private boolean active = true;

    /** Seller who added the product (null for the seeded catalogue). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private User vendor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
