package com.example.predictionservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceData {
    private String symbol;
    private double price;
    private long timestamp;
}
