package com.example.predictionservice.strategy;

import com.example.predictionservice.model.Signal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MachineLearningStrategy implements PredictionStrategy {

    @Value("${ml.python-api.url:http://localhost:5000/predict}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Signal calculateSignal(String symbol, List<Double> prices) {
        if (prices == null || prices.size() < 5) {
            // Need some minimum data points for an ARIMA prediction
            return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("symbol", symbol);
            requestBody.put("prices", prices);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String action = (String) responseBody.getOrDefault("action", "HOLD");
                double confidence = ((Number) responseBody.getOrDefault("confidence", 0.0)).doubleValue();
                return new Signal(symbol, action, confidence, System.currentTimeMillis());
            }
        } catch (Exception e) {
            System.err.println("Failed to call Python ML API: " + e.getMessage());
        }

        return new Signal(symbol, "HOLD", 0.0, System.currentTimeMillis());
    }

    @Override
    public String getName() {
        return "ML_ARIMA_STRATEGY";
    }
}
