package com.example.userservice.dto;

import java.math.BigDecimal;

public class BalanceResponse {

    private BigDecimal newBalance;

    public BalanceResponse() {
    }

    public BalanceResponse(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }
}
