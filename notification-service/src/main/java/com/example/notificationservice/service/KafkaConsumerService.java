package com.example.notificationservice.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Service
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final SimpMessagingTemplate mt;
    private final ObjectMapper om;
    private final NotificationService ns;

    public KafkaConsumerService(SimpMessagingTemplate mt, ObjectMapper om, NotificationService ns) {
        this.mt = mt; this.om = om; this.ns = ns;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void consumeUserEvents(String msg) {
        try {
            Map p = om.readValue(msg, Map.class);
            mt.convertAndSend("/topic/admin-alerts", p);
            if (p.containsKey("userId")) ns.saveNotification(p.get("userId").toString(), "Welcome!", "USER_REGISTERED");
        } catch (Exception e) {
            logger.error("Error processing user-events message: {}", msg, e);
        }
    }

    @KafkaListener(topics = "prediction-events", groupId = "notification-group")
    public void consumePredictionEvents(String msg) {
        try {
            Map p = om.readValue(msg, Map.class);
            mt.convertAndSend("/topic/predictions", p);
        } catch (Exception e) {
            logger.error("Error processing prediction-events message: {}", msg, e);
        }
    }

    @KafkaListener(topics = "order-triggers", groupId = "notification-group")
    public void consumeOrderEvents(String msg) {
        try {
            Map p = om.readValue(msg, Map.class);
            if (p.containsKey("userId")) {
                String uid = p.get("userId").toString();
                mt.convertAndSendToUser(uid, "/queue/orders", p);
                ns.saveNotification(uid, "Order updated", "ORDER_UPDATE");
            }
        } catch (Exception e) {
            logger.error("Error processing order-events message: {}", msg, e);
        }
    }
}
