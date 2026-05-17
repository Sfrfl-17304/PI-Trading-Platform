package com.trading.historical_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.trading.historical_service.config.InfluxProperties;

@SpringBootApplication
@EnableConfigurationProperties(InfluxProperties.class)
public class HistoricalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HistoricalServiceApplication.class, args);
	}

}
