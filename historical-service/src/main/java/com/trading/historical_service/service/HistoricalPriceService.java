package com.trading.historical_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.trading.historical_service.config.InfluxProperties;
import com.trading.historical_service.dto.PricePoint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricalPriceService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalPriceService.class);

    private final InfluxDBClient influxDBClient;
    private final InfluxProperties influxProperties;

    /**
     * Récupère les prix historiques pour un symbole sur une période donnée,
     * avec agrégation optionnelle par intervalle.
     *
     * @param symbol   symbole (ex: BTCUSDT)
     * @param start    début de la période (inclus)
     * @param end      fin de la période (exclus)
     * @param interval chaîne Flux (ex: "1m", "5m", "1h") – peut être null ou vide
     * @return liste de PricePoint (timestamp, prix moyen)
     */
    public List<PricePoint> getPrices(String symbol, Instant start, Instant end, String interval) {
        // Construction de la requête Flux
        StringBuilder flux = new StringBuilder();
        flux.append(String.format("from(bucket: \"%s\")", influxProperties.getBucket()));
        flux.append(String.format(" |> range(start: %s, stop: %s)", start.toString(), end.toString()));
        flux.append(String.format(" |> filter(fn: (r) => r._measurement == \"%s\"", influxProperties.getMeasurement()));
        flux.append(String.format(" and r.symbol == \"%s\")", symbol));

        // Agrégation uniquement si interval est fourni et non vide
        if (interval != null && !interval.trim().isEmpty()) {
            flux.append(String.format(" |> aggregateWindow(every: %s, fn: mean)", interval));
        }

        flux.append(" |> yield(name: \"prices\")");

        String query = flux.toString();
        log.debug("Requête Flux exécutée : {}", query);

        List<PricePoint> points = new ArrayList<>();

        QueryApi queryApi = influxDBClient.getQueryApi();
        try {
            List<FluxTable> tables = queryApi.query(query);
            if (tables != null && !tables.isEmpty()) {
                FluxTable table = tables.get(0);
                for (FluxRecord record : table.getRecords()) {
                    // Le timestamp peut être null (si pas de time column)
                    long timestamp = record.getTime() != null ? record.getTime().toEpochMilli() : 0L;
                    // La valeur est stockée dans _value (champ "price")
                    Double price = (Double) record.getValue();
                    if (price != null) {
                        points.add(new PricePoint(timestamp, price));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'interrogation d'InfluxDB : {}", e.getMessage(), e);
            throw new RuntimeException("Impossible de récupérer les données historiques", e);
        }

        return points;
    }
}