package com.crypto.market_data_service.controller;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.market_data_service.model.PriceTick;
import com.crypto.market_data_service.service.PriceStore;

@RestController
@RequestMapping("/prices")
@CrossOrigin
public class PriceController {

  private final PriceStore priceStore;

  public PriceController(PriceStore priceStore){
    this.priceStore = priceStore;
  }

  @GetMapping("/latest")
  public ResponseEntity<Collection<PriceTick>> getLatest(){
    return ResponseEntity.ok(priceStore.getAll());
  }
}
