# WireMock-backed HTTP integration testing

## 1. Назначение

Документ описывает production-like сценарий тестирования внешней HTTP-интеграции Quickmart через WireMock. Реализация используется для сценария `MOCK_ONLINE` оплаты в checkout flow и демонстрирует controlled external dependency simulation без подмены бизнес-логики unit-level моками.

Цель решения:

- выделить внешний HTTP adapter слой;
- изолированно тестировать поведение backend при разных ответах провайдера;
- валидировать не только обработку response, но и корректность исходящего HTTP request;
- сохранить предсказуемое поведение checkout при отказах внешнего сервиса.

## 2. Выбранный функциональный блок

### 2.1 Выбранный сценарий

Выбран блок `MOCK_ONLINE payment authorization` в checkout flow.

С точки зрения архитектуры этот сценарий подходит лучше других кандидатов, потому что:

- он небольшой и функционально завершенный;
- уже существует в бизнес-модели приложения как отдельный `PaymentMethod`;
- имеет естественную синхронную HTTP-интеграционную точку;
- допускает богатый failure-oriented test set: success, rejection, `5xx`, timeout, malformed payload;
- результат интеграции прямо влияет на observable business outcome checkout.

### 2.2 Почему WireMock оправдан именно здесь

WireMock уместен, потому что задача состоит не в unit-проверке одной функции, а в проверке целого integration path:

- бизнес-сервис инициирует HTTP-вызов;
- gateway формирует headers и JSON body;
- приложение интерпретирует разные классы ответов;
- transaction boundary корректно удерживает или откатывает бизнес-изменения.

Прямой unit mocking `RestClient` не позволяет надежно проверить:

- HTTP path;
- request headers;
- request body;
- network timeout semantics;
- mapping raw HTTP response в интеграционный результат.

### 2.3 Рассмотренные альтернативы

| Кандидат | Статус | Причина отказа |
| --- | --- | --- |
| Notification service | Не выбран | Для текущего проекта это был бы искусственный side effect с меньшей бизнес-ценностью и слабее наблюдаемым результатом |
| Recommendation/info service | Не выбран | Сценарий выглядел бы менее естественным и не давал бы такого ясного failure impact |
| Address/verification API | Не выбран | Потребовал бы добавления новой доменной логики без достаточной базы в текущем приложении |

## 3. Архитектурное решение

### 3.1 Слои ответственности

Реализация разделена на четыре слоя.

#### Domain / service layer

- `PaymentProcessingService`
- `CheckoutService`

Назначение:

- определить, когда требуется внешний вызов;
- перевести outcome провайдера в доменный `PaymentStatus` или контролируемую `BusinessException`;
- сохранить checkout-правила и transaction semantics внутри backend.

#### External client / gateway layer

- `MockOnlinePaymentGateway`
- `HttpMockOnlinePaymentGateway`
- `LocalMockOnlinePaymentGateway`
- DTO в `client/payment/model`

Назначение:

- инкапсулировать HTTP-вызовы;
- скрыть от сервисного слоя детали endpoint path, headers, timeout handling и JSON mapping;
- предоставить единый integration contract для runtime и test profiles.

#### Configuration layer

- `MockOnlinePaymentProperties`
- `MockOnlinePaymentClientConfig`
- `application.yml`

Назначение:

- хранить `base-url`, `api-key`, timeouts и feature flag;
- выбирать HTTP gateway или local fallback bean через configuration-driven wiring.

#### Test support / mocks layer

- `tests/backend/shared/.../wiremock/payment/foundation`
- `tests/backend/shared/.../wiremock/payment/mock`
- `tests/backend/shared/.../wiremock/payment/scenario`
- `tests/backend/shared/.../wiremock/payment/assertion`

Назначение:

- централизовать WireMock server lifecycle;
- переиспользовать stub definitions;
- держать suite-классы короткими и сценарными;
- валидировать business outcome и outgoing request отдельно.

### 3.2 Runtime flow

```text
CheckoutService
  -> PaymentProcessingService
  -> MockOnlinePaymentGateway
       -> HttpMockOnlinePaymentGateway (enabled=true)
          -> POST /api/v1/payments/authorize
       -> LocalMockOnlinePaymentGateway (fallback when enabled=false)
  -> payment result
  -> order persistence / rollback
```

### 3.3 Почему выбран Adapter / Gateway pattern

Gateway pattern применен для того, чтобы:

- не связывать бизнес-логику checkout с деталями HTTP;
- упростить замену sandbox provider на другой transport или реальный provider;
- изолировать request/response mapping и failure classification;
- сделать тестирование на HTTP-уровне локальным и воспроизводимым.

## 4. Реализация интеграции

### 4.1 Основные runtime-компоненты

- `app/backend/src/main/kotlin/com/quickmart/service/PaymentProcessingService.kt`
- `app/backend/src/main/kotlin/com/quickmart/client/payment/MockOnlinePaymentGateway.kt`
- `app/backend/src/main/kotlin/com/quickmart/client/payment/HttpMockOnlinePaymentGateway.kt`
- `app/backend/src/main/kotlin/com/quickmart/client/payment/LocalMockOnlinePaymentGateway.kt`
- `app/backend/src/main/kotlin/com/quickmart/config/MockOnlinePaymentClientConfig.kt`
- `app/backend/src/main/kotlin/com/quickmart/config/MockOnlinePaymentProperties.kt`

### 4.2 Конфигурация

Используются следующие runtime-параметры:

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `APP_MOCK_ONLINE_PAYMENT_ENABLED` | Включает реальный HTTP gateway | `false` |
| `APP_MOCK_ONLINE_PAYMENT_BASE_URL` | Base URL внешнего payment provider | `http://localhost:8089` |
| `APP_MOCK_ONLINE_PAYMENT_API_KEY` | Credential для sandbox provider | `quickmart-local-sandbox-key` |
| `APP_MOCK_ONLINE_PAYMENT_CONNECT_TIMEOUT` | Connect timeout | `500ms` |
| `APP_MOCK_ONLINE_PAYMENT_READ_TIMEOUT` | Read timeout | `2s` |

Если feature flag выключен, используется `LocalMockOnlinePaymentGateway`, сохраняющий локальную sandbox-семантику без внешнего HTTP-зависимого вызова.

### 4.3 Request / response mapping

HTTP gateway отправляет:

- `POST /api/v1/payments/authorize`
- `Content-Type: application/json`
- `Accept: application/json`
- `X-Api-Key`
- `X-Request-Id`

Body содержит:

- `customerId`
- `amount`
- `currency`
- `paymentMethod`

Подробный wire contract фиксирован в [mock-online-payment-provider-contract.md](mock-online-payment-provider-contract.md).

### 4.4 Обработка ошибок

Gateway разделяет ошибки на две категории:

- управляемый бизнес-отказ провайдера;
- техническая недоступность или некорректный ответ.

Внутреннее отображение:

| Внешний результат | Внутренний эффект |
| --- | --- |
| `409` с rejection payload | `BusinessException` со статусом `409` |
| `5xx` | `BusinessException` со статусом `503` |
| timeout | `BusinessException` со статусом `503` |
| malformed success payload | `BusinessException` со статусом `502` |

Это позволяет не допустить uncontrolled exception leakage наружу и сохранить детерминированное поведение checkout.

## 5. WireMock strategy

### 5.1 Почему используются reusable stubs

WireMock-слой не реализован как набор одноразовых inline-моков внутри тестов. Stub definitions вынесены в `MockOnlinePaymentWireMockStubs`, потому что это:

- уменьшает дублирование;
- централизует endpoint path, headers и expected request body;
- облегчает добавление новых provider scenarios;
- делает suite-классы читаемыми и пригодными для code review.

### 5.2 Реализованные stub scenarios

| Сценарий | HTTP поведение | Назначение |
| --- | --- | --- |
| Success | `200` + valid approval payload | Проверка happy path checkout |
| Functional rejection | `409` + rejection payload | Проверка controlled business rejection |
| Server-side error | `503` | Проверка controlled technical failure |
| Timeout | `200` c fixed delay > `read-timeout` | Проверка timeout handling |
| Malformed payload | `200` + incomplete success payload | Проверка invalid response handling |

### 5.3 Что именно проверяют tests

Тесты валидируют:

- корректность бизнес-результата checkout;
- наличие или отсутствие persistence side effects;
- rollback semantics при неуспешном внешнем вызове;
- корректность outgoing request:
  - path;
  - headers;
  - request body;
  - количество вызовов.

## 6. Разница между unit mocking, WireMock и реальной интеграцией

### 6.1 Unit-level mocking

Unit-мок gateway полезен для локальной проверки ветвления бизнес-логики, но не проверяет:

- HTTP serialization;
- headers;
- timeout behavior;
- response parsing.

### 6.2 WireMock-backed integration testing

WireMock проверяет integration contract на уровне HTTP без реального внешнего провайдера. Это текущий целевой уровень проверки для Quickmart.

Преимущества:

- воспроизводимость;
- отсутствие внешней сетевой зависимости;
- возможность тестировать деградации и некорректные payload;
- контроль над transport-level деталями.

### 6.3 Реальная внешняя интеграция

Реальный provider нужен только для sandbox/UAT или production verification. Он не заменяет WireMock, потому что:

- менее стабилен;
- хуже воспроизводим;
- дороже для CI;
- не гарантирует покрытие failure cases по требованию теста.

## 7. Тестовая архитектура

### 7.1 Расположение тестов

Все WireMock-backed тесты размещены в `tests/backend`, а не в runtime-модуле:

- suite: `tests/backend/suites/kotlin/com/quickmart/test/suites/wiremock/payment`
- shared layer: `tests/backend/shared/kotlin/com/quickmart/test/shared/wiremock/payment`

Это соответствует общему правилу проекта: активная backend automation сосредоточена в `:api-tests`.

### 7.2 Основные test components

- `BaseMockOnlinePaymentComponentTest`
- `MockOnlinePaymentCheckoutScenario`
- `MockOnlinePaymentWireMockStubs`
- `MockOnlinePaymentAssertions`
- `MockOnlinePaymentTestDataFactory`

### 7.3 Покрываемые сценарии

- успешная авторизация внешним provider;
- функциональный отказ провайдера;
- server-side ошибка провайдера;
- timeout;
- malformed payload;
- проверка request contract;
- проверка rollback side effects.

## 8. Локальный запуск и проверка

### 8.1 Автотесты

Основная команда:

```powershell
.\gradlew.bat :api-tests:wiremockTest
```

Дополнительно:

```powershell
.\gradlew.bat :api-tests:testClasses
.\gradlew.bat :backend:bootJar
```

### 8.2 Локальная ручная проверка

#### Local backend + containerized WireMock

PowerShell:

```powershell
$env:APP_MOCK_ONLINE_PAYMENT_ENABLED="true"
docker compose --profile infra up -d wiremock
.\gradlew.bat :backend:bootRun
```

#### Full Docker stack + infra

PowerShell:

```powershell
$env:APP_MOCK_ONLINE_PAYMENT_ENABLED="true"
docker compose --profile core --profile infra up --build -d
```

В Docker backend использует `APP_MOCK_ONLINE_PAYMENT_BASE_URL=http://wiremock:8080`, поэтому отдельный override не нужен.

Для ручной проверки необходимо:

1. Добавить или обновить WireMock mapping.
2. Выполнить checkout с `paymentMethod=MOCK_ONLINE`.
3. Проверить HTTP response backend.
4. Убедиться, что при success создается заказ.
5. Убедиться, что при error/timeout заказ не коммитится.

## 9. Ограничения и trade-offs

### 9.1 Ограничения текущего scope

- интеграция ограничена одним synchronous authorization endpoint;
- retry/circuit breaker не добавлялись, чтобы не усложнять MVP без подтвержденной необходимости;
- аутентификация провайдера упрощена до `X-Api-Key`;
- нет webhook-based eventual consistency;
- timeout и error handling реализованы на уровне request lifecycle без advanced resilience stack.

### 9.2 Архитектурные trade-offs

| Решение | Выигрыш | Компромисс |
| --- | --- | --- |
| Отдельный gateway слой | Чистая бизнес-архитектура и расширяемость | Дополнительные классы и конфигурация |
| Configuration-driven switch между HTTP и local gateway | Безопасный local fallback | Поведение зависит от feature flag и env-конфига |
| WireMock component tests вместо unit-only mocks | Реалистичная проверка integration contract | Более тяжелый test setup |
| Отсутствие retry/circuit breaker | Простота и предсказуемость MVP | Нет advanced resilience policy |

## 10. Как расширять решение

При дальнейшем развитии рекомендуется:

- добавлять новые provider endpoints отдельными gateway methods, а не встраивать HTTP в сервисы;
- расширять `MockOnlinePaymentWireMockStubs`, а не плодить inline stubs в suite-классах;
- фиксировать новые request/response contracts в отдельном integration document;
- при появлении retry/circuit breaker обновить failure matrix и тесты;
- если понадобится multi-provider routing, вводить provider-specific adapters за общим gateway interface.

## 11. Почему такой подход лучше, чем прямые моки в тестах

Подход с WireMock лучше прямых самодельных моков, потому что:

- проверяет реальный HTTP contract, а не только вызов метода;
- отражает behavior внешней зависимости на транспортном уровне;
- дает воспроизводимые timeout/error scenarios;
- облегчает review и собеседовательную демонстрацию архитектурного качества;
- масштабируется на новые integration cases без ломки бизнес-слоя.
