package com.trading.historical_service.controller;

import com.trading.historical_service.dto.HistoricalPriceResponse;
import com.trading.historical_service.dto.PricePoint;
import com.trading.historical_service.service.HistoricalPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/historical")
@RequiredArgsConstructor
public class HistoricalController {

    private final HistoricalPriceService priceService;

    @GetMapping("/prices")
    public HistoricalPriceResponse getPrices(
            @RequestParam String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "1m") String interval
    ) {
        List<PricePoint> points = priceService.getPrices(symbol, start, end, interval);
        return new HistoricalPriceResponse(symbol, points);
    }
}