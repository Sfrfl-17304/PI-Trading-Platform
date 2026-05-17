package com.example.userservice.event;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TradeEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TradeEventConsumer.class);
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public TradeEventConsumer(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "balance-update", groupId = "user-service-group")
    public void consumeBalanceUpdate(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            UUID userId = UUID.fromString(payload.get("userId").toString());
            
            // Allow negative balances for sells, positive for buys depending on ordertype logic sent by order-service
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setBalance(user.getBalance().add(amount));
                userRepository.save(user);
                logger.info("Updated balance for user {} by {}. New balance: {}", userId, amount, user.getBalance());
            } else {
                logger.warn("Received balance update for unknown user ID: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to process balance-update message: {}", message, e);
        }
    }
}
