package com.example.predictionservice.service;

import com.example.predictionservice.model.Signal;
import com.example.predictionservice.strategy.PredictionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsensusService {

    private final List<PredictionStrategy> strategies;

    @Autowired
    public ConsensusService(List<PredictionStrategy> strategies) {
        this.strategies = strategies;
    }

    public Signal getConsensusSignal(String symbol, List<Double> prices) {
        if (prices == null || prices.isEmpty()) {
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        List<Signal> signals = strategies.stream()
                .map(strategy -> strategy.calculateSignal(symbol, prices))
                .collect(Collectors.toList());

        // Consensus logic: Calculate a weighted score
        double totalScore = 0.0;
        double totalWeight = 0.0;

        for (Signal signal : signals) {
            double weight = signal.getConfidence();
            totalWeight += weight;

            if ("BUY".equalsIgnoreCase(signal.getAction())) {
                totalScore += weight;
            } else if ("SELL".equalsIgnoreCase(signal.getAction())) {
                totalScore -= weight;
            }
        }

        double normalizedScore = totalWeight == 0 ? 0 : totalScore / totalWeight;
        
        String finalAction;
        double finalConfidence = Math.abs(normalizedScore);

        if (normalizedScore > 0.3) {
            finalAction = "BUY";
        } else if (normalizedScore < -0.3) {
            finalAction = "SELL";
        } else {
            finalAction = "HOLD";
            // If it's a hold, recalculate confidence as distance to threshold
            finalConfidence = 1.0 - Math.abs(normalizedScore) / 0.3; 
        }

        return new Signal(symbol, finalAction, finalConfidence, System.currentTimeMillis());
    }
}
