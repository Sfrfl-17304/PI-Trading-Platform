package com.crypto.market_data_service.service;

import com.crypto.market_data_service.model.PriceTick;

public interface PriceFetcher {
    PriceTick fetchPrice(String symbol);
    String getSourceType();
}
