package com.example.predictionservice.controller;

import com.example.predictionservice.model.Signal;
import com.example.predictionservice.service.ConsensusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/predict")
public class PredictionController {

    private final ConsensusService consensusService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${services.historical.url:http://localhost:8087}")
    private String historicalServiceUrl;

    @Autowired
    public PredictionController(ConsensusService consensusService, KafkaTemplate<String, String> kafkaTemplate) {
        this.consensusService = consensusService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    // Strategy 1: Predict in real-time based on data provided by frontend
    @PostMapping("/{symbol}")
    public Signal predictRealTime(@PathVariable String symbol, @RequestBody List<Double> prices) {
        return processAndSendSignal(symbol, prices);
    }

    // Strategy 2: Predict using historical data autonomously fetched from historical-service
    @GetMapping("/{symbol}")
    public Signal predictFromHistory(@PathVariable String symbol) {
        // Fetch last 12 hours of data automatically in 15m intervals to get some good context
        Instant end = Instant.now();
        Instant start = end.minus(12, ChronoUnit.HOURS);

        String url = String.format("%s/api/historical/prices?symbol=%s&start=%s&end=%s&interval=15m",
                historicalServiceUrl, symbol, start.toString(), end.toString());

        List<Double> prices = new ArrayList<>();
        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                for (Map<String, Object> point : data) {
                    if (point.containsKey("price")) {
                        prices.add(Double.valueOf(point.get("price").toString()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch historical data: " + e.getMessage());
        }

        return processAndSendSignal(symbol, prices);
    }

    private Signal processAndSendSignal(String symbol, List<Double> prices) {
        Signal signal = consensusService.getConsensusSignal(symbol, prices);

        try {
            String signalJson = objectMapper.writeValueAsString(signal);
            kafkaTemplate.send("prediction-events", symbol, signalJson);
        } catch (Exception e) {
            System.err.println("Failed to serialize or send signal to Kafka: " + e.getMessage());
        }

        return signal;
    }
}
