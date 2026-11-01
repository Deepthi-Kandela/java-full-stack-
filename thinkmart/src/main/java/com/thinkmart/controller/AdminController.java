package com.thinkmart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.OrderDto;
import com.thinkmart.dto.StatusRequest;
import com.thinkmart.model.ContactMessage;
import com.thinkmart.model.Role;
import com.thinkmart.model.User;
import com.thinkmart.repository.ContactMessageRepository;
import com.thinkmart.security.Access;
import com.thinkmart.security.AuthInterceptor;
import com.thinkmart.service.OrderService;

import jakarta.validation.Valid;

/** Admin-only endpoints. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderService orderService;
    private final ContactMessageRepository contactMessageRepository;

    public AdminController(OrderService orderService, ContactMessageRepository contactMessageRepository) {
        this.orderService = orderService;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping("/orders")
    public List<OrderDto> orders(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        Access.requireRole(user, Role.ADMIN);
        return orderService.allOrders();
    }

    @PutMapping("/orders/{id}/status")
    public OrderDto updateStatus(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user,
                                 @PathVariable Long id,
                                 @Valid @RequestBody StatusRequest request) {
        Access.requireRole(user, Role.ADMIN);
        return orderService.updateStatus(id, request.status());
    }

    @GetMapping("/messages")
    public List<ContactMessage> messages(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        Access.requireRole(user, Role.ADMIN);
        return contactMessageRepository.findAllByOrderByIdDesc();
    }
}
