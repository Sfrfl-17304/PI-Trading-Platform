package com.crypto.order_service.model.dto;

import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private String id;
    private Long userId;
    private String symbol;
    private OrderType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .type(order.getType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .stopLoss(order.getStopLoss())
                .takeProfit(order.getTakeProfit())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .closedAt(order.getClosedAt())
                .build();
    }
}