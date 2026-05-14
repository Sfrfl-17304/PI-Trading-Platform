package com.crypto.order_service.service.order;

import com.crypto.order_service.client.UserServiceClient;
import com.crypto.order_service.exception.InsufficientBalanceException;
import com.crypto.order_service.exception.InvalidOrderStateException;
import com.crypto.order_service.exception.OrderNotFoundException;
import com.crypto.order_service.model.dto.CreateOrderRequest;
import com.crypto.order_service.model.dto.OrderResponse;
import com.crypto.order_service.model.entity.Order;
import com.crypto.order_service.model.enums.OrderStatus;
import com.crypto.order_service.model.enums.OrderType;
import com.crypto.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderCacheService orderCacheService;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. Validation métier
        if (request.getType() == OrderType.BUY) {
            if (request.getStopLoss() != null && request.getPrice() != null
                    && request.getStopLoss().compareTo(request.getPrice()) >= 0) {
                throw new IllegalArgumentException("Stop-loss doit être inférieur au prix pour un achat");
            }
            if (request.getTakeProfit() != null && request.getPrice() != null
                    && request.getTakeProfit().compareTo(request.getPrice()) <= 0) {
                throw new IllegalArgumentException("Take-profit doit être supérieur au prix pour un achat");
            }
        } else { // SELL
            if (request.getStopLoss() != null && request.getPrice() != null
                    && request.getStopLoss().compareTo(request.getPrice()) <= 0) {
                throw new IllegalArgumentException("Stop-loss doit être supérieur au prix pour une vente");
            }
            if (request.getTakeProfit() != null && request.getPrice() != null
                    && request.getTakeProfit().compareTo(request.getPrice()) >= 0) {
                throw new IllegalArgumentException("Take-profit doit être inférieur au prix pour une vente");
            }
        }

        // 2. Vérifier le solde
         BigDecimal requiredAmount = request.getPrice() != null
                ? request.getPrice().multiply(request.getQuantity())
                : BigDecimal.ZERO; // si pas de prix, pas de vérification (ordre conditionnel pur)
        if (requiredAmount.compareTo(BigDecimal.ZERO) > 0) {
            var userProfile = userServiceClient.getUserProfile(userId);
            if (userProfile.getBalance().compareTo(requiredAmount) < 0) {
                throw new InsufficientBalanceException("Solde insuffisant");
            }
        }

        // 3. Création de l'ordre
        Order order = Order.builder()
                .userId(userId)
                .symbol(request.getSymbol())
                .type(request.getType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .stopLoss(request.getStopLoss())
                .takeProfit(request.getTakeProfit())
                .status(OrderStatus.OPEN)
                .build();
        order = orderRepository.save(order);
        orderCacheService.cacheOpenOrder(order);

        return OrderResponse.from(order);
    }

    public List<OrderResponse> getUserOrders(Long userId, OrderStatus status) {
        List<Order> orders = (status != null)
                ? orderRepository.findByUserIdAndStatus(userId, status)
                : orderRepository.findByUserId(userId); // à créer si besoin
        return orders.stream().map(OrderResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Ordre introuvable"));
        if (!order.getUserId().equals(userId)) {
            throw new InvalidOrderStateException("Cet ordre ne vous appartient pas");
        }
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new InvalidOrderStateException("L'ordre n'est pas ouvert");
        }
        orderCacheService.removeOpenOrder(orderId, order.getSymbol());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}