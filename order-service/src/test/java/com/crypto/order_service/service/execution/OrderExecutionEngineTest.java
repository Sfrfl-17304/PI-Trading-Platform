package com.crypto.order_service.service.execution;

import com.crypto.order_service.kafka.producer.BalanceUpdateProducer;
import com.crypto.order_service.kafka.producer.OrderTriggeredProducer;
import com.crypto.order_service.model.dto.PriceTick;
import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import com.crypto.order_service.repository.OrderRepository;
import com.crypto.order_service.service.order.OrderCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderExecutionEngineTest {

    @Mock
    private OrderCacheService orderCacheService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private OrderTriggeredProducer orderTriggeredProducer;   // nouveau

    @Mock
    private BalanceUpdateProducer balanceUpdateProducer;     // nouveau

    @InjectMocks
    private OrderExecutionEngine engine;

    @BeforeEach
    void setUp() {
        // Initialise le cache (appel à @PostConstruct manuel)
        engine.init();

        // Configuration du mock Redis pour le verrou
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldTriggerStopLossForBuyOrder() {
        // given
        Order buyOrder = Order.builder()
                .id("order-1")
                .userId(1L)
                .symbol("BTC/USD")
                .type(OrderType.BUY)
                .quantity(new BigDecimal("0.01"))
                .price(new BigDecimal("45000"))
                .stopLoss(new BigDecimal("44000"))
                .status(OrderStatus.OPEN)
                .build();

        when(orderCacheService.getOpenOrderIds("BTC/USD"))
                .thenReturn(Collections.singleton("order-1"));
        when(orderCacheService.getOrder("order-1"))
                .thenReturn(Optional.of(buyOrder));
        when(valueOperations.setIfAbsent(eq("lock:order:order-1"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        // when
        PriceTick tick = new PriceTick("source", "BTC/USD", 43900.0, java.time.Instant.now());
        engine.processTick(tick);

        // then
        verify(orderCacheService).removeOpenOrder("order-1", "BTC/USD");
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CLOSED));
        verify(orderTriggeredProducer).send(any());   // vérifie que l'événement est émis
        verify(balanceUpdateProducer).send(any());
    }
}