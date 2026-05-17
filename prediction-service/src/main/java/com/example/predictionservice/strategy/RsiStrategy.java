package com.example.predictionservice.strategy;

import com.example.predictionservice.model.Signal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RsiStrategy implements PredictionStrategy {

    private static final int PERIOD = 14;

    @Override
    public Signal calculateSignal(String symbol, List<Double> prices) {
        if (prices.size() <= PERIOD) {
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        double gains = 0.0;
        double losses = 0.0;

        int startIndex = prices.size() - PERIOD - 1;
        for (int i = startIndex; i < prices.size() - 1; i++) {
            double difference = prices.get(i + 1) - prices.get(i);
            if (difference > 0) {
                gains += difference;
            } else {
                losses -= difference;
            }
        }

        double avgGain = gains / PERIOD;
        double avgLoss = losses / PERIOD;

        if (avgLoss == 0) {
            return new Signal(symbol, "BUY", 1.0, System.currentTimeMillis());
        }

        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));

        String action = "HOLD";
        double confidence = 0.5;

        if (rsi < 30) {
            action = "BUY";
            confidence = (30 - rsi) / 30.0;
        } else if (rsi > 70) {
            action = "SELL";
            confidence = (rsi - 70) / 30.0;
        }

        return new Signal(symbol, action, confidence, System.currentTimeMillis());
    }

    @Override
    public String getName() {
        return "RSI_STRATEGY";
    }
}
