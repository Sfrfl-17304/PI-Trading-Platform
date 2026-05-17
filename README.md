# PI Trading Platform

Welcome to the PI Trading Platform backend documentation! This document outlines the architecture, the purpose of each microservice, and provides all the endpoints and WebSocket channels required for the **Frontend Developer** to successfully integrate with the platform.

---

## 🏗 System Architecture & Running the Platform

This platform uses a robust Microservices Architecture glued together by **Kafka**, **PostgreSQL**, **Redis**, and **InfluxDB**.

### Requirements
- Docker & Docker Compose
- Java 21 (optional if only running via Docker)

### Startup
To start the entire trading platform seamlessly, run:
```bash
docker-compose up -d --build
```
This command builds and stages databases, streaming platforms, and all internal microservices.

---

## 🛠 Services Overview

### 1. User Service (Port `8081`)
Manages authentication, profiles, and wallet balances. 

**Endpoints**:
*   `POST /api/auth/login`: Issue JSON payload with `{"username":"<user>","password":"<pwd>"}` to receive the `accessToken` (JWT).  *(All secure endpoints require `Authorization: Bearer <accessToken>` headers)*.
*   *Kafka Topic Consumption*: Listens to `balance-update` (emitted by Order Service) to credit/debit wallets automatically on trade execution.

### 2. Order Service (Port `8082`)
Your main engine. It manages trading orders, executes stop-losses/take-profits, and interacts with market ticks in real-time.

**Endpoints**:
*   `POST /api/orders`: Create an order.
    ```json
    {
      "symbol": "BTCUSDT",
      "type": "BUY",
      "quantity": 0.01,
      "price": 80000,
      "stopLoss": 79500,
      "takeProfit": 81000
    }
    ```
*   `GET /api/orders?status=OPEN`: List orders.
*   `DELETE /api/orders/{orderId}`: Cancel an order.

**Kafka Triggers:**
*   Emits to `order-triggers` (pushes direct updates to WebSocket).
*   Emits to `balance-update` (tells User Service to deduct/add cash).

### 3. Market Data Service (Port `8083`)
Connects to external crypto APIs to pull real-time data ticks.
*   Pushes real-time ticks to Kafka `raw-prices.crypto` to feed the historical databases and the order engine.

### 4. Notification Service (Port `8084`) - ⭐ **WebSockets!**
The gateway for real-time frontend updates. Ensure your UI connects to `ws://localhost:8084/ws` over STOMP/SockJS.

**WebSocket Subscriptions:**
*   `/topic/admin-alerts`: User registration alerts.
*   `/topic/predictions`: Live AI trading signals pushed by the Prediction Service.
*   `/queue/orders`: Real-time order execution updates tailored to the logged-in user.

### 5. Prediction Service (Port `8085`) & Python API (Port `5000`)
Hosts the AI and Consensus Algorithms (EMA, RSI, ARIMA). **As a frontend developer, you can use two distinct strategies here to get model predictions:**

#### Strategy A: Predict Using Internal Historical Data
Ask the AI to autonomously poll the last 12 hours of data from the Historical Service and crunch the prediction.
*   **Endpoint**: `GET /api/v1/predict/{symbol}`  
    *Example: `GET /api/v1/predict/BTCUSDT`*

#### Strategy B: Predict Using Custom Frontend Data
Provide your own localized array of prices (e.g., from a custom graph timeframe you are viewing on the frontend).
*   **Endpoint**: `POST /api/v1/predict/{symbol}`
    *Body:* `[ 79000, 79200, 79550, ... ]`

*Note: Calling either of these endpoints returns the `{"action":"BUY|SELL|HOLD", "confidence": 0.XX}` JSON immediately AND broadcasts it to the WebSocket (`/topic/predictions`)!*

### 6. Historical Service (Port `8087`)
The Time-Series database wrapper (powered by InfluxDB). Perfect for painting Candlestick charts on the UI.

**Endpoint**:
*   `GET /api/historical/prices?symbol=BTCUSDT&start=2026-05-14T00:00:00Z&end=2026-05-15T00:00:00Z&interval=1m`

*Returns:*
```json
{
  "symbol": "BTCUSDT",
  "data": [
    { "timestamp": 1778763060000, "price": 79735.54 }
  ]
}
```

---

## 🔄 Quick Flow Summary (Example Trade Loop)
1. Frontend calls `Historical Service` to draw the BTC Graph.
2. User clicks "Predict Trend". Frontend calls `Prediction Service` `GET /api/v1/predict/BTCUSDT`.
3. Prediction service asks Historical Service for context, runs ARIMA + EMA + RSI, and replies with `"BUY"`.
4. User clicks "Buy", Frontend POSTs to `Order Service`.
5. `Order Service` debits the user's wallet via Kafka (`User Service`).
6. When price shifts and hits the `takeProfit`, `Order Service` executes and tells `Notification Service`.
7. `Notification Service` pushes a WebSocket alert to Frontend: *"Order closed successfully."*