package com.crypto.market_data_service.service;

import com.crypto.market_data_service.model.PriceTick;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import com.influxdb.client.domain.WritePrecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class InfluxDBWriter {
    private static final Logger log = LoggerFactory.getLogger(InfluxDBWriter.class);

    @Value("${influx.url}")
    private String url;

    @Value("${influx.token}")
    private String token;

    @Value("${influx.org}")
    private String org;

    @Value("${influx.bucket}")
    private String bucket;

    private InfluxDBClient influxDBClient;

    @PostConstruct
    public void init() {
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        log.info("Connecté à InfluxDB v2 sur {}", url);
    }

    public void write(PriceTick tick) {
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("price_ticks")
                    .addTag("source", tick.getSource())
                    .addTag("symbol", tick.getSymbol())
                    .addField("price", tick.getPrice())
                    .time(tick.getTimestamp().toEpochMilli(), WritePrecision.MS);
            writeApi.writePoint(point);
            log.debug("Écrit dans InfluxDB : {} {}", tick.getSymbol(), tick.getPrice());
        } catch (Exception e) {
            log.error("Erreur écriture InfluxDB : {}", e.getMessage());
        }
    }
}