# 📦 Product Catalog Service (prod-cat-service)

## 📌 Overview
Handles product management, inventory updates, search indexing, and audit logging. Supports Elasticsearch and Redis.

---

## ⚙️ Features

- ✅ Product CRUD (SQL + Elasticsearch)
- ✅ Dynamic search with filters (price/category/full-text)
- ✅ Inventory update and rollback APIs
- ✅ Inventory audit logging with filters
- ✅ Retry queue for failed ES writes
- ✅ Redis caching for product fetches
- ✅ Prometheus + Actuator for metrics
- ✅ Rate limiting via Bucket4j
- ✅ SendGrid email alerts for low stock
- ✅ CI via GitHub Actions
- ✅ Docker-ready

---

## 🧰 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security + OAuth2
- Elasticsearch 8.17.3
- Redis + Spring Cache
- SendGrid Email API
- Micrometer + Prometheus
- Spring JPA + MySQL
- Swagger/OpenAPI
- Docker + GitHub Actions

---

## 📂 Key APIs

| Method | Path                         | Description                 |
|--------|------------------------------|-----------------------------|
| GET    | `/products/{id}`             | Get product by ID           |
| POST   | `/products`                  | Create product              |
| PUT    | `/products/{id}`             | Update product              |
| DELETE | `/products/{id}`             | Delete product              |
| GET    | `/search`                    | Search with filters         |
| PUT    | `/internal/update-stock`     | Update stock (internal only)|
| POST   | `/internal/rollback-stock`   | Rollback stock after failure|
| GET    | `/admin/inventory-logs`      | Admin: view stock changes   |

---

## 📈 Monitoring & Metrics

- `product.index.retry.success`
- `product.index.retry.failure`
- `inventory.updated.total`
- `inventory.rollback.success`
- `inventory.rollback.failed`

---

## 🔍 Search Filters

Supports:
- query (text)
- minPrice / maxPrice
- category
- pagination + sorting

---

## 🔄 Retry Job

Indexes products from retry queue (MySQL) to Elasticsearch with exponential backoff.

---

## 🔐 Security

- JWT RBAC + introspection via Auth Service
- `@HasScope`, `@AdminOnly` annotations
- Redis TTL-based token validation

---

## 🐳 Docker

```bash
docker build -t prod-cat-service .
docker run -p 8082:8082 prod-cat-service
```

---

## 🧪 Future Enhancements

- Text highlighting in search results
- Elasticsearch aggregations (top categories, price range)
- Service Mesh with Zipkin or Jaeger
- Flyway for DB migrations

---

## 👨‍💻 Author

**Aayush Kumar** – [GitHub](https://github.com/Aayush20)