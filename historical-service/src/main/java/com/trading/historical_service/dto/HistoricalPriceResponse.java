package com.trading.historical_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalPriceResponse {
    private String symbol;
    private List<PricePoint> data;
}