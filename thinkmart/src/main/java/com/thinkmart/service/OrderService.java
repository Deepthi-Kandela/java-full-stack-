package com.thinkmart.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkmart.dto.OrderDto;
import com.thinkmart.exception.ApiException;
import com.thinkmart.model.CartItem;
import com.thinkmart.model.CustomerOrder;
import com.thinkmart.model.OrderItem;
import com.thinkmart.model.OrderStatus;
import com.thinkmart.model.Product;
import com.thinkmart.model.User;
import com.thinkmart.repository.CartItemRepository;
import com.thinkmart.repository.OrderRepository;
import com.thinkmart.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /** Turns the user's cart into an order, reduces stock and empties the cart (all in one transaction). */
    @Transactional
    public OrderDto checkout(User user, String shippingAddress) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByIdAsc(user.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setShippingAddress(
                shippingAddress == null || shippingAddress.isBlank() ? user.getAddress() : shippingAddress.trim());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            if (!p.isActive() || p.getStock() < ci.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Not enough stock for " + p.getBrand() + ". Please update your cart.");
            }
            p.setStock(p.getStock() - ci.getQuantity());
            productRepository.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setCategory(p.getCategory());
            oi.setBrand(p.getBrand());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);

        CustomerOrder saved = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        return OrderDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> myOrders(User user) {
        return orderRepository.findByUserIdOrderByIdDesc(user.getId()).stream().map(OrderDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> allOrders() {
        return orderRepository.findAllByOrderByIdDesc().stream().map(OrderDto::from).toList();
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, String status) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown status: " + status);
        }
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A cancelled order cannot be re-opened");
        }
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            // put the items back into stock
            for (OrderItem oi : order.getItems()) {
                Product p = oi.getProduct();
                if (p != null) {
                    p.setStock(p.getStock() + oi.getQuantity());
                    productRepository.save(p);
                }
            }
        }
        order.setStatus(newStatus);
        return OrderDto.from(orderRepository.save(order));
    }
}
