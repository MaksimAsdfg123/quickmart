# Quickmart

Quickmart - монорепозиторий MVP-сервиса быстрой доставки продуктов и товаров для дома. Репозиторий объединяет backend API на Spring Boot, frontend SPA на React, отдельные модули API/UI automation, performance smoke и Docker Compose-окружение для локальной разработки. Основной бизнес-поток системы: каталог -> корзина -> checkout -> сопровождение заказа; для администраторов доступны управление каталогом, остатками, промокодами и заказами.

## Overview

- `app/backend` - REST API, доменная логика checkout/order management, JWT-аутентификация, Swagger/OpenAPI, Caffeine cache, optional Kafka audit trail и configurable mock-online payment provider gateway.
- `app/frontend` - customer/admin SPA с каталогом, корзиной, адресами, checkout и административными разделами.
- `tests/backend` - отдельный модуль backend automation c black-box API suites и self-contained component suites (`cache`, `kafka`, `wiremock`).
- `tests/frontend` - Playwright-based UI automation.
- `tests/performance` - k6 smoke-load сценарий для nightly performance.

## Реализованные возможности

- Покупатель: регистрация, логин, просмотр каталога и карточек товаров, фильтрация по категориям и поиску, корзина, адресная книга, checkout со слотами доставки и промокодами, история заказов и отмена заказа.
- Администратор: управление категориями, товарами, остатками, промокодами и статусами заказов через `/api/admin/*` и текущую frontend admin-панель.
- Платформа: healthcheck, Swagger UI, сид-данные для demo, after-commit Kafka order events с audit trail, in-memory caching публичного каталога, Docker Compose profiles `core` и `infra`.
- Backend-only capability: управление delivery slots реализовано в backend API, но отдельной страницы в текущем frontend admin UI пока нет.

## Архитектурный обзор

```text
React SPA
  -> REST API (Spring Boot)
  -> PostgreSQL
  -> Caffeine caches for public catalog reads
  -> outbound HTTP gateway for MOCK_ONLINE payment provider (optional)
  -> Spring transactional events
       -> Kafka (optional)
       -> order_event_audit read model

API and UI automation live in separate Gradle modules and validate the system
either through public endpoints/browser flows or through isolated component suites.
```

Ключевые архитектурные решения:

- runtime-код отделен от automation: бизнес-логика живет только в `app/backend` и `app/frontend`, тестовая инфраструктура - только в `tests/*`;
- backend остается единственным владельцем бизнес-правил checkout, статусов заказа, остатков, промокодов и security-контрактов;
- Kafka-интеграция опциональна, включается только флагом и не делает основной checkout flow асинхронным;
- публичные `GET /api/products`, `GET /api/products/{id}` и `GET /api/categories` защищены локальным read-cache с явной инвалидацией на мутациях.

Подробности: [docs/architecture/system-overview.md](docs/architecture/system-overview.md).

## Tech Stack

| Область | Стек |
| --- | --- |
| Backend runtime | Kotlin 1.9, Spring Boot 3.3, Spring MVC, Spring Security, Spring Data JPA, Flyway, PostgreSQL, Springdoc OpenAPI |
| Frontend runtime | React 18, TypeScript, Vite, React Query, React Hook Form, Zustand, Axios |
| Backend automation | Kotlin, JUnit 5, Rest Assured, Spring Boot Test, H2, Embedded Kafka, WireMock, Awaitility, Allure |
| UI automation | Kotlin, Playwright for Java, JUnit 5, Allure |
| Ops / CI | Docker Compose, Kafka, Kafka UI, GitHub Actions, k6 |

## Структура проекта

```text
project2/
  app/
    backend/        Spring Boot backend
    frontend/       React + TypeScript SPA
  tests/
    backend/        API and backend component tests
    frontend/       Playwright UI tests
    performance/    k6 load smoke
    config/         shared test-environment.properties
  docs/
    architecture/
    features/
    integrations/
    operations/
    testing/
  .github/workflows/
  build.gradle.kts
  settings.gradle.kts
  docker-compose.yml
```

## Локальный запуск

### Вариант 1. Полный стек в Docker

```powershell
docker compose --profile core up --build -d
```

После старта доступны:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Healthcheck: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Вариант 2. Полный стек c optional infra

PowerShell:

```powershell
$env:APP_KAFKA_ENABLED="true"
docker compose --profile core --profile infra up --build -d
```

Bash:

```bash
export APP_KAFKA_ENABLED=true
docker compose --profile core --profile infra up --build -d
```

Дополнительно поднимаются:

- Kafka broker: `localhost:9092`
- Kafka UI: [http://localhost:8090](http://localhost:8090)
- Redis: `localhost:6379`
- WireMock: [http://localhost:8089](http://localhost:8089)

`redis` остается опциональным infra-service, а `wiremock` теперь может использоваться как local sandbox provider для `MOCK_ONLINE` checkout при `APP_MOCK_ONLINE_PAYMENT_ENABLED=true`.

### Вариант 3. Hybrid local development

Если backend и frontend нужно запускать локально, а инфраструктуру оставить в Docker:

```powershell
docker compose --profile core up -d postgres
.\gradlew.bat :backend:bootRun
cd app/frontend
npm ci
npm run dev
```

Frontend по умолчанию обращается к `http://localhost:8080`. Для другого адреса backend задайте `VITE_API_BASE_URL`.

Пошаговые сценарии и команды остановки: [docs/operations/local-development.md](docs/operations/local-development.md).

## Demo accounts и seed-данные

- Администратор: `admin@quickmart.local` / `password`
- Покупатель: `anna@example.com` / `password`
- Покупатель: `ivan@example.com` / `password`
- Активные seed-промокоды: `WELCOME100`, `SAVE10`
- Идентификаторы delivery slots не фиксированы; актуальные значения нужно получать через `GET /api/delivery-slots`

## Запуск тестов

Основные Gradle entry points:

```powershell
.\gradlew.bat :backend:test
.\gradlew.bat apiSmokeTest
.\gradlew.bat apiTest
.\gradlew.bat :api-tests:kafkaTest
.\gradlew.bat :api-tests:cacheTest
.\gradlew.bat :api-tests:wiremockTest
.\gradlew.bat installUiBrowsers
.\gradlew.bat uiSmokeTest
.\gradlew.bat uiTest
```

Что важно учитывать:

- активные regression suites сосредоточены в отдельных модулях `:api-tests` и `:ui-tests`;
- `:api-tests:kafkaTest`, `:api-tests:cacheTest` и `:api-tests:wiremockTest` - это self-contained component suites, не требующие заранее поднятого backend через Docker Compose;
- текущая UI automation покрывает auth/access flows, а не весь пользовательский путь приложения;
- текущая API automation покрывает auth smoke и специализированные component suites, а не полный REST-регресс всех бизнес-функций.

Подробности: [docs/testing/test-strategy.md](docs/testing/test-strategy.md).

## Конфигурация

Ключевые переменные окружения:

- backend runtime: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`, `APP_KAFKA_ENABLED`, `KAFKA_BOOTSTRAP_SERVERS`, `APP_CACHE_ENABLED`, `APP_MOCK_ONLINE_PAYMENT_ENABLED`, `APP_MOCK_ONLINE_PAYMENT_BASE_URL`, `APP_MOCK_ONLINE_PAYMENT_API_KEY`;
- frontend runtime: `VITE_API_BASE_URL`;
- automation: `API_BASE_URL`, `UI_BASE_URL`, `AUTH_*`, `E2E_*`, `UI_HEADLESS`, `UI_BROWSER`.

Полный справочник: [docs/operations/configuration.md](docs/operations/configuration.md).

## Documentation Map

- `AGENTS.md` - repo-level instruction entrypoint; для любых запросов на создание или изменение автотестов сначала читать этот файл и затем `docs/testing/test-authoring-rules.md`.
- [docs/architecture/system-overview.md](docs/architecture/system-overview.md) - архитектура системы, границы модулей и ключевые технические решения.
- [docs/features/functional-overview.md](docs/features/functional-overview.md) - реализованные пользовательские и административные возможности, а также backend-only API capabilities.
- [docs/integrations/cache.md](docs/integrations/cache.md) - выбранная cache strategy, схема ключей, invalidation model и текущие trade-offs.
- [docs/integrations/wiremock.md](docs/integrations/wiremock.md) - архитектура WireMock-backed HTTP integration testing для `MOCK_ONLINE` payment provider.
- [docs/integrations/mock-online-payment-provider-contract.md](docs/integrations/mock-online-payment-provider-contract.md) - HTTP-контракт sandbox payment provider, используемый gateway layer и WireMock stubs.
- [docs/operations/local-development.md](docs/operations/local-development.md) - локальный запуск, Docker Compose profiles, hybrid development и служебные команды.
- [docs/operations/configuration.md](docs/operations/configuration.md) - переменные окружения, compose overrides и конфигурация automation.
- [docs/operations/ci.md](docs/operations/ci.md) - GitHub Actions workflow map, CI-контракт и набор публикуемых артефактов.
- [docs/testing/test-strategy.md](docs/testing/test-strategy.md) - обзор тестовых слоев, coverage profile и точек входа.
- [docs/testing/test-authoring-rules.md](docs/testing/test-authoring-rules.md) - canonical rules для написания новых автотестов во всех test-модулях проекта.
- [docs/testing/backend-api-tests.md](docs/testing/backend-api-tests.md) - устройство `:api-tests`, black-box API suites и backend component suites.
- [docs/testing/ui-tests.md](docs/testing/ui-tests.md) - устройство `:ui-tests`, Playwright tasks, diagnostics и текущий scope UI automation.
- [docs/testing/manual-api-checklist.md](docs/testing/manual-api-checklist.md) - ручная smoke-памятка по backend API и seed-данным.
- [docs/integrations/kafka.md](docs/integrations/kafka.md) - архитектура, конфигурация и эксплуатационные детали optional Kafka-интеграции.

## Текущий scope и ограничения

- frontend admin UI покрывает товары, категории, остатки, заказы и промокоды; delivery slots пока доступны только через backend API;
- GitHub Actions запускают black-box API/UI suites и performance smoke, но не запускают `:api-tests:kafkaTest`, `:api-tests:cacheTest` и `:api-tests:wiremockTest` как отдельные job'ы;
- backend module test task существует и участвует в CI, но основная автоматизация проекта живет в специализированных test-модулях;
- Kafka и HTTP sandbox payment provider остаются feature-flagged интеграциями для local/dev/test usage и по умолчанию выключены.
