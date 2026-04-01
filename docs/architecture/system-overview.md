# Архитектурный обзор системы

## Назначение

Quickmart - монорепозиторий MVP-сервиса быстрой доставки продуктов и товаров для дома. Система разделена на runtime-модули и automation-модули, чтобы бизнес-код, инфраструктура тестов и CI-контракты развивались независимо и не смешивались между собой.

## Границы модулей

| Модуль | Путь | Ответственность |
| --- | --- | --- |
| `:backend` | `app/backend` | Spring Boot backend, бизнес-логика, persistence, security, Swagger/OpenAPI, cache, optional Kafka, outbound HTTP integrations |
| `:api-tests` | `tests/backend` | black-box API automation и backend component suites (`cache`, `kafka`, `wiremock`) |
| `:ui-tests` | `tests/frontend` | Playwright-based UI automation |

Frontend собирается и запускается как отдельный Node/Vite-проект в `app/frontend`, но остается частью того же монорепозитория и developer workflow.

## Runtime flow

```text
React SPA
  -> /api/* HTTP contract
  -> Spring Boot backend
       -> PostgreSQL (основная модель данных)
       -> Caffeine caches (public catalog reads)
       -> outbound HTTP gateway for MOCK_ONLINE payment provider (optional)
       -> Spring transactional events
            -> Kafka topic quickmart.order-events (optional)
            -> order_event_audit read model
```

Ключевые последствия такой схемы:

- frontend не содержит бизнес-правил checkout, inventory и order transitions;
- backend остается единственной точкой истины для доменных правил и security-контракта;
- optional Kafka не участвует в синхронном path принятия решения по заказу;
- read-cache применяется только к публичным сценариям чтения и не заменяет основную persistence-модель.

## Source layout

```text
project2/
  app/
    backend/
      src/main/kotlin/com/quickmart/
        cache/         read-cache keys and invalidation
        client/        outbound HTTP gateways and integration DTOs
        config/        Spring and infrastructure configuration
        controller/    public HTTP endpoints
        controller/admin/
        domain/        entities and enums
        dto/           request/response contracts
        events/        internal order integration events
        exception/     error contract and handler
        kafka/         Kafka publisher and consumer
        mapper/        entity <-> DTO mapping
        repository/    JPA repositories
        security/      JWT and auth integration
        service/       business logic
      src/main/resources/
        application.yml
        db/migration/
    frontend/
      src/
        api/           HTTP clients
        app/           root app and router
        pages/         customer and admin screens
        shared/        reusable UI, stores and utilities
  tests/
    config/
      test-environment.properties
    backend/
      shared/          reusable API/cache/kafka/wiremock test infrastructure
      suites/          executable backend test suites
    frontend/
      shared/          Playwright base, fixtures, pages, flows, assertions
      suites/          executable UI suites
    performance/
      smoke-load.js
```

## Backend responsibilities

Backend уже реализует следующие функциональные блоки:

- JWT-аутентификация и авторизация с ролями `CUSTOMER` и `ADMIN`;
- публичный каталог товаров и категорий;
- корзина, адреса доставки и checkout;
- пользовательская история заказов и отмена заказа;
- административное управление товарами, категориями, остатками, заказами, промокодами и delivery slots;
- OpenAPI/Swagger и health endpoint;
- Caffeine cache для публичных чтений каталога;
- HTTP sandbox provider integration для `MOCK_ONLINE` payment authorization;
- optional Kafka audit trail для событий жизненного цикла заказа.

## Frontend responsibilities

Frontend реализует:

- customer flow: каталог, карточка товара, корзина, checkout, addresses, orders;
- auth flow: login, register, session persistence, protected routes;
- admin flow: товары, категории, остатки, заказы, промокоды.

Важно: delivery slot administration уже есть в backend API, но еще не вынесена в SPA.

## Testing architecture

Automation сознательно вынесена из runtime-модулей:

- `tests/backend` проверяет систему либо как внешний REST API, либо как self-contained Spring component layer с WireMock/Embedded Kafka для интеграционных цепочек;
- `tests/frontend` проверяет UI через реальный браузер и артефакты Playwright;
- `tests/performance` содержит отдельный k6 smoke-load сценарий и не смешивается с функциональной автоматизацией.

Это позволяет:

- не перегружать runtime-модули тестовой инфраструктурой;
- развивать отдельные test tasks и CI jobs;
- держать в документации четкое различие между runtime surface и automation surface.

## Ключевые архитектурные решения

### 1. Runtime и automation разделены на уровне модулей

Главная идея проекта - не складывать API/UI automation в `app/backend` или `app/frontend`. Это делает структуру репозитория более понятной для нового участника и упрощает CI.

### 2. Backend - владелец доменных правил

Все критичные инварианты, включая checkout, transition rules, promo validation, delivery slot limits и inventory checks, реализованы в backend-сервисах. Frontend выступает клиентом публичного API, а не носителем правил.

### 3. Public catalog caching ограничен read-side

Кэширование включено только для публичных сценариев чтения:

- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/categories`

Инвалидация завязана на мутации продуктов, категорий и остатков, поэтому cache-слой остается локальным ускорителем, а не альтернативным источником истины.

### 4. Kafka остается optional integration

Kafka используется только для `order lifecycle events -> async audit trail`. Публикация событий происходит после успешного commit бизнес-транзакции, поэтому checkout/order flow не превращается в распределенную сагу.

### 5. HTTP integrations изолируются gateway-слоем

Внешние HTTP-зависимости не встраиваются напрямую в checkout business logic. Для `MOCK_ONLINE` payment flow выделен самостоятельный gateway layer, который:

- инкапсулирует request/response mapping;
- классифицирует timeout/network/server failures;
- переключается configuration-driven между HTTP provider и local fallback;
- тестируется в `:api-tests` через embedded WireMock.

### 6. Swagger - канонический API contract

README и `docs/` описывают архитектуру, workflow и эксплуатационные сценарии. Канонический контракт endpoint-ов и DTO остается в Swagger/OpenAPI, доступном через `/swagger-ui/index.html` и `/v3/api-docs`.
