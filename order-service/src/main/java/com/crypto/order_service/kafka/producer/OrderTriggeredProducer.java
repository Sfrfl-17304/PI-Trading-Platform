package com.crypto.order_service.kafka.producer;

import com.crypto.order_service.model.event.OrderTriggeredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTriggeredProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(OrderTriggeredEvent event) {
        kafkaTemplate.send("order-triggers", event);
    }
}