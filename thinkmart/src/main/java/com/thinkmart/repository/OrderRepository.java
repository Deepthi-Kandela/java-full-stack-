package com.thinkmart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thinkmart.model.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserIdOrderByIdDesc(Long userId);

    List<CustomerOrder> findAllByOrderByIdDesc();
}
