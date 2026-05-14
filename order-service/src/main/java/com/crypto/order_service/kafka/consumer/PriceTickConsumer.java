package com.crypto.order_service.kafka.consumer;

import com.crypto.order_service.model.dto.PriceTick;
import com.crypto.order_service.service.execution.OrderExecutionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceTickConsumer {

    private final OrderExecutionEngine executionEngine;

    @KafkaListener(topics = "raw-prices.crypto", groupId = "order-service-group")
    public void consumeCrypto(PriceTick tick) {
        executionEngine.processTick(tick);
    }

    @KafkaListener(topics = "raw-prices.indices", groupId = "order-service-group")
    public void consumeIndices(PriceTick tick) {
        executionEngine.processTick(tick);
    }
}