package com.example.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BalanceRequest {

    public enum Type {
        DEPOSIT,
        WITHDRAW
    }

    @NotNull
    @DecimalMin(value = "0.00000001", inclusive = true)
    private BigDecimal amount;

    @NotNull
    private Type type;

    public BalanceRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
