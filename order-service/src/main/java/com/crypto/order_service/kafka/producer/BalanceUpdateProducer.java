package com.crypto.order_service.kafka.producer;

import com.crypto.order_service.model.event.BalanceUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceUpdateProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(BalanceUpdateEvent event) {
        kafkaTemplate.send("balance-update", event);
    }
}