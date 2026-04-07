package com.crypto.market_data_service.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

    @Bean
    @SuppressWarnings("null")
    public InfluxDBClient influxDBClient(InfluxProperties props) {
        return InfluxDBClientFactory.create(props.getUrl(),
                props.getToken().toCharArray(),
                props.getOrg(),
                props.getBucket());
    }
}