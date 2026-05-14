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
public class BalanceUpdateEvent {
    private Long userId;
    private double amount;          // positif pour crédit, négatif pour débit
    private String reason;          // "order_execution"
    private String referenceId;     // orderId
    private Instant timestamp;
}