package com.crypto.order_service.service.execution;

import com.crypto.order_service.kafka.producer.BalanceUpdateProducer;
import com.crypto.order_service.kafka.producer.OrderTriggeredProducer;
import com.crypto.order_service.model.dto.PriceTick;
import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import com.crypto.order_service.model.event.BalanceUpdateEvent;
import com.crypto.order_service.model.event.OrderTriggeredEvent;
import com.crypto.order_service.repository.OrderRepository;
import com.crypto.order_service.service.order.OrderCacheService;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionEngine {

    private final OrderCacheService orderCacheService;
    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderTriggeredProducer orderTriggeredProducer;
    private final BalanceUpdateProducer balanceUpdateProducer;

    private LoadingCache<String, List<Order>> openOrdersCache;

    @PostConstruct
    public void init() {
        openOrdersCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .build(symbol -> loadOpenOrders(symbol));
    }

    private List<Order> loadOpenOrders(String symbol) {
        log.debug("Chargement des ordres ouverts pour {} depuis Redis", symbol);
        Set<String> ids = orderCacheService.getOpenOrderIds(symbol);
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Order> orders = new ArrayList<>();
        for (String id : ids) {
            Optional<Order> optOrder = orderCacheService.getOrder(id);
            optOrder.ifPresent(orders::add);
        }
        return orders;
    }

    public void processTick(PriceTick tick) {
        String symbol = tick.getSymbol();
        double currentPrice = tick.getPrice();
        log.debug("Tick reçu : {} {}", symbol, currentPrice);

        List<Order> openOrders = openOrdersCache.get(symbol);
        if (openOrders.isEmpty()) {
            return;
        }

        List<Order> triggered = new ArrayList<>();
        for (Order order : openOrders) {
            if (isStopLossTriggered(order, currentPrice) || isTakeProfitTriggered(order, currentPrice)) {
                triggered.add(order);
            }
        }

        for (Order order : triggered) {
            // Verrou Redis optionnel pour éviter les double exécutions
            String lockKey = "lock:order:" + order.getId();
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofSeconds(2));
            if (Boolean.TRUE.equals(locked)) {
                try {
                    executeOrder(order, currentPrice);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                log.warn("Ordre {} déjà en cours d'exécution (verrouillé)", order.getId());
            }
        }
    }

    private boolean isStopLossTriggered(Order order, double currentPrice) {
        if (order.getStopLoss() == null) {
            return false;
        }
        BigDecimal stopLoss = order.getStopLoss();
        BigDecimal price = BigDecimal.valueOf(currentPrice);
        if (order.getType() == OrderType.BUY) {
            return price.compareTo(stopLoss) <= 0;
        } else { // SELL
            return price.compareTo(stopLoss) >= 0;
        }
    }

    private boolean isTakeProfitTriggered(Order order, double currentPrice) {
        if (order.getTakeProfit() == null) {
            return false;
        }
        BigDecimal takeProfit = order.getTakeProfit();
        BigDecimal price = BigDecimal.valueOf(currentPrice);
        if (order.getType() == OrderType.BUY) {
            return price.compareTo(takeProfit) >= 0;
        } else { // SELL
            return price.compareTo(takeProfit) <= 0;
        }
    }

    private void executeOrder(Order order, double executionPrice) {
        log.info("🚀 Ordre {} déclenché : {} à {}", order.getId(), order.getType(), executionPrice);
        // 1. Retirer de Redis
        orderCacheService.removeOpenOrder(order.getId(), order.getSymbol());
        // 2. Mettre à jour en base
        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());
        orderRepository.save(order);
         // Envoi de l'événement de déclenchement
        String triggerType = (order.getStopLoss() != null && isStopLossTriggered(order, executionPrice))
                            ? "STOP_LOSS" : "TAKE_PROFIT";
        OrderTriggeredEvent triggeredEvent = OrderTriggeredEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .symbol(order.getSymbol())
            .type(order.getType().name())
            .quantity(order.getQuantity().doubleValue())
            .executionPrice(executionPrice)
            .triggerType(triggerType)
            .timestamp(Instant.now())
            .build();
        orderTriggeredProducer.send(triggeredEvent);

        // Calcul du profit/perte (simplifié)
        double profitLoss = 0.0;
        if (order.getPrice() != null) {
            double entry = order.getPrice().doubleValue();
            double quantity = order.getQuantity().doubleValue();
            profitLoss = (order.getType() == OrderType.BUY)
                        ? (executionPrice - entry) * quantity
                        : (entry - executionPrice) * quantity;
        }
        BalanceUpdateEvent balanceEvent = BalanceUpdateEvent.builder()
                .userId(order.getUserId())
                .amount(profitLoss)
                .reason("order_execution")
                .referenceId(order.getId())
                .timestamp(Instant.now())
                .build();
        balanceUpdateProducer.send(balanceEvent);
        }
}