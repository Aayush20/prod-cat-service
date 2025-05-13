# 🛒 Product Catalog Service (prod-cat-service)

This microservice is part of a larger **E-Commerce Platform** and handles all product-related operations. It supports product creation, search, stock updates, inventory audit logs, Elasticsearch indexing, caching, retry logic, and more.

---

## 🚀 Features

- ✅ Product CRUD (Create, Read, Update, Delete)
- ✅ Elasticsearch-based dynamic search (full-text, category, price)
- ✅ Inventory stock update and rollback APIs
- ✅ Inventory audit log with filters and pagination (admin)
- ✅ Retry mechanism for failed Elasticsearch indexing
- ✅ Redis-based caching (per-product, featured list)
- ✅ Prometheus monitoring and metrics
- ✅ JWT-based security with role/claim-based access
- ✅ Structured logs with requestId (MDC)
- ✅ Rate limiting on internal endpoints (Bucket4j)
- ✅ Actuator endpoints (`/actuator/*`)
- ✅ Swagger/OpenAPI docs with examples
- ✅ CI GitHub Workflow for Maven build + test + Docker build
- ✅ Dockerfile for containerized deployment

---

## ⚙️ Running the Service Locally

### 1. Prerequisites

- Java 17
- Maven
- Docker (for Redis, Elasticsearch, MySQL)

### 2. Start Dependencies

```bash
# Start MySQL
docker run -d -p 3306:3306 --name mysql   -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=ecom mysql:8

# Start Redis
docker run -d -p 6379:6379 --name redis redis

# Start Elasticsearch (v8.17.3)
docker run -d -p 9200:9200 -e "discovery.type=single-node"   -e "xpack.security.enabled=false"   docker.elastic.co/elasticsearch/elasticsearch:8.17.3
```

### 3. Run App

```bash
# Build & run
mvn clean install
java -jar target/*.jar
```

---

## 🔐 Security

- JWT token verification with issuer URI
- Role checks (`admin`, `order-service`, etc.)
- Internal endpoints protected with `ROLE_INTERNAL` or `SCOPE_internal`

---

## 📦 Docker Build

```bash
docker build -t yourname/prod-cat-service .
```

---

## 🧪 Test Coverage

Includes:
- Unit tests (ProductService, Elasticsearch logic)
- Integration tests (Controller + Repository)
- Cache behavior tests (cache hit/miss/evict)

```bash
mvn test
```

---

## 📈 Monitoring

- `GET /actuator/prometheus` – Metrics exposed
- Prometheus and Grafana ready
- Elasticsearch retry success/failure counters
- Redis cache keys viewable via `/admin/cache-stats`

---

## 📚 Swagger UI

Visit: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

Includes example responses and filterable APIs.

---

## 📊 Redis Cache Stats

```http
GET /admin/cache-stats
Authorization: Bearer <admin-token>
```

Returns all Redis keys, key count, and cache names.

---

## 🔍 Search API

```http
GET /search?query=phone&minPrice=100&maxPrice=1000&category=Mobile&page=0&size=10&sortBy=price&sortOrder=DESC
```

Filters supported: `query`, `minPrice`, `maxPrice`, `category`, pagination, sorting.

---

## 📅 Retry Job

Indexes products that failed to get indexed due to Elasticsearch errors. Tries 3 times with exponential backoff. Managed via scheduler.

---

## 🔄 Inventory Audit Log API

```http
GET /admin/inventory-logs?page=0&size=10&productId=123&updatedBy=order-service
```

Admin-only filterable logs of stock changes.

---

## 💡 Future Enhancements (To-Do Later)

| Feature                     | Description |
|----------------------------|-------------|
| 🔧 **Price filtering in Elasticsearch** | Currently commented; enable later |
| 🔧 **Text Highlighting in Search**      | Use `highlight` block in ES response |
| 🔧 **Aggregations**                     | Top categories, price range buckets |
| 🔧 **Role & Scope Enforcement (RBAC)**  | Implement in `auth-service` to unify all MS |
| 🔧 **Docker CI Push**                  | Push Docker image to registry (ECR/DockerHub) |
| 🔧 **Service Mesh/Zipkin/Jaeger**      | For distributed tracing across services |
| 🔧 **DB Migration Tool**               | Introduce Flyway or Liquibase |
| 🔧 **Index mapping tuning**            | Custom analyzers for better search accuracy |
| 🔧 **Add category CRUD APIs**          | If dynamic categories are needed later |

---

## ✅ Status

**Ready for integration.** Works with order-service, auth-service, payment-service.

No major core logic missing. Everything is structured for production deployment.