package com.crypto.order_service.model.entity;

import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @Column(precision = 20, scale = 8)
    private BigDecimal stopLoss;

    @Column(precision = 20, scale = 8)
    private BigDecimal takeProfit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.OPEN;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}