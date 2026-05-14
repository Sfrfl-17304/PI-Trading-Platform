package com.crypto.order_service.model.dto;

import com.crypto.order_service.model.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String symbol;
    @NotNull
    private OrderType type;
    @NotNull @Positive
    private BigDecimal quantity;
    private BigDecimal price;        // optionnel
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
}