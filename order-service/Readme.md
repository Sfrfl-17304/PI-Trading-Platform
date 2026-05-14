# Order Service

Microservice de gestion des ordres de trading avec stop-loss / take-profit.  
Écoute les prix en temps réel via Kafka, déclenche les ordres et informe les autres services.

---

## Démarrage

```bash
docker-compose up -d order-service
```

Service accessible sur : `http://localhost:8082`

---

## Authentification

Tous les endpoints nécessitent un token JWT dans le header :

```http
Authorization: Bearer <token>
```

### Obtenir un token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"<user>","password":"<pwd>"}'
```

Copier la valeur du champ `accessToken`.

---

# Endpoints REST

## 1. Créer un ordre

### Endpoint

```http
POST /api/orders
```

### Headers

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### Body JSON

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

### Paramètres

| Champ        | Type   | Obligatoire | Description                           |
|--------------|--------|--------------|---------------------------------------|
| symbol       | string | oui          | Paire de trading (ex: BTCUSDT)        |
| type         | string | oui          | BUY ou SELL                           |
| quantity     | number | oui          | Quantité de crypto                    |
| price        | number | non          | Prix d’entrée                         |
| stopLoss     | number | non          | Seuil de stop-loss                    |
| takeProfit   | number | non          | Seuil de take-profit                  |

### Exemple curl

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"BTCUSDT","type":"BUY","quantity":0.01,"price":80000,"stopLoss":79500,"takeProfit":81000}'
```

### Réponse

```http
201 Created
```

---

## 2. Lister les ordres

### Endpoint

```http
GET /api/orders?status=OPEN
```

### Paramètre optionnel

- `status` : `OPEN`, `CLOSED`, `CANCELLED`

### Exemple curl

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8082/api/orders?status=OPEN
```

### Réponse

```http
200 OK
```

Retourne un tableau d’ordres.

---

## 3. Annuler un ordre

### Endpoint

```http
DELETE /api/orders/{orderId}
```

### Exemple curl

```bash
curl -X DELETE \
  -H "Authorization: Bearer <token>" \
  http://localhost:8082/api/orders/<orderId>
```

### Réponse

```http
204 No Content
```

---

# Événements Kafka

## Topics consommés

- `raw-prices.crypto`
- `raw-prices.indices`

### Format `PriceTick`

```json
{
  "source": "BINANCE",
  "symbol": "BTCUSDT",
  "price": 80230.22,
  "timestamp": "2026-05-14T14:23:30.013Z"
}
```

---

## Topics produits

### `order-triggers`

Utilisé par Notification Service / Frontend.

```json
{
  "orderId": "90c80587-...",
  "userId": 1,
  "symbol": "BTCUSDT",
  "type": "BUY",
  "quantity": 0.01,
  "executionPrice": 80230.22,
  "triggerType": "TAKE_PROFIT",
  "timestamp": "2026-05-14T14:23:30.014Z"
}
```

---

### `balance-update`

Utilisé par User Service.

```json
{
  "userId": 1,
  "amount": 530.22,
  "reason": "order_execution",
  "referenceId": "90c80587-...",
  "timestamp": "2026-05-14T14:23:30.015Z"
}
```

---

# Configuration

| Variable d’environnement      | Valeur par défaut                         |
|--------------------------------|-------------------------------------------|
| DB_URL                         | jdbc:postgresql://localhost:5432/crypto  |
| DB_USERNAME                    | user                                      |
| DB_PASSWORD                    | password                                  |
| KAFKA_BOOTSTRAP_SERVERS        | localhost:9092                            |
| REDIS_HOST                     | localhost                                 |
| REDIS_PORT                     | 6379                                      |
| USER_SERVICE_URL               | http://localhost:8081                     |
| JWT_SECRET                     | partagé avec User Service                 |
| SERVER_PORT                    | 8082                                      |

---

# Test rapide

1. Créer un ordre avec un take-profit très proche du prix actuel.
2. Observer les logs :

```bash
docker-compose logs -f order-service
```

3. Dès qu’un tick franchit le seuil, un message :

```text
Ordre ... déclenché
```

apparaît.

4. Vérifier en base :

```bash
docker-compose exec postgres psql -U user -d crypto \
  -c "SELECT id,status,closed_at FROM orders;"
```