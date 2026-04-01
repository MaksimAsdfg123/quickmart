# Backend API и component tests

## Назначение

Модуль `:api-tests` (`tests/backend`) объединяет два класса проверок:

- black-box API automation против уже запущенного backend;
- self-contained backend component suites, поднимающие Spring context локально на test profile.

Это принципиально важно для навигации по проекту: не все задачи модуля `:api-tests` требуют заранее поднятый Docker stack или внешний backend.

Базовый стандарт для всех новых backend API и backend component tests зафиксирован в [test-authoring-rules.md](test-authoring-rules.md). Текущий документ описывает только устройство и scope модуля `:api-tests`.

## Структура модуля

```text
tests/backend/
  build.gradle.kts
  suites/kotlin/com/quickmart/test/suites/
    api/
      auth/
        AuthApiTest.kt
    cache/
      catalog/
        PublicCatalogCacheTest.kt
      category/
        PublicCategoryCacheTest.kt
      config/
        CacheDisabledTest.kt
    kafka/
      config/
      failure/
      flow/
      service/
    wiremock/
      payment/
        MockOnlinePaymentCheckoutTest.kt
  shared/kotlin/com/quickmart/test/shared/
    foundation/
    clients/
    auth/
    cache/
    kafka/
    wiremock/
    common/
  resources/
    application-test.yml
    allure.properties
    junit-platform.properties
  ../config/
    test-environment.properties
```

## Gradle tasks

| Task | Тип проверки | Требования |
| --- | --- | --- |
| `:api-tests:apiSmokeTest` | black-box smoke suite (`@Tag("smoke")`) | backend должен быть доступен по `API_BASE_URL` |
| `:api-tests:apiTest` | black-box API suite (`@Tag("api")`) | backend должен быть доступен по `API_BASE_URL` |
| `:api-tests:test` | default test task | запускает module test layer, но исключает `kafka`, `cache` и `wiremock` tags |
| `:api-tests:kafkaTest` | Kafka component suites (`@Tag("kafka")`) | внешний backend не нужен; используется Spring test profile + H2 + Embedded Kafka |
| `:api-tests:cacheTest` | cache component suites (`@Tag("cache")`) | внешний backend не нужен; используется Spring test profile + H2 |
| `:api-tests:wiremockTest` | WireMock component suites (`@Tag("wiremock")`) | внешний backend не нужен; используется Spring test profile + H2 + embedded WireMock |

Root shortcuts:

- `.\gradlew.bat apiSmokeTest`
- `.\gradlew.bat apiTest`

## Current suite inventory

### Black-box API suite

Сейчас black-box API automation покрывает auth/access scenarios:

- регистрация пользователя;
- логин customer и admin;
- duplicate email;
- invalid credentials;
- validation errors;
- доступ к защищенному endpoint без токена;
- запрет доступа customer к admin endpoint;
- базовая проверка корзины после регистрации.

### Cache component suites

`cacheTest` покрывает:

- cache hit для публичных страниц каталога;
- cache hit для карточки товара;
- invalidation после обновления товара;
- invalidation после изменения остатков;
- invalidation после изменения или деактивации категории;
- корректный fallback при `APP_CACHE_ENABLED=false`.

### Kafka component suites

`kafkaTest` покрывает:

- публикацию `order.created`, `order.status_changed`, `order.cancelled`;
- корректность event envelope и payload;
- audit trail ordering;
- rollback/no-publish behavior для failure scenarios;
- disabled mode при `app.kafka.enabled=false`;
- идемпотентность audit storage.

### WireMock component suites

`wiremockTest` покрывает:

- `MOCK_ONLINE` checkout against embedded HTTP sandbox provider;
- successful authorization with persisted external reference;
- functional rejection without order persistence;
- provider `5xx` handling;
- timeout handling;
- malformed payload handling;
- verification of outgoing request path, headers, body and call count;
- rollback semantics for cart and stock when external authorization fails.

## Как устроен shared layer

Модуль следует паттерну `suites + shared`:

- `shared/foundation` - общая HTTP-конфигурация, Allure/logging, environment loading;
- `shared/clients` - endpoint-oriented REST clients без assertions;
- `shared/<domain>/scenario` - повторно используемые бизнес-последовательности;
- `shared/<domain>/assertion` - доменные проверки;
- `shared/<domain>/data` - фабрики данных и модели;
- `shared/wiremock/.../mock` - reusable WireMock stub definitions и request verification helpers;
- `shared/common` - общие утилиты.

## Environment and resources

### Black-box API suites

Black-box tests читают значения в порядке:

1. env vars;
2. `tests/config/test-environment.properties`;
3. fallback values в коде.

Ключевые значения:

- `API_BASE_URL`
- `AUTH_ADMIN_EMAIL`
- `AUTH_ADMIN_PASSWORD`
- `AUTH_CUSTOMER_EMAIL`
- `AUTH_CUSTOMER_PASSWORD`

### Component suites

`kafkaTest`, `cacheTest` и `wiremockTest` используют:

- Spring profile `test`;
- `tests/backend/resources/application-test.yml`;
- in-memory H2 вместо PostgreSQL;
- `EmbeddedKafka` для enabled Kafka suites;
- embedded WireMock server для external HTTP integration suites;
- `app.kafka.enabled=false` для disabled Kafka suite и cache suites.

## Allure и отчеты

- Allure results: `tests/backend/build/allure-results`
- JUnit XML: `tests/backend/build/test-results`
- HTML reports: `tests/backend/build/reports/tests`

Пример генерации отчета:

```powershell
allure generate tests/backend/build/allure-results --clean -o tests/backend/build/allure-report
allure open tests/backend/build/allure-report
```

## Parallel execution

- в `tests/backend/resources/junit-platform.properties` включен parallel execution;
- black-box tasks `apiTest` и `apiSmokeTest` принудительно отключают JUnit parallel mode для более читаемых HTTP trace attachments;
- cache/kafka/wiremock base suites используют `ExecutionMode.SAME_THREAD`, чтобы не смешивать контексты component tests.

## Правила поддержки

- новые black-box API tests добавляются в `suites/api/...`;
- новые cache component tests добавляются в `suites/cache/...`;
- новые Kafka component tests добавляются в `suites/kafka/...`;
- новые external HTTP integration tests добавляются в `suites/wiremock/...`;
- не следует переносить backend automation обратно в `app/backend/src/test/...`;
- `app/backend/src/test` может использоваться для runtime-модульных тестов, но активный test framework проекта сосредоточен в `tests/backend`.
