package com.example.predictionservice.controller;

import com.example.predictionservice.model.Signal;
import com.example.predictionservice.service.ConsensusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predict")
public class PredictionController {

    private final ConsensusService consensusService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PredictionController(ConsensusService consensusService, KafkaTemplate<String, String> kafkaTemplate) {
        this.consensusService = consensusService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/{symbol}")
    public Signal predict(@PathVariable String symbol, @RequestBody List<Double> prices) {
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
