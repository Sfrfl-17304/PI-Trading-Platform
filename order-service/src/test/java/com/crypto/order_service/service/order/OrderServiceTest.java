package com.crypto.order_service.service.order;

import com.crypto.order_service.client.UserServiceClient;
import com.crypto.order_service.exception.InsufficientBalanceException;
import com.crypto.order_service.model.dto.CreateOrderRequest;
import com.crypto.order_service.model.dto.OrderResponse;
import com.crypto.order_service.model.dto.UserProfileResponse;
import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderType;
import com.crypto.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderCacheService orderCacheService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validRequest;
    private UserProfileResponse userProfile;

    @BeforeEach
    void setUp() {
        validRequest = new CreateOrderRequest();
        validRequest.setSymbol("BTC/USD");
        validRequest.setType(OrderType.BUY);
        validRequest.setQuantity(new BigDecimal("0.01"));
        validRequest.setPrice(new BigDecimal("45000"));
        validRequest.setStopLoss(new BigDecimal("44000"));
        validRequest.setTakeProfit(new BigDecimal("46000"));

        userProfile = new UserProfileResponse();
        userProfile.setId(1L);
        userProfile.setBalance(new BigDecimal("50000")); // solde suffisant
    }

    @Test
    void shouldCreateOrderWhenBalanceSufficient() {
        // given
        when(userServiceClient.getUserProfile(1L)).thenReturn(userProfile);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId("order-123");
            return o;
        });

        // when
        OrderResponse response = orderService.createOrder(1L, validRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSymbol()).isEqualTo("BTC/USD");
        assertThat(response.getStatus()).isEqualTo(com.crypto.order_service.model.enums.OrderStatus.OPEN);
        verify(orderCacheService).cacheOpenOrder(any(Order.class));
    }

    @Test
    void shouldThrowWhenBalanceInsufficient() {
        // given
        userProfile.setBalance(new BigDecimal("100")); // trop peu
        when(userServiceClient.getUserProfile(1L)).thenReturn(userProfile);

        // when / then
        assertThatThrownBy(() -> orderService.createOrder(1L, validRequest))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Solde insuffisant");
        verify(orderRepository, never()).save(any());
    }
}