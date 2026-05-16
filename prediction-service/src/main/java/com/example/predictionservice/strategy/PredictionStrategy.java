package com.example.predictionservice.strategy;

import com.example.predictionservice.model.Signal;
import java.util.List;

public interface PredictionStrategy {
    Signal calculateSignal(String symbol, List<Double> prices);
    String getName();
}
