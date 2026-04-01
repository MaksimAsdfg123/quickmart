# Конфигурация

## Источники конфигурации

### Backend runtime

Backend читает настройки из:

1. переменных окружения;
2. `app/backend/src/main/resources/application.yml`;
3. значений по умолчанию, заданных через placeholder fallback в `application.yml`.

### Frontend runtime

Frontend использует:

1. `VITE_API_BASE_URL` на этапе сборки или старта dev server;
2. fallback `http://localhost:8080` в `app/frontend/src/api/client.ts`.

### Test automation

Automation использует два источника:

1. env vars;
2. `tests/config/test-environment.properties`.

Часть UI-настроек имеет только кодовые fallback values в `UiTestEnvironment`.

## Backend runtime variables

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/quickmart` | JDBC URL для backend runtime |
| `DB_USERNAME` | `quickmart` | Пользователь БД |
| `DB_PASSWORD` | `quickmart` | Пароль БД |
| `SERVER_PORT` | `8080` | HTTP-порт backend |
| `SPRING_MAIN_LAZY_INITIALIZATION` | `true` | Lazy initialization Spring context |
| `JWT_SECRET` | `very-secret-jwt-key-change-me-in-production-please` | Секрет JWT |
| `JWT_EXPIRATION_SECONDS` | `86400` | TTL JWT в секундах |
| `APP_KAFKA_ENABLED` | `false` | Включает producer/consumer Kafka |
| `APP_KAFKA_TOPIC` | `quickmart.order-events` | Topic order lifecycle events |
| `APP_KAFKA_CONSUMER_GROUP` | `quickmart-order-audit` | Consumer group для audit consumer |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Адрес Kafka broker |
| `APP_CACHE_ENABLED` | `true` | Глобальный флаг read-cache |
| `APP_MOCK_ONLINE_PAYMENT_ENABLED` | `false` | Включает HTTP gateway для `MOCK_ONLINE` payment provider |
| `APP_MOCK_ONLINE_PAYMENT_BASE_URL` | `http://localhost:8089` | Base URL sandbox payment provider |
| `APP_MOCK_ONLINE_PAYMENT_API_KEY` | `quickmart-local-sandbox-key` | API key для sandbox payment provider |
| `APP_MOCK_ONLINE_PAYMENT_CONNECT_TIMEOUT` | `500ms` | Connect timeout внешнего payment provider |
| `APP_MOCK_ONLINE_PAYMENT_READ_TIMEOUT` | `2s` | Read timeout внешнего payment provider |
| `APP_CACHE_PUBLIC_CATALOG_TTL` | `30s` | TTL для публичных страниц каталога |
| `APP_CACHE_PUBLIC_CATALOG_MAX_SIZE` | `256` | Max size для cache страниц каталога |
| `APP_CACHE_PUBLIC_PRODUCT_TTL` | `30s` | TTL для карточек товара |
| `APP_CACHE_PUBLIC_PRODUCT_MAX_SIZE` | `1024` | Max size для cache карточек товара |
| `APP_CACHE_PUBLIC_CATEGORIES_TTL` | `30m` | TTL для списка публичных категорий |
| `APP_CACHE_PUBLIC_CATEGORIES_MAX_SIZE` | `64` | Max size для cache категорий |

## Frontend variables

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080` | Base URL для Axios client |

Для Docker-образа frontend значение передается как build argument и превращается в `ENV` внутри стадии сборки.

## Docker Compose overrides

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `POSTGRES_PORT` | `5432` | Порт Postgres на хосте |
| `BACKEND_PORT` | `8080` | Порт backend на хосте |
| `FRONTEND_PORT` | `5173` | Порт frontend на хосте |
| `KAFKA_PORT` | `9092` | Kafka listener для хоста |
| `KAFKA_UI_PORT` | `8090` | Порт Kafka UI |
| `REDIS_PORT` | `6379` | Порт Redis |
| `WIREMOCK_PORT` | `8089` | Порт WireMock |

## Automation variables

### Backend API automation

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `TEST_ENVIRONMENT` | `local` | Имя окружения для отчета и диагностики |
| `API_BASE_URL` | `http://127.0.0.1:8080` | Base URL black-box API tests |
| `API_CONNECT_TIMEOUT_MS` | `5000` | HTTP connect timeout |
| `API_READ_TIMEOUT_MS` | `10000` | HTTP read timeout |
| `AUTH_ADMIN_EMAIL` | `admin@quickmart.local` | Seeded admin account |
| `AUTH_ADMIN_PASSWORD` | `password` | Пароль admin account |
| `AUTH_CUSTOMER_EMAIL` | `anna@example.com` | Seeded customer account |
| `AUTH_CUSTOMER_PASSWORD` | `password` | Пароль customer account |
| `API_HTTP_TRACE_CONSOLE` | зависит от `CI` | Вывод HTTP trace в консоль для API tests |

### UI automation

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `UI_BASE_URL` | `http://127.0.0.1:5173` | Base URL frontend для Playwright |
| `API_BASE_URL` | `http://127.0.0.1:8080` | Base URL backend для API setup helpers |
| `E2E_CUSTOMER_EMAIL` | `anna@example.com` | Customer account для UI tests |
| `E2E_CUSTOMER_PASSWORD` | `password` | Пароль customer account |
| `E2E_ADMIN_EMAIL` | `admin@quickmart.local` | Admin account для UI tests |
| `E2E_ADMIN_PASSWORD` | `password` | Пароль admin account |
| `UI_HEADLESS` | `true` | Headless/headed режим браузера |
| `UI_BROWSER` | `chromium` | `chromium`, `firefox` или `webkit` |
| `UI_SLOW_MO_MS` | `0` | Замедление действий браузера |
| `UI_ACTION_TIMEOUT_MS` | `10000` | Action timeout |
| `UI_NAVIGATION_TIMEOUT_MS` | `25000` | Navigation timeout |
| `DEBUG` | пусто | Playwright debug trace в консоль при debug runs |

## Test resources

| Файл | Роль |
| --- | --- |
| `tests/config/test-environment.properties` | Общий cross-project contract для API/UI automation |
| `tests/backend/resources/application-test.yml` | Spring Boot test profile для backend component suites |
| `tests/backend/resources/junit-platform.properties` | JUnit parallel config для backend test module |
| `tests/frontend/resources/junit-platform.properties` | JUnit parallel config для UI test module |
| `tests/frontend/resources/logback-test.xml` | UI test logging |
| `tests/**/resources/allure.properties` | Allure output configuration |

## Seed-данные и test accounts

Миграции backend создают:

- аккаунты `admin@quickmart.local`, `anna@example.com`, `ivan@example.com`;
- начальные адреса для customer users;
- каталог, остатки, промокоды и delivery slots.

Эти данные используются как в ручной проверке, так и в CI bootstrap scenarios.

## Замечания по эксплуатации

- `APP_KAFKA_ENABLED=false` - штатное значение для обычного локального запуска;
- `APP_CACHE_ENABLED=true` - штатное значение для runtime и component cache tests;
- `wiremock` может использоваться как local sandbox payment provider при `APP_MOCK_ONLINE_PAYMENT_ENABLED=true`; в Docker backend по умолчанию используется `http://wiremock:8080`, а при local `bootRun` - `http://localhost:8089`;
- `redis` поднимается через `docker-compose`, но текущий backend на него не завязан;
- если меняются значения env vars для Docker backend, контейнер нужно перезапустить.
