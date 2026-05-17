package com.example.predictionservice.strategy;

import com.example.predictionservice.model.Signal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmaStrategy implements PredictionStrategy {

    private static final int SHORT_PERIOD = 9;
    private static final int LONG_PERIOD = 21;

    @Override
    public Signal calculateSignal(String symbol, List<Double> prices) {
        if (prices.size() < LONG_PERIOD) {
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        double shortEma = calculateEMA(prices, SHORT_PERIOD);
        double longEma = calculateEMA(prices, LONG_PERIOD);

        String action;
        double confidence;

        if (shortEma > longEma) {
            action = "BUY";
            confidence = Math.min((shortEma - longEma) / longEma * 10.0, 1.0); // Rough confidence based on spread
        } else {
            action = "SELL";
            confidence = Math.min((longEma - shortEma) / longEma * 10.0, 1.0);
        }

        return new Signal(symbol, action, Math.max(0.1, confidence), System.currentTimeMillis());
    }

    private double calculateEMA(List<Double> prices, int period) {
        double multiplier = 2.0 / (period + 1);
        
        // Start with SMA for the first 'period' elements
        double sma = 0.0;
        int startIndex = prices.size() - period;
        for (int i = startIndex; i < prices.size(); i++) {
            sma += prices.get(i);
        }
        sma /= period;

        double ema = sma;
        // In a real scenario, you'd calculate EMA throughout the entire history. 
        // For simplicity here, we simulate it with the recent window.
        for (int i = startIndex; i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * multiplier + ema;
        }

        return ema;
    }

    @Override
    public String getName() {
        return "EMA_STRATEGY";
    }
}
