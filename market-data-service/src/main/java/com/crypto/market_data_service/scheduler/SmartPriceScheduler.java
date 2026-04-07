package com.crypto.market_data_service.scheduler;

import com.crypto.market_data_service.model.PriceTick;
import com.crypto.market_data_service.service.InfluxDBWriter;
import com.crypto.market_data_service.service.PriceFetcher;
import com.crypto.market_data_service.service.PriceFetcherSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
public class SmartPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(SmartPriceScheduler.class);

    
    private PriceFetcherSelector selector;
    private KafkaTemplate<String, String> kafkaTemplate;
    private InfluxDBWriter influxDBWriter;
    
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    public SmartPriceScheduler(PriceFetcherSelector selector,KafkaTemplate<String, String> kafkaTemplate,InfluxDBWriter influxDBWriter) {
        this.selector = selector;
        this.kafkaTemplate = kafkaTemplate;
        this.influxDBWriter = influxDBWriter;
    }
    @Scheduled(fixedDelay = 5000)
    public void fetchAllPrices() {
        long start =System.currentTimeMillis();
        List<Runnable> tasks =new ArrayList<>();
        PriceFetcher binanceFetcher = selector.getFetcher("binance");
        List<String> cryptoSymbols = List.of("BTCUSDT", "ETHUSDT");
        for (String symbol : cryptoSymbols) {
            tasks.add(() -> processAndSend(binanceFetcher, symbol, "raw-prices.crypto"));
        }
        PriceFetcher sp500Fetcher = selector.getFetcher("sp500");
        tasks.add(() -> processAndSend(sp500Fetcher, "SPY", "raw-prices.indices"));
        tasks.forEach(executor::submit);
        long end = System.currentTimeMillis();
        log.info("Cycle de collecte soumis en {} ms (tâches parallélisées)", end - start);
    }

    private void processAndSend(PriceFetcher fetcher, String symbol, String topic) {
        try {
            PriceTick tick = fetcher.fetchPrice(symbol);
            String json = String.format(
            "{\"source\":\"%s\",\"symbol\":\"%s\",\"price\":%f,\"timestamp\":\"%s\"}",
            tick.getSource(),
            tick.getSymbol(),
            tick.getPrice(),
            tick.getTimestamp().toString()
        );
            kafkaTemplate.send(topic,tick.getSource(), json);
            log.info("Prix publié sur topic {} : {} -> {} $ (source: {})",topic, tick.getSymbol(), tick.getPrice(), tick.getSource());
            influxDBWriter.write(tick);
        } catch (Exception e) {
            log.error("Erreur pour {} (source: {}) : {}", symbol,
                    (fetcher != null ? fetcher.getSourceType() : "inconnu"), e.getMessage());
        }
    }
}