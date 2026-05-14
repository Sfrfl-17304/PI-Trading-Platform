package com.trading.historical_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricePoint {
    private long timestamp;   // en millisecondes
    private double price;
}