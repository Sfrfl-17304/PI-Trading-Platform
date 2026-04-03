package com.crypto.market_data_service.scheduler;

import com.crypto.market_data_service.model.PriceTick;
import com.crypto.market_data_service.service.PriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class PriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceScheduler.class);

    @Autowired
    private PriceFetcher priceFetcher;

    @Scheduled(fixedDelay = 200) // toutes les secondes
    public void fetchAndLogPrice() {
        String symbol = "BTCUSDT"; // temporaire, un seul symbole pour tester
        try {
            PriceTick tick = priceFetcher.fetchPrice(symbol);
            log.info("Prix récupéré : {} -> {} $ à {}", tick.getSymbol(), tick.getPrice(), tick.getTimestamp());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du prix pour {} : {}", symbol, e.getMessage());
        }
    }
}
