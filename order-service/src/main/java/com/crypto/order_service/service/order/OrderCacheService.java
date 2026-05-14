package com.crypto.order_service.service.order;

import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_HASH_PREFIX = "order:";
    private static final String OPEN_ORDERS_SET_PREFIX = "orders:open:";

    // Stocker un ordre ouvert dans Redis
    public void cacheOpenOrder(Order order) {
        String orderKey = ORDER_HASH_PREFIX + order.getId();
        Map<String, String> fields = new HashMap<>();
        fields.put("userId", order.getUserId().toString());
        fields.put("symbol", order.getSymbol());
        fields.put("type", order.getType().name());
        fields.put("quantity", order.getQuantity().toString());
        if (order.getPrice() != null) fields.put("price", order.getPrice().toString());
        if (order.getStopLoss() != null) fields.put("stopLoss", order.getStopLoss().toString());
        if (order.getTakeProfit() != null) fields.put("takeProfit", order.getTakeProfit().toString());
        fields.put("status", order.getStatus().name());

        redisTemplate.opsForHash().putAll(orderKey, fields);
        redisTemplate.opsForSet().add(OPEN_ORDERS_SET_PREFIX + order.getSymbol(), order.getId());
        log.debug("Ordre {} ajouté au cache", order.getId());
    }

    // Supprimer un ordre qui n'est plus ouvert
    public void removeOpenOrder(String orderId, String symbol) {
        redisTemplate.delete(ORDER_HASH_PREFIX + orderId);
        redisTemplate.opsForSet().remove(OPEN_ORDERS_SET_PREFIX + symbol, orderId);
        log.debug("Ordre {} retiré du cache", orderId);
    }

    // Récupérer la liste des IDs d'ordres ouverts pour un symbole
    public Set<String> getOpenOrderIds(String symbol) {
        Set<Object> members = redisTemplate.opsForSet().members(OPEN_ORDERS_SET_PREFIX + symbol);
        if (members == null) return Collections.emptySet();
        return members.stream().map(Object::toString).collect(Collectors.toSet());
    }

    // Récupérer un ordre depuis le cache
    public Optional<Order> getOrder(String orderId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(ORDER_HASH_PREFIX + orderId);
        if (entries.isEmpty()) return Optional.empty();

        Order order = Order.builder()
                .id(orderId)
                .userId(Long.valueOf(entries.get("userId").toString()))
                .symbol(entries.get("symbol").toString())
                .type(OrderType.valueOf(entries.get("type").toString()))
                .quantity(new BigDecimal(entries.get("quantity").toString()))
                .status(OrderStatus.valueOf(entries.get("status").toString()))
                .build();

        if (entries.containsKey("price")) order.setPrice(new BigDecimal(entries.get("price").toString()));
        if (entries.containsKey("stopLoss")) order.setStopLoss(new BigDecimal(entries.get("stopLoss").toString()));
        if (entries.containsKey("takeProfit")) order.setTakeProfit(new BigDecimal(entries.get("takeProfit").toString()));

        return Optional.of(order);
    }
}