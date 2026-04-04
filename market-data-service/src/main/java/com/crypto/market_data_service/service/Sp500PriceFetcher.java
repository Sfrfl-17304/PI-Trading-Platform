package com.crypto.market_data_service.service;

import com.crypto.market_data_service.model.PriceTick;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.Map;

@Service
public class Sp500PriceFetcher implements PriceFetcher {

    private final RestTemplate restTemplate = new RestTemplate();
    // L'URL de l'API Finnhub pour le quote du S&P 500
    private static final String URL = "https://finnhub.io/api/v1/quote?symbol=SPY&token=d78fi01r01qhel7vod7gd78fi01r01qhel7vod80";

    @Override
    public PriceTick fetchPrice(String symbol) {
        // Appel à l'API Finnhub
        Map<String, Object> response = restTemplate.getForObject(URL, Map.class);
        
        // Extraction du prix (le champ 'c' représente le prix actuel)
        double price = ((Number) response.get("c")).doubleValue();
        
        // Création du PriceTick avec la source "SP500"
        return new PriceTick("SP500", symbol, price, Instant.now());
    }

    @Override
    public String getSourceType() {
        return "sp500";
    }
}