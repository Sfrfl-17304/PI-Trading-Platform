package com.crypto.order_service.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTriggeredEvent {
    private String orderId;
    private Long userId;
    private String symbol;
    private String type;            // BUY ou SELL
    private double quantity;
    private double executionPrice;
    private String triggerType;     // "STOP_LOSS" ou "TAKE_PROFIT"
    private Instant timestamp;
}