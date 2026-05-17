package com.example.predictionservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signal {
    private String symbol;
    private String action; // BUY, SELL, HOLD
    private double confidence; // 0.0 to 1.0
    private long timestamp;
}
