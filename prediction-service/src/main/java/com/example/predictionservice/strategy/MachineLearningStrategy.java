package com.example.predictionservice.strategy;

import com.example.predictionservice.model.Signal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class MachineLearningStrategy implements PredictionStrategy {

    private final Random random = new Random();

    @Override
    public Signal calculateSignal(String symbol, List<Double> prices) {
        // Here you would integrate with your pre-trained model (e.g. using ONNX, DL4J, or a REST call to a Python service)
        // For demonstration, we'll return a stubbed signal based on a simple heuristic/randomness.
        
        if (prices.isEmpty()) {
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        double latestPrice = prices.get(prices.size() - 1);
        
        // Mock prediction score between 0.0 and 1.0 (where > 0.6 is BUY, < 0.4 is SELL)
        double predictionScore = 0.2 + (0.8 - 0.2) * random.nextDouble(); 

        String action;
        double confidence;

        if (predictionScore > 0.6) {
            action = "BUY";
            confidence = predictionScore;
        } else if (predictionScore < 0.4) {
            action = "SELL";
            confidence = 1.0 - predictionScore;
        } else {
            action = "HOLD";
            confidence = 0.5;
        }

        return new Signal(symbol, action, confidence, System.currentTimeMillis());
    }

    @Override
    public String getName() {
        return "ML_PRE_TRAINED_STRATEGY";
    }
}
