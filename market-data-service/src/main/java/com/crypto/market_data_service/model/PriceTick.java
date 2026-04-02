package com.crypto.market_data_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceTick {
    private String symbol;
    private double price;
    private Instant timestamp;
}
