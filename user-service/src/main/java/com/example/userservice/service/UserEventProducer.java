package com.example.userservice.service;

import com.example.userservice.event.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private final String topic;

    public UserEventProducer(
        KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate,
        @Value("${app.kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(topic, event.getUserId().toString(), event);
    }
}
