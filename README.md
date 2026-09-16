# StaySphere — Distributed Booking Platform

> A production-grade, microservices-based Airbnb-like platform built with Java 21 and Spring Boot, implementing concurrency-safe booking, event-driven architecture, dynamic pricing, and a smart waitlist system.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Microservices](#microservices)
- [Key Engineering Features](#key-engineering-features)
- [Database Design](#database-design)
- [Event Flow](#event-flow)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Services](#running-the-services)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [Engineering Decisions](#engineering-decisions)

---

## Overview

StaySphere is a distributed property booking platform built to demonstrate real-world backend engineering patterns including:

- Distributed locking for concurrency-safe bookings
- Event-driven communication between decoupled services
- Dynamic pricing with seasonal and demand multipliers
- Smart availability optimization using interval merging
- Idempotent payment processing
- Priority waitlist with automatic slot promotion

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Client Layer                      │
│            Web · Mobile · Admin Portal               │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│              API Gateway  (Port 8080)                │
│         JWT Validation · Routing · Rate Limit        │
└──┬──────┬──────┬──────┬──────┬──────┬──────┬────────┘
   │      │      │      │      │      │      │
   ▼      ▼      ▼      ▼      ▼      ▼      ▼
 Auth  Property Booking Payment Pricing Waitlist Notification
 8081   8082    8083    8084    8085    8086     8087
                                              Availability
                                                 8088
                        │
┌───────────────────────▼─────────────────────────────┐
│                  Event Bus (Kafka)                    │
│   booking-events · payment-events · waitlist-events  │
└─────────────────────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                   Data Layer                         │
│  PostgreSQL · Redis · S3/Cloudinary  │
└─────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| API Gateway | Spring Cloud Gateway |
| Database | PostgreSQL 16 |
| Cache / Locks | Redis 7 |
| Messaging | Apache Kafka |
| Search | PostgreSQL FTS (`tsvector` + `pg_trgm`) |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Email | SendGrid |
| Media | Cloudinary |
| Build | Gradle (multi-module) |
| Containerization | Docker + Docker Compose |

---

## Microservices

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | JWT validation, routing, rate limiting |
| auth-service | 8081 | Registration, login, JWT + refresh tokens |
| property-service | 8082 | Listings, search, images, **availability**, **pricing** (`/api/availability`, `/api/pricing`) |
| booking-service | 8083 | Bookings, Redis locking, **waitlist** (`/api/waitlist`) |
| payment-service | 8084 | Idempotent payment processing |
| notification-service | 8087 | Email via SendGrid, Kafka consumers |

---

## Key Engineering Features

### 1. Concurrency-Safe Booking with Redis Distributed Locks

When two users attempt to book the same property for overlapping dates simultaneously, only one should succeed. This is solved using Redis SETNX (Set if Not Exists):

```
User A ──► SETNX lock:booking:propertyId:checkIn:checkOut ──► Lock acquired (TTL 10 min)
User B ──► SETNX lock:booking:propertyId:checkIn:checkOut ──► Lock exists → BookingConflictException
```

- Lock key format: `lock:booking:{propertyId}:{checkIn}:{checkOut}`
- TTL: 10 minutes — auto-expires if service crashes
- Lock released on: booking confirmed, booking failed, booking cancelled
- `@Version` optimistic locking on Booking entity as second layer of protection

### 2. Event-Driven Architecture with Kafka

Services communicate asynchronously through Kafka topics. No service calls another service directly.

```
booking-service  ──► booking-events  ──► notification-service (email guest)
                                     ──► availability-service (block dates)
                                     ──► waitlist-service (on cancellation)

payment-service  ──► payment-events  ──► booking-service (auto-confirm), notification-service (payment receipt)

waitlist-service ──► waitlist-events ──► notification-service (slot available)
```

**Topics:**
- `booking-events` — booking_created, booking_confirmed, booking_cancelled
- `payment-events` — payment_success, payment_failed, payment_refunded
- `waitlist-events` — slot_offered

### 3. Smart Availability Optimizer (O(n log n) Interval Merge)

Instead of checking availability date by date, the system computes all available date ranges at once using an interval merge algorithm:

```
Input (blocked intervals):  [Apr 5–10], [Apr 8–15], [Apr 20–25]
After merge:                [Apr 5–15], [Apr 20–25]
Available gaps:             [Apr 1–5],  [Apr 15–20], [Apr 25–30]
```

**Algorithm steps:**
1. Sort blocked intervals by start date — O(n log n)
2. Merge overlapping intervals — O(n)
3. Find gaps between merged intervals — O(n)
4. Cache result in Redis, invalidate on new booking

### 4. Dynamic Pricing Engine

Price is calculated as:

```
Final Price = Base Price × Weekend Multiplier × Peak Season Multiplier × Long Stay Discount

Example:
Base price:           ₹2,500/night
Weekend multiplier:   ×1.3  (Friday/Saturday nights)
Peak season:          ×1.5  (Dec 15 – Jan 5)
Long stay discount:   ×0.9  (7+ nights)

Final price: ₹2,500 × 1.3 × 1.5 × 0.9 = ₹4,387/night
```

### 5. Idempotent Payment Processing

The same payment request sent multiple times only processes once:

```
Request 1: idempotencyKey=pay-booking-123 → Creates payment, returns SUCCESS
Request 2: idempotencyKey=pay-booking-123 → Finds existing, returns same SUCCESS
Request 3: idempotencyKey=pay-booking-123 → Finds existing, returns same SUCCESS
```

Database has a unique constraint on `idempotency_key` as a hard guarantee.

### 6. Priority Waitlist with TTL Auto-Promotion

When a booking is cancelled:

```
booking_cancelled event published
        ↓
waitlist-service consumes event
        ↓
First guest in queue → status: OFFERED, slot_expires_at: now + 30 min
        ↓
waitlist-events: slot_offered published
        ↓
notification-service sends email to guest
        ↓
If guest doesn't book in 30 min → @Scheduled job expires slot
        ↓
Next guest in queue gets the offer
```

---

## Database Design

### Tables

```sql
-- Auth Service
users (id, email, password, full_name, role, created_at)
refresh_tokens (id, user_id, token, expires_at, created_at)

-- Property Service
properties (id, host_id, title, description, city, country,
            address, latitude, longitude, price_per_night,
            max_guests, bedrooms, bathrooms, property_type,
            status, created_at)
property_amenities (property_id, amenity)
availability (id, property_id, start_date, end_date, status)

-- Booking Service
bookings (id, property_id, guest_id, host_id, check_in,
          check_out, total_guests, total_price, price_per_night,
          status, idempotency_key, version, confirmed_at,
          cancelled_at, created_at)

-- Payment Service
payments (id, booking_id, guest_id, host_id, idempotency_key,
          amount, currency, status, payment_method,
          transaction_id, failure_reason, version,
          processed_at, created_at)

-- Pricing Service
pricing_rules (id, property_id, base_price, weekend_multiplier,
               peak_season_multiplier, peak_season_start,
               peak_season_end, minimum_stay, long_stay_discount,
               long_stay_threshold_nights, status)

-- Waitlist Service
waitlist_entries (id, property_id, guest_id, check_in,
                  check_out, total_guests, queue_position,
                  status, slot_offered_at, slot_expires_at,
                  created_at)

-- Notification Service
notifications (id, recipient_id, recipient_email, subject,
               message, notification_type, status,
               reference_id, error_message, sent_at, created_at)

-- Availability Service
blocked_dates (id, property_id, start_date, end_date,
               reason, reference_id, created_at)
```

### Key Indexes

```sql
-- Fast conflict detection for booking
INDEX idx_booking_property ON bookings(property_id)
INDEX idx_booking_guest ON bookings(guest_id)

-- Fast availability lookup
INDEX idx_availability_property ON availability(property_id, start_date, end_date)

-- Idempotency fast lookup
INDEX idx_payment_idempotency ON payments(idempotency_key)

-- Waitlist queue ordering
INDEX idx_waitlist_property ON waitlist_entries(property_id, check_in, queue_position)
```

---

## Event Flow

### Complete Booking Flow (Happy Path)

```
1. Guest calls POST /api/bookings
        ↓
2. booking-service acquires Redis lock
        ↓
3. Conflict check against bookings table
        ↓
4. Booking saved with status PENDING
        ↓
5. booking_created event → Kafka
        ↓
6. notification-service sends "Booking received" email
        ↓
7. Guest calls POST /api/payments
        ↓
8. payment-service checks idempotency key
        ↓
9. Payment processed (Stripe/Razorpay)
        ↓
10. payment_success event → Kafka
        ↓
11. booking-service auto-confirms (payment_success consumer); notification-service sends "Payment successful" email
        ↓
12. Booking status → CONFIRMED (or guest may POST /api/bookings/{id}/confirm if event was missed)
        ↓
13. Redis lock released
        ↓
14. booking_confirmed event → Kafka
        ↓
15. availability-service blocks the dates
        ↓
16. notification-service sends "Booking confirmed" to guest + host
```

### Cancellation Flow

```
1. Guest calls POST /api/bookings/{id}/cancel
        ↓
2. Booking status → CANCELLED
        ↓
3. booking_cancelled event → Kafka
        ↓
4. availability-service unblocks the dates
        ↓
5. waitlist-service checks if anyone is waiting
        ↓
6. First in queue gets slot_offered (30 min TTL)
        ↓
7. notification-service sends "Slot available!" email
```

---

## Prerequisites

Make sure you have these installed:

```bash
java -version      # Java 21+
docker -v          # Docker 24+
docker compose version  # Docker Compose v2+
gradle -v          # Gradle 8+ (or use wrapper ./gradlew)
```

---

## Getting Started

### Step 1 — Clone the repository

```bash
git clone https://github.com/sachin3059/stay-sphere.git
cd stay-sphere
```

### Step 2 — Start infrastructure with Docker Compose

```bash
docker compose up -d
```

This starts:
- PostgreSQL on port 5432
- Redis on port 6379
- Apache Kafka on port 9092
- Zookeeper on port 2181

Verify all containers are running:

```bash
docker compose ps
```

Expected output:
```
staysphere-postgres        Running
staysphere-redis           Running
staysphere-zookeeper       Running
staysphere-kafka           Running
```

### Step 3 — Configure environment variables

Each service reads from its own `application.yml`. The default values work out of the box for local development.

For SendGrid and Cloudinary, update these files:

**notification-service/src/main/resources/application.yml**
```yaml
sendgrid:
  api-key: YOUR_SENDGRID_API_KEY
  from-email: your-verified-email@example.com
  from-name: StaySphere
```

**property-service/src/main/resources/application.yml**
```yaml
cloudinary:
  cloud-name: YOUR_CLOUD_NAME
  api-key: YOUR_API_KEY
  api-secret: YOUR_API_SECRET
```

---

## Running the Services

Open a separate terminal for each service or use IntelliJ's run configurations.

### Option A — Run individually (recommended for development)

```bash
# Terminal 1 — Auth Service
./gradlew :auth-service:bootRun

# Terminal 2 — Property Service
./gradlew :property-service:bootRun

# Terminal 3 — Booking Service
./gradlew :booking-service:bootRun

# Terminal 4 — Payment Service
./gradlew :payment-service:bootRun

# Terminal 5 — Pricing Service
./gradlew :pricing-service:bootRun

# Terminal 6 — Waitlist Service
./gradlew :waitlist-service:bootRun

# Terminal 7 — Notification Service
./gradlew :notification-service:bootRun

# Terminal 8 — Availability Service
./gradlew :availability-service:bootRun

# Terminal 9 — API Gateway (start last)
./gradlew :api-gateway:bootRun
```

### Option B — Build all services

```bash
./gradlew build -x test
```

### Verify services are running

```bash
curl http://localhost:8081/api/auth/health
curl http://localhost:8082/api/properties/health
curl http://localhost:8083/api/bookings/health
curl http://localhost:8084/api/payments/health
curl http://localhost:8085/api/pricing/health
curl http://localhost:8086/api/waitlist/health
curl http://localhost:8087/api/notifications/health
curl http://localhost:8088/api/availability/health
```

All should return `{service-name} service is running`.

---

## API Reference

### Auth Service — Port 8081

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login, returns JWT |
| POST | `/api/auth/refresh` | None | Refresh JWT token |
| GET | `/api/auth/health` | None | Health check |

**Register request:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "role": "GUEST"
}
```

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "user@example.com",
  "role": "GUEST",
  "fullName": "John Doe"
}
```

---

### Property Service — Port 8082

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/properties` | JWT | Create listing |
| GET | `/api/properties` | None | Get all listings |
| GET | `/api/properties/{id}` | None | Get listing by ID |
| GET | `/api/properties/host` | JWT | Get host's listings |
| GET | `/api/properties/search` | None | Search by city/price/guests |
| POST | `/api/properties/{id}/images` | JWT | Upload images |

**Search query params:**
```
/api/properties/search?city=Pune&guests=2&minPrice=1000&maxPrice=5000
```

**Create property request:**
```json
{
  "title": "Cozy Apartment in Pune",
  "description": "Beautiful apartment in Koregaon Park",
  "city": "Pune",
  "country": "India",
  "address": "Lane 5, Koregaon Park, Pune 411001",
  "latitude": 18.5362,
  "longitude": 73.8938,
  "pricePerNight": 2500.00,
  "maxGuests": 4,
  "bedrooms": 2,
  "bathrooms": 1,
  "propertyType": "APARTMENT",
  "amenities": ["WiFi", "AC", "Kitchen", "Parking"]
}
```

---

### Booking Service — Port 8083

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/bookings` | JWT | Create booking |
| POST | `/api/bookings/{id}/confirm` | JWT | Confirm booking |
| POST | `/api/bookings/{id}/cancel` | JWT | Cancel booking |
| GET | `/api/bookings/{id}` | JWT | Get booking by ID |
| GET | `/api/bookings/my` | JWT | Get my bookings |
| GET | `/api/bookings/property/{propertyId}` | JWT | Get property bookings |

**Create booking request:**
```json
{
  "propertyId": "962b20d6-8b68-44e1-b288-536f764a99ce",
  "hostId": "host@example.com",
  "checkIn": "2026-05-01",
  "checkOut": "2026-05-05",
  "totalGuests": 2,
  "pricePerNight": 2500.00
}
```

**Booking response:**
```json
{
  "id": "8d0a4e8f-bba1-4933...",
  "propertyId": "962b20d6...",
  "guestId": "guest@example.com",
  "checkIn": "2026-05-01",
  "checkOut": "2026-05-05",
  "totalPrice": 10000.00,
  "status": "PENDING",
  "idempotencyKey": "uuid-here"
}
```

---

### Payment Service — Port 8084

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/payments` | JWT | Process payment |
| POST | `/api/payments/{id}/refund` | JWT | Refund payment |
| GET | `/api/payments/{id}` | JWT | Get payment by ID |
| GET | `/api/payments/booking/{bookingId}` | None | Get payment by booking |

**Payment request:**
```json
{
  "bookingId": "8d0a4e8f-bba1-4933...",
  "hostId": "host@example.com",
  "idempotencyKey": "pay-booking-8d0a4e8f",
  "amount": 10000.00,
  "currency": "INR",
  "paymentMethod": "UPI"
}
```

**Supported payment methods:** `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `NET_BANKING`, `WALLET`

---

### Pricing Service — Port 8085

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/pricing/rules` | JWT | Create pricing rule |
| GET | `/api/pricing/rules/{propertyId}` | None | Get rule for property |
| POST | `/api/pricing/calculate` | None | Calculate price for dates |

**Create pricing rule:**
```json
{
  "propertyId": "962b20d6...",
  "basePrice": 2500.00,
  "weekendMultiplier": 1.3,
  "peakSeasonMultiplier": 1.5,
  "peakSeasonStart": "2026-12-15",
  "peakSeasonEnd": "2027-01-05",
  "minimumStay": 2,
  "longStayDiscount": 0.9,
  "longStayThresholdNights": 7
}
```

**Calculate price request:**
```json
{
  "propertyId": "962b20d6...",
  "checkIn": "2026-12-20",
  "checkOut": "2026-12-27"
}
```

**Calculate price response:**
```json
{
  "propertyId": "962b20d6...",
  "basePrice": 2500.00,
  "finalPricePerNight": 4387.50,
  "totalPrice": 30712.50,
  "totalNights": 7,
  "appliedRules": [
    "Weekend surcharge: ×1.3",
    "Peak season surcharge: ×1.5",
    "Long stay discount: ×0.9"
  ]
}
```

---

### Waitlist Service — Port 8086

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/waitlist` | JWT | Join waitlist |
| DELETE | `/api/waitlist/{id}` | JWT | Leave waitlist |
| GET | `/api/waitlist/my` | JWT | Get my waitlist entries |
| GET | `/api/waitlist/property/{id}` | None | Get property waitlist |

**Join waitlist request:**
```json
{
  "propertyId": "962b20d6...",
  "checkIn": "2026-05-01",
  "checkOut": "2026-05-05",
  "totalGuests": 2
}
```

---

### Availability Service — Port 8088

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/availability/{propertyId}` | None | Get available ranges |
| GET | `/api/availability/{propertyId}/check` | None | Check specific dates |
| POST | `/api/availability/block` | JWT | Host blocks dates |
| GET | `/api/availability/{propertyId}/blocked` | None | Get blocked dates |

**Get available ranges:**
```
/api/availability/{propertyId}?from=2026-04-01&to=2026-04-30
```

**Response:**
```json
[
  { "from": "2026-04-01", "to": "2026-04-10", "nights": 9 },
  { "from": "2026-04-15", "to": "2026-04-20", "nights": 5 },
  { "from": "2026-04-25", "to": "2026-04-30", "nights": 5 }
]
```

---

## Project Structure

```
stay-sphere/
├── api-gateway/
│   └── src/main/java/com/staysphere/gateway/
│       ├── config/          # JWT config
│       ├── filter/          # JWT authentication filter
│       └── controller/      # Fallback controller
├── auth-service/
│   └── src/main/java/com/staysphere/auth/
│       ├── entity/          # User, RefreshToken
│       ├── repository/      # UserRepository
│       ├── dto/             # Request/Response DTOs
│       ├── security/        # JWT filter, config
│       ├── service/         # AuthService, RefreshTokenService
│       └── controller/      # AuthController
├── property-service/
│   └── src/main/java/com/staysphere/property/
│       ├── entity/          # Property, Availability, Pricing entities
│       ├── repository/      # JPA + Postgres FTS search
│       ├── service/         # PropertyService, CloudinaryService
│       └── controller/      # PropertyController
├── booking-service/
│   └── src/main/java/com/staysphere/booking/
│       ├── entity/          # Booking (@Version optimistic lock)
│       ├── service/         # BookingService, RedisLockService
│       └── controller/      # BookingController
├── payment-service/
│   └── src/main/java/com/staysphere/payment/
│       ├── entity/          # Payment (idempotency key, @Version)
│       ├── service/         # PaymentService (idempotency check)
│       └── controller/      # PaymentController
├── pricing-service/
│   └── src/main/java/com/staysphere/pricing/
│       ├── entity/          # PricingRule
│       ├── service/         # PricingService (dynamic engine)
│       └── controller/      # PricingController
├── waitlist-service/
│   └── src/main/java/com/staysphere/waitlist/
│       ├── entity/          # WaitlistEntry
│       ├── service/         # WaitlistService (@KafkaListener, @Scheduled)
│       └── controller/      # WaitlistController
├── notification-service/
│   └── src/main/java/com/staysphere/notification/
│       ├── entity/          # Notification
│       ├── service/         # NotificationService (@KafkaListener), EmailService
│       └── controller/      # NotificationController
├── availability-service/
│   └── src/main/java/com/staysphere/availability/
│       ├── entity/          # BlockedDate
│       ├── service/         # AvailabilityService (interval merge, @KafkaListener)
│       └── controller/      # AvailabilityController
├── common/
│   └── src/main/java/com/staysphere/common/
│       ├── ApiResponse.java
│       ├── ErrorResponse.java
│       └── GlobalExceptionHandler.java
├── docker-compose.yml
├── build.gradle              # Root build — shared dependencies
└── settings.gradle           # Module declarations
```

---

## Engineering Decisions

### Why Redis for distributed locking instead of database locks?

Database-level locks (SELECT FOR UPDATE) hold a connection for the entire lock duration, limiting throughput. Redis SETNX is a single atomic O(1) operation with automatic TTL expiry — no connection held, no deadlock risk, horizontally scalable. If the service crashes, the TTL ensures the lock auto-releases after 10 minutes.

### Why Kafka instead of synchronous REST calls between services?

Synchronous calls create tight coupling — if notification-service is down, booking confirmation fails. With Kafka, booking-service publishes an event and returns immediately. Notification-service, availability-service, and waitlist-service process the event independently at their own pace. This gives fault isolation, independent scalability, and natural retry mechanisms.

### Why idempotency keys for payments?

Network failures can cause payment requests to be retried. Without idempotency keys, a guest could be charged twice for the same booking if the first request times out but actually succeeded. The UUID idempotency key with a unique database constraint guarantees the same request is only processed once, regardless of how many times it's sent.

### Why interval merge for availability instead of per-date queries?

Querying availability date by date for a 30-day window would require 30 database queries. The interval merge algorithm computes all available ranges in a single query + O(n log n) in-memory computation, then caches the result in Redis. Subsequent requests hit the cache until a new booking invalidates it.

### Why separate availability-service instead of using property-service's availability table?

Each service owns its data. Property-service manages listing metadata. Availability-service manages the calendar state — which dates are blocked, why, and by which booking. This separation means availability logic (interval merge, Redis caching, Kafka consumption) can evolve independently without touching property management code.

---

## Environment Variables Reference

| Variable | Service | Description |
|---|---|---|
| `spring.datasource.url` | All | PostgreSQL connection URL |
| `spring.datasource.username` | All | DB username (default: staysphere) |
| `spring.datasource.password` | All | DB password (default: staysphere123) |
| `spring.data.redis.host` | Booking, Availability | Redis host (default: localhost) |
| `spring.kafka.bootstrap-servers` | Booking, Payment, Waitlist, Notification, Availability | Kafka broker |
| `jwt.secret` | All | 256-bit hex JWT signing key |
| `jwt.expiration` | All | Token expiry in ms (default: 86400000 = 24h) |
| `sendgrid.api-key` | Notification | SendGrid API key |
| `sendgrid.from-email` | Notification | Verified sender email |
| `cloudinary.cloud-name` | Property | Cloudinary cloud name |
| `cloudinary.api-key` | Property | Cloudinary API key |
| `cloudinary.api-secret` | Property | Cloudinary API secret |

---

## License

MIT License — free to use, modify, and distribute.

---

*Built with Java 21, Spring Boot 3.3.5, and a lot of distributed systems thinking.*