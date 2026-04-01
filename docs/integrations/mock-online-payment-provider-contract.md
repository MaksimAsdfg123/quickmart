# Контракт sandbox payment provider

## Назначение

Документ фиксирует HTTP-контракт внешнего sandbox payment provider, используемого Quickmart для сценария `MOCK_ONLINE` оплаты. Контракт нужен для двух целей:

- для runtime-интеграции через gateway `MockOnlinePaymentGateway`;
- для WireMock-backed component tests, которые симулируют поведение внешнего сервиса на HTTP-уровне.

Контракт намеренно ограничен одним endpoint авторизации платежа. Это соответствует текущему scope backend: Quickmart не поддерживает capture, refund, webhook callbacks или reconciliation.

## Endpoint

- Метод: `POST`
- Путь: `/api/v1/payments/authorize`
- Content-Type request: `application/json`
- Content-Type response: `application/json`

## Request contract

### Headers

| Header | Обязательность | Назначение |
| --- | --- | --- |
| `X-Api-Key` | Да | Простой credential для sandbox provider |
| `X-Request-Id` | Да | Идентификатор запроса для трассировки и идемпотентной диагностики |

### Body

```json
{
  "customerId": "00000000-0000-0000-0000-000000000001",
  "amount": 1449.00,
  "currency": "RUB",
  "paymentMethod": "MOCK_ONLINE"
}
```

### Field semantics

| Поле | Тип | Назначение |
| --- | --- | --- |
| `customerId` | `UUID` | Идентификатор покупателя в Quickmart |
| `amount` | `decimal` | Сумма, которую требуется авторизовать |
| `currency` | `string` | Валюта операции; в текущем scope всегда `RUB` |
| `paymentMethod` | `string` | Тип оплаты; в текущем scope всегда `MOCK_ONLINE` |

## Response contract

### Success response

HTTP status: `200`

```json
{
  "decision": "APPROVED",
  "paymentReference": "wm-pay-approved-001"
}
```

Правила интерпретации:

- `decision` должно быть равно `APPROVED`;
- `paymentReference` должно быть непустым;
- любое успешное тело без `paymentReference` или с неожиданным `decision` считается `INVALID_RESPONSE`.

### Functional rejection response

HTTP status: `409`

```json
{
  "code": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds"
}
```

Правила интерпретации:

- rejection считается управляемым бизнес-результатом;
- Quickmart не коммитит заказ и возвращает контролируемую `BusinessException` с HTTP 409;
- тело rejection допускает альтернативные `code` и `message`, если они непустые.

### Server-side error response

HTTP status: `500` / `503`

```json
{
  "code": "TEMPORARY_UNAVAILABLE",
  "message": "Provider maintenance"
}
```

Правила интерпретации:

- любые `5xx` ответы трактуются как техническая недоступность провайдера;
- Quickmart завершает checkout контролируемой `BusinessException` с HTTP 503;
- детали body не участвуют в бизнес-решении и используются только для диагностики.

### Invalid payload response

Пример:

```json
{
  "decision": "APPROVED"
}
```

Правила интерпретации:

- payload считается некорректным, если успешный ответ нельзя разобрать или в нем отсутствуют обязательные поля;
- Quickmart трактует такой ответ как `INVALID_RESPONSE`;
- checkout завершается контролируемой `BusinessException` с HTTP 502.

## Timeout contract

Timeout моделируется отсутствием ответа в пределах configured `read-timeout`.

Текущая runtime-конфигурация Quickmart использует:

- `connect-timeout`
- `read-timeout`

Оба значения задаются через `app.integrations.mock-online-payment.*` и могут различаться между local/dev/test окружениями.

## Mapping rules inside Quickmart

| Provider outcome | Internal mapping | Business effect |
| --- | --- | --- |
| `200 APPROVED + paymentReference` | `MockOnlinePaymentAuthorizationResult.Approved` | Заказ коммитится, `PaymentStatus=PAID` |
| `4xx` rejection | `MockOnlinePaymentAuthorizationResult.Rejected` | Заказ не создается, клиент получает controlled 409 |
| `5xx` | `TechnicalFailure(SERVER_ERROR)` | Заказ не создается, клиент получает controlled 503 |
| timeout | `TechnicalFailure(TIMEOUT)` | Заказ не создается, клиент получает controlled 503 |
| malformed body | `TechnicalFailure(INVALID_RESPONSE)` | Заказ не создается, клиент получает controlled 502 |

## Non-goals

Текущий контракт намеренно не покрывает:

- asynchronous callbacks/webhooks;
- pre-auth / capture split;
- refunds;
- partial approvals;
- idempotency keys на стороне провайдера;
- HMAC signatures или OAuth-based authentication.

При расширении integration scope контракт должен обновляться до изменения gateway logic и WireMock stubs.
