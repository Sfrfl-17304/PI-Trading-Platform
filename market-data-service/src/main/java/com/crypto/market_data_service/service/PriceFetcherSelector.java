package com.crypto.market_data_service.service;


import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceFetcherSelector {
    private final Map<String, PriceFetcher> fetcherMap = new ConcurrentHashMap<>();

    
    public PriceFetcherSelector(List<PriceFetcher> fetchers) {
        for (PriceFetcher fetcher : fetchers) {
            fetcherMap.put(fetcher.getSourceType().toLowerCase(), fetcher);
        }
    }

    public PriceFetcher getFetcher(String sourceType) {
        PriceFetcher fetcher = fetcherMap.get(sourceType.toLowerCase());
        if (fetcher == null) {
            throw new IllegalArgumentException("Aucun fetcher trouvé pour : " + sourceType);
        }
        return fetcher;
    }
}
