package com.example.predictionservice.controller;

import com.example.predictionservice.model.Signal;
import com.example.predictionservice.service.ConsensusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predict")
public class PredictionController {

    private final ConsensusService consensusService;

    @Autowired
    public PredictionController(ConsensusService consensusService) {
        this.consensusService = consensusService;
    }

    @PostMapping("/{symbol}")
    public Signal predict(@PathVariable String symbol, @RequestBody List<Double> prices) {
        return consensusService.getConsensusSignal(symbol, prices);
    }
}
