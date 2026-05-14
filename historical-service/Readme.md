# Historical Service – Documentation API

---

# Base URL

| Environnement | URL |
|---|---|
| Développement local (machine hôte) | `http://localhost:8087` |
| Docker (frontend conteneurisé) | `http://historical-service:8087` |

---

# Endpoint unique

```http
GET /api/historical/prices
```

---

# Paramètres (Query String)

| Nom | Type | Requis | Défaut | Exemple |
|---|---|---|---|---|
| symbol | string | Oui | – | BTCUSDT |
| start | ISO 8601 (UTC) | Oui | – | 2026-05-14T00:00:00Z |
| end | ISO 8601 (UTC) | Oui | – | 2026-05-15T00:00:00Z |
| interval | string | Non | 1m | 1m, 5m, 15m, 1h, 1d |

---

# Exemple de requête

```bash
curl "http://localhost:8087/api/historical/prices?symbol=BTCUSDT&start=2026-05-14T00:00:00Z&end=2026-05-15T00:00:00Z&interval=1m"
```

---

# Réponse (succès – HTTP 200)

```json
{
  "symbol": "BTCUSDT",
  "data": [
    {
      "timestamp": 1778763060000,
      "price": 79735.54
    },
    {
      "timestamp": 1778763120000,
      "price": 79736.17
    }
  ]
}
```

---

# Description des champs

| Champ | Description |
|---|---|
| timestamp | Millisecondes depuis epoch (Unix time) |
| price | Prix moyen sur l’intervalle (double) |

Si aucune donnée n’existe pour la période demandée, `data` retourne un tableau vide :

```json
{
  "symbol": "BTCUSDT",
  "data": []
}
```

---

# Codes d’erreur

| Code HTTP | Signification |
|---|---|
| 400 | Paramètre invalide (date mal formatée, symbole manquant) |
| 503 | InfluxDB indisponible |
| 500 | Erreur interne |

---

# Débogage & Logs

Afficher les logs du conteneur :

```bash
docker logs historical-service
```

---

# Exemple complet

```bash
curl -X GET \
"http://localhost:8087/api/historical/prices?symbol=BTCUSDT&start=2026-05-14T00:00:00Z&end=2026-05-15T00:00:00Z&interval=5m"
```

---

# Notes

- Toutes les dates doivent être au format ISO 8601 UTC.
- Le service retourne des données agrégées selon l’intervalle demandé.
- Les timestamps sont toujours exprimés en millisecondes Unix.
- Les symboles doivent correspondre aux symboles stockés dans InfluxDB.

---

# Besoin d’aide ?

Contacte le développeur du Historical Service.