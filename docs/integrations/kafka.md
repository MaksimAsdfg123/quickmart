# Kafka-интеграция

## Scope

Текущая Kafka-интеграция Quickmart реализует один целевой сценарий:

`order lifecycle events -> async audit trail`

Интеграция остается намеренно ограниченной:

- Kafka выключена по умолчанию;
- используется один topic;
- используется один consumer group;
- основной бизнес-flow заказа продолжает работать синхронно и без обязательной зависимости от Kafka.

## Архитектурный поток

```text
checkout / cancel / status update
  -> OrderEventPublisher
  -> Spring application event
  -> AFTER_COMMIT listener
  -> Kafka topic quickmart.order-events
  -> OrderEventAuditConsumer
  -> order_event_audit
  -> GET /api/admin/orders/{id}/events
```

### Компоненты

| Компонент | Роль |
| --- | --- |
| `OrderEventPublisher` | публикует внутренние order lifecycle events из бизнес-логики |
| `OrderKafkaPublisher` | отправляет событие в Kafka после commit транзакции |
| `OrderEventAuditConsumer` | читает Kafka-сообщения и передает их на сохранение |
| `OrderEventAuditService` | сохраняет audit trail и обеспечивает идемпотентность |
| `AdminOrderController` | отдает историю событий через `/api/admin/orders/{id}/events` |
| `V4__order_event_audit.sql` | создает таблицу audit storage |

Ключевое правило: сообщение публикуется только после успешного commit бизнес-транзакции.

## Event contract

### Topic и consumer group

- topic: `quickmart.order-events`
- consumer group: `quickmart-order-audit`

### Envelope

Каждое сообщение содержит:

- `eventId`
- `eventType`
- `aggregateId`
- `occurredAt`
- `payloadVersion`
- `payload`

`aggregateId` совпадает с `orderId`.

### Поддерживаемые типы событий

- `order.created`
- `order.status_changed`
- `order.cancelled`

### Payload semantics

Payload описывает состояние заказа на момент публикации события и включает:

- `orderId`
- `userId`
- `previousStatus`
- `currentStatus`
- `paymentMethod`
- `paymentStatus`
- `total`
- `itemCount`
- `items`

Важно для интерпретации:

- при `CARD` заказ публикуется как уже оплаченный (`paymentStatus=PAID`);
- при `CASH` заказ создается с `paymentStatus=PENDING`, а при доставке переводится в `PAID`;
- при failed `MOCK_ONLINE` платеж не коммитится и Kafka side effect не возникает.

## Конфигурация

### Backend

Основные настройки backend:

- `APP_KAFKA_ENABLED=false`
- `APP_KAFKA_TOPIC=quickmart.order-events`
- `APP_KAFKA_CONSUMER_GROUP=quickmart-order-audit`
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`

### Docker Compose

Kafka и Kafka UI включены только в профиль `infra`.

Запуск containerized stack с Kafka:

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

В Docker Compose используются два listener-а:

- `kafka:19092` для контейнеров;
- `localhost:9092` для локальных клиентов и backend, запущенного вне Docker.

## Kafka UI

Kafka UI поднимается только для local/dev/test диагностики.

Что в нем полезно смотреть:

- topic `quickmart.order-events`;
- consumer group `quickmart-order-audit`;
- offsets и lag;
- JSON envelope конкретных сообщений.

UI не является production-компонентом и не должен публиковаться наружу.

## Хранение audit trail

История событий сохраняется в таблицу `order_event_audit`:

- `event_id`
- `order_id`
- `event_type`
- `payload_version`
- `occurred_at`
- `payload_json`
- `created_at`
- `updated_at`

Идемпотентность обеспечивается `UNIQUE`-ограничением на `event_id`.

## Тестовое покрытие Kafka

`.\gradlew.bat :api-tests:kafkaTest` покрывает:

- публикацию `order.created`, `order.status_changed`, `order.cancelled`;
- корректность event envelope и payload;
- audit trail ordering;
- rollback/no-publish behavior для business failures;
- disabled mode при `app.kafka.enabled=false`;
- идемпотентность audit storage и работу read model.

Важно: этот task не запускается отдельным job'ом в стандартном GitHub Actions PR/main pipeline, поэтому при изменениях Kafka-кода его нужно прогонять явно.

## Manual smoke

1. Запустить backend с `APP_KAFKA_ENABLED=true`.
2. Поднять `infra` профиль и открыть Kafka UI.
3. Выполнить checkout.
4. Изменить статус заказа или отменить заказ.
5. Проверить сообщения в `quickmart.order-events`.
6. Сверить данные с `GET /api/admin/orders/{id}/events`.

## Ограничения текущей реализации

- интеграция рассчитана на local/dev/test usage;
- используется single-broker Kafka setup;
- нет outbox-таблицы и гарантий exactly-once между БД и Kafka;
- нет schema registry;
- payload versioning ограничен полем `payloadVersion`;
- Kafka не является обязательной частью обычного `core` запуска.

## Troubleshooting

### Kafka выключена

Симптомы:

- topic пустой;
- consumer group не появляется;
- `/api/admin/orders/{id}/events` возвращает пустой список.

Проверьте:

- `APP_KAFKA_ENABLED=true`;
- backend перезапущен после смены env vars;
- backend действительно подключается к нужному broker.

### Сообщения есть в topic, но audit trail пустой

Проверьте:

- существует ли consumer group `quickmart-order-audit`;
- нет ли ошибок десериализации или записи в БД;
- запрашивается ли правильный `orderId`;
- не идет ли чтение слишком рано до завершения async consumer path.

### Backend в Docker и Kafka недоступна

Проверьте:

- что backend-контейнер использует `kafka:19092`, а не `localhost:9092`;
- что поднят профиль `infra`;
- что порты `9092` и `8090` не заняты другими процессами.
