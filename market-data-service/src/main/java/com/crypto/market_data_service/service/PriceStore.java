package com.crypto.market_data_service.service;

import org.springframework.stereotype.Service;

import com.crypto.market_data_service.model.PriceTick;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceStore {
  private final Map<String, PriceTick> storeMap = new ConcurrentHashMap<>();

  public void update(PriceTick tick){
    storeMap.put(tick.getSymbol(),tick);
  }

  public Collection<PriceTick> getAll(){
    return storeMap.values();
  }
}
