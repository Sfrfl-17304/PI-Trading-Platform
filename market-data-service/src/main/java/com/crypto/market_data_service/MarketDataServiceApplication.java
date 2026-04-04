package com.crypto.market_data_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationProperties
public class MarketDataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketDataServiceApplication.class, args);
	}

}
