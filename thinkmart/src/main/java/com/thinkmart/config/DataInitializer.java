package com.thinkmart.config;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.thinkmart.model.Product;
import com.thinkmart.model.Role;
import com.thinkmart.model.User;
import com.thinkmart.repository.ProductRepository;
import com.thinkmart.repository.UserRepository;

/** On first start: creates the admin account and the 12 products from the original frontend. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(ProductRepository productRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            User admin = new User();
            admin.setFirstName("Site");
            admin.setLastName("Admin");
            admin.setEmail(adminEmail);
            admin.setPhone("9876543210");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setState("Andhra Pradesh");
            admin.setAddress("Think Mart HQ, Andhra Pradesh, India");
            userRepository.save(admin);
        }

        if (productRepository.count() == 0) {
            add("Mobile", "VIVO V29", "34999", 4.9, "vivo-v29",
                    "Bright AMOLED display, 50MP camera and all-day battery with fast charging.");
            add("Mobile", "Samsung Galaxy", "45000", 4.7, "samsung-galaxy",
                    "Smooth 120Hz screen, powerful processor and a camera that shines in low light.");
            add("Laptop", "HP Pavilion", "60000", 4.5, "hp-pavilion",
                    "Thin and light laptop with 16GB RAM and 512GB SSD for study and work.");
            add("Headphones", "boAt Rockerz", "2499", 4.3, "boat-rockerz",
                    "Wireless over-ear headphones with deep bass and 40 hours of playback.");
            add("Smart Watch", "Noise ColorFit", "3499", 4.2, "noise-colorfit",
                    "Tracks steps, heart rate and sleep, with calls and notifications on your wrist.");
            add("Monitor", "Dell 24 inch", "9999", 4.4, "dell-monitor",
                    "Full HD IPS monitor with slim bezels and a flicker-free, eye-friendly screen.");
            add("Camera", "Canon EOS", "42000", 4.8, "canon-eos",
                    "24MP mirrorless camera for sharp photos and Full HD video.");
            add("Speaker", "JBL Flip", "5999", 4.6, "jbl-flip",
                    "Portable waterproof Bluetooth speaker with powerful, clear sound.");
            add("Keyboard", "Logitech K380", "1999", 4.5, "logitech-k380",
                    "Compact wireless keyboard that pairs with up to three devices.");
            add("Mouse", "HP Wireless", "899", 4.1, "hp-mouse",
                    "Comfortable wireless mouse with a long-lasting battery.");
            add("Television", "Sony Bravia 43", "55000", 4.7, "sony-bravia",
                    "4K smart TV with vivid colours and built-in streaming apps.");
            add("Power Bank", "Mi 20000mAh", "1499", 4.4, "mi-powerbank",
                    "High-capacity power bank with fast charging for two devices at once.");
        }
    }

    private void add(String category, String brand, String price, double rating, String image, String description) {
        Product p = new Product();
        p.setCategory(category);
        p.setBrand(brand);
        p.setPrice(new BigDecimal(price));
        p.setRating(rating);
        p.setDescription(description);
        p.setImageUrl("/images/" + image + ".jpg");
        p.setStock(50);
        productRepository.save(p);
    }
}
