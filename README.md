# Microservices

**Gateway** is the single entry point for the Cafetiria distributed system. It routes incoming requests to the appropriate downstream services and propagates the `X-Correlation-Id` header across all calls.

---

## Architecture

src/main/java/com/microservices/margo/gateway/
* filter/ -> CorrelationIdFilter

Routes:
- `/orders/**` → Order Service (`http://order-service:8088`)
- `/users/**` → User Service (`http://user-service:8080`)

---

## Routing Table

| Incoming Request          | Forwarded To                          |
|---------------------------|---------------------------------------|
| `POST /orders`            | `POST http://order-service:8088/api/orders`          |
| `GET /orders/{id}`        | `GET http://order-service:8088/api/orders/{id}`      |
| `GET /health/orders`        | `GET http://order-service:8088/api/health`      |
| `PATCH /orders/{id}/status` | `PATCH http://order-service:8088/api/orders/{id}/status` |
| `POST /users`             | `POST http://user-service:8080/api/users`            |
| `GET /users/{id}`         | `GET http://user-service:8080/api/users/{id}`        |
| `GET /health/users`         | `GET http://user-service:8080/api/health`        |

---

## How to Run Locally

**Prerequisites:** Java 21+, Order Service and User Service running.

1. Set environment variables:

```bash
export ORDER_SERVICE_URL=http://localhost:8088
export USER_SERVICE_URL=http://localhost:8080
```

2. Run the application:

```bash
./mvnw spring-boot:run
```

The Gateway will be available at `http://localhost:8081`.

---

## How to Run with Docker

**Prerequisites:** Docker and Docker Compose installed.

1. Create a `.env` file in the project root:

```env
DB_USER=postgres
DB_PASSWORD=postgres
```

2. Start all services:

```bash
docker compose up --build
```

3. To stop and remove containers:

```bash
docker compose down
```

4. To stop and also remove volumes:

```bash
docker compose down -v
```

All services will be available via the gateway at `http://localhost:8081`.

---

## Health Check

```bash
curl http://localhost:8081/actuator/health
```
health of microservices is also accessible via gateway;
```bash
curl http://localhost:8088/orders/health
curl http://localhost:8081/users/health
```

**Response `200 UP`:**

```json
{
  "status": "UP"
}
```

---

## API Curl Examples

All requests go through the gateway on port `8081`.

### Create a user

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Marharyta",
    "surname": "Kovalenko",
    "phone": "+380991234567",
    "birthDate": "2000-01-15",
    "email": "marharyta@example.com"
  }'
```

**Response `201 Created`:**

```json
{
  "id": "b3f1c2d4-e5a6-7890-bcde-f12345678901",
  "name": "Marharyta",
  "surname": "Kovalenko",
  "phone": "+380991234567",
  "birthDate": "2000-01-15",
  "email": "marharyta@example.com",
  "createdAt": "2024-03-01T10:00:00"
}
```

---

### Get a user by ID

```bash
curl http://localhost:8081/users/b3f1c2d4-e5a6-7890-bcde-f12345678901
```

---

### Create an order

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId": "b3f1c2d4-e5a6-7890-bcde-f12345678901",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

**Response `201 Created`:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "ownerUserId": "b3f1c2d4-e5a6-7890-bcde-f12345678901",
  "itemName": "Latte",
  "quantity": 2,
  "price": 5.99,
  "status": "PENDING",
  "createdAt": "2024-03-01T10:00:00"
}
```

---

### Get an order by ID

```bash
curl http://localhost:8081/orders/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

### Update order status

```bash
curl -X PATCH http://localhost:8081/orders/a1b2c3d4-e5f6-7890-abcd-ef1234567890/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus": "CONFIRMED"}'
```

**Response `204 No Content`**

---

## Correlation ID

Every request gets an `X-Correlation-Id` header. If the client does not provide one, the gateway generates it automatically and propagates it to all downstream services.

**Provide your own:**

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: my-custom-id-123" \
  -d '{
    "ownerUserId": "b3f1c2d4-e5a6-7890-bcde-f12345678901",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

The same `X-Correlation-Id` will appear in the response headers and in the logs of every service that handled the request.

---

## Expected Behavior When User Service Is Down

When User Service is unreachable, any request that requires user validation (e.g. `POST /orders`) will fail gracefully:

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "ownerUserId": "b3f1c2d4-e5a6-7890-bcde-f12345678901",
    "itemName": "Latte",
    "quantity": 2,
    "price": 5.99
  }'
```

**Response `503 Service Unavailable`:**

```json
{"message": "Users service is unavailable"}
```

Requests that do not depend on User Service (e.g. `GET /orders/{id}`, `PATCH /orders/{id}/status`) will continue to work normally.
