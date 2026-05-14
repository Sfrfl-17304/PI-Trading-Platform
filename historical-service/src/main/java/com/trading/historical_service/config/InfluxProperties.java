package com.trading.historical_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "influx")
public class InfluxProperties {
    private String url;
    private String token;
    private String org;
    private String bucket;
    private String measurement;
}