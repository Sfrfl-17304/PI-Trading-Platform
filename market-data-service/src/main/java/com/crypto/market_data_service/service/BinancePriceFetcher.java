package com.crypto.market_data_service.service;

import com.crypto.market_data_service.model.PriceTick;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.Map;

@Service
public class BinancePriceFetcher implements PriceFetcher {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.binance.com/api/v3/ticker/price?symbol=";

    @Override
    public PriceTick fetchPrice(String symbol) {
        String url = BASE_URL + symbol;
        Map<String, String> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("price")) {
            throw new RuntimeException("Réponse invalide de Binance pour " + symbol);
        }
        double price = Double.parseDouble(response.get("price"));
        return new PriceTick("BINANCE",symbol, price, Instant.now());
    }

    @Override
    public String getSourceType(){
        return "binance";
    }
}