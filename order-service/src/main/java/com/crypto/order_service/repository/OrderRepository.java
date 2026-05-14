package com.crypto.order_service.repository;

import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserId(Long userId);
}