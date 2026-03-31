# Kafka-интеграция

## Назначение

Текущая Kafka-интеграция в Quickmart реализует сценарий `order lifecycle events -> async audit trail`.

Цель интеграции:

- публиковать доменные события жизненного цикла заказа без изменения основного синхронного checkout/order flow;
- обрабатывать эти события асинхронно отдельным consumer-ом;
- сохранять историю событий в БД для разработчиков, QA и админской диагностики.

Интеграция осознанно остается MVP-уровня:

- один основной topic;
- один consumer group;
- один полезный асинхронный эффект обработки: сохранение audit trail;
- Kafka остается опциональной и выключена по умолчанию.

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

Практически это работает так:

1. `CheckoutService` или `OrderService` завершают основную транзакцию по заказу.
2. Внутри бизнес-логики публикуется внутреннее Spring-событие через `OrderEventPublisher`.
3. `OrderKafkaPublisher` получает его только на фазе `AFTER_COMMIT`.
4. После успешного commit событие сериализуется в JSON и отправляется в Kafka topic `quickmart.order-events`.
5. `OrderEventAuditConsumer` читает сообщение из Kafka.
6. `OrderEventAuditService` идемпотентно сохраняет событие в таблицу `order_event_audit`.
7. История событий доступна через admin endpoint.

Ключевое правило: сообщение не отправляется в Kafka до фиксации бизнес-транзакции в БД.

## Конфигурация

### Backend

Основные параметры backend лежат в `app/backend/src/main/resources/application.yml`.

- `app.kafka.enabled`
  Значение по умолчанию: `false`.
  Если `false`, producer и consumer не поднимаются, а backend работает как раньше.
- `app.kafka.topic`
  Значение по умолчанию: `quickmart.order-events`.
- `app.kafka.consumer-group`
  Значение по умолчанию: `quickmart-order-audit`.
- `spring.kafka.bootstrap-servers`
  По умолчанию использует `localhost:9092` для локального запуска вне Docker.

Для Docker Compose backend получает:

- `APP_KAFKA_ENABLED`
- `KAFKA_BOOTSTRAP_SERVERS`

### Docker Compose

Kafka и Kafka UI входят только в профиль `infra`, поэтому обычный старт `core` не меняется.

Локальный запуск с Kafka:

```bash
$env:APP_KAFKA_ENABLED="true"
docker compose --profile core --profile infra up --build -d
```

Доступные сервисы:

- Backend API: [http://localhost:8080](http://localhost:8080)
- Kafka broker для локального клиента: `localhost:9092`
- Kafka UI: [http://localhost:8090](http://localhost:8090)

Дополнительные переменные:

- `KAFKA_PORT`
  Порт Kafka на хосте, по умолчанию `9092`.
- `KAFKA_UI_PORT`
  Порт Kafka UI на хосте, по умолчанию `8090`.

В compose Kafka настроена с двумя listeners:

- `kafka:19092` для контейнеров внутри docker network;
- `localhost:9092` для локальных клиентов с хоста.

Это важно, потому что и backend-контейнер, и Kafka UI должны подключаться к broker по сетевому имени `kafka`, а локальные утилиты и IDE-клиенты должны иметь рабочий `localhost`.

## Topic и формат события

### Основной topic

- `quickmart.order-events`

### Event envelope

Каждое сообщение публикуется в общем envelope-формате:

- `eventId`
- `eventType`
- `aggregateId`
- `occurredAt`
- `payloadVersion`
- `payload`

Текущее Kotlin-представление:

```kotlin
data class OrderLifecycleIntegrationEvent(
    val eventId: UUID,
    val eventType: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val payloadVersion: Int,
    val payload: OrderEventPayload,
)
```

`aggregateId` совпадает с `orderId`.

### Поддерживаемые типы событий

- `order.created`
- `order.cancelled`
- `order.status_changed`

### Payload

Полезная нагрузка описывает актуальное состояние заказа в момент публикации события и включает:

- `orderId`
- `userId`
- `previousStatus`
- `currentStatus`
- `paymentMethod`
- `paymentStatus`
- `total`
- `itemCount`
- `items`

Элементы `items` содержат:

- `productId`
- `productName`
- `quantity`

Пример сообщения:

```json
{
  "eventId": "4fcb1b6a-5a7d-4efc-a773-9d9cb9570b2e",
  "eventType": "order.created",
  "aggregateId": "80000000-0000-0000-0000-000000000001",
  "occurredAt": "2026-03-31T09:15:30Z",
  "payloadVersion": 1,
  "payload": {
    "orderId": "80000000-0000-0000-0000-000000000001",
    "userId": "10000000-0000-0000-0000-000000000001",
    "previousStatus": null,
    "currentStatus": "CREATED",
    "paymentMethod": "CARD",
    "paymentStatus": "PENDING",
    "total": 1490.00,
    "itemCount": 3,
    "items": [
      {
        "productId": "20000000-0000-0000-0000-000000000001",
        "productName": "Milk 3.2%",
        "quantity": 1
      }
    ]
  }
}
```

## Producer

Producer-связка состоит из двух частей:

- `OrderEventPublisher`
- `OrderKafkaPublisher`

`OrderEventPublisher` используется бизнес-сервисами и публикует внутренние Spring application events.

`OrderKafkaPublisher` подписывается на них через `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.

Что это дает:

- событие отправляется только после успешного commit;
- rollback бизнес-транзакции не приводит к отправке Kafka-сообщения;
- checkout и изменение статуса остаются синхронными и не превращаются в распределенную сагу.

Producer активен только если `app.kafka.enabled=true`.

## Consumer и идемпотентность

Kafka consumer реализован в `OrderEventAuditConsumer`.

Поведение consumer-а:

- читает сообщения из `quickmart.order-events`;
- использует consumer group `quickmart-order-audit`;
- десериализует JSON в `OrderLifecycleIntegrationEvent`;
- передает событие в `OrderEventAuditService`.

Идемпотентность реализована на уровне audit storage:

- в таблице `order_event_audit` поле `event_id` имеет `UNIQUE` constraint;
- при повторной обработке того же события сохранение вызовет `DataIntegrityViolationException`;
- `OrderEventAuditService` перехватывает это состояние и пропускает дубликат как штатный случай.

Для MVP этого достаточно, чтобы безопасно переживать повторную доставку уже записанных событий.

## Хранение audit trail

История событий хранится в таблице `order_event_audit`.

Схема хранения:

- `event_id`
- `order_id`
- `event_type`
- `payload_version`
- `occurred_at`
- `payload_json`
- `created_at`
- `updated_at`

Особенности:

- `order_id` связан с таблицей `orders`;
- индекс `(order_id, occurred_at)` ускоряет чтение истории заказа;
- payload хранится как сериализованный JSON, чтобы не плодить отдельные таблицы под каждый тип события.

## Admin endpoint для чтения истории

Для просмотра результата асинхронной обработки используется существующий admin endpoint:

```text
GET /api/admin/orders/{id}/events
```

Свойства endpoint:

- read-only;
- требует Bearer JWT с ролью `ADMIN`;
- возвращает историю событий заказа в хронологическом порядке;
- может вернуть пустой список, если Kafka отключена или событие еще не обработано consumer-ом.

Формат ответа:

- `eventId`
- `eventType`
- `aggregateId`
- `occurredAt`
- `payloadVersion`
- `payload`

## Kafka UI

### Зачем он добавлен

Kafka UI нужен для локальной/dev/test диагностики:

- быстро посмотреть topic;
- убедиться, что producer реально публикует сообщения;
- проверить, что consumer group существует и двигает offsets;
- сравнить Kafka-сообщения с тем, что попало в `order_event_audit`.

UI не нужен backend для работы и не должен считаться production-компонентом.

### Как поднять

```bash
$env:APP_KAFKA_ENABLED="true"
docker compose --profile core --profile infra up --build -d
```

После старта откройте:

- [http://localhost:8090](http://localhost:8090)

Если нужен другой порт:

```bash
$env:KAFKA_UI_PORT="18090"
docker compose --profile core --profile infra up --build -d
```

### Что видно в UI

В UI заранее настроен один локальный кластер:

- `quickmart-local`

Он подключается к broker-у по `kafka:19092`.

Основные места, которые полезны для проверки:

- `Topics`
  Здесь виден `quickmart.order-events`, количество partitions и список сообщений.
- `Consumers`
  Здесь видна группа `quickmart-order-audit`, ее offsets и lag.
- `Messages`
  Здесь можно открыть конкретный topic и посмотреть JSON event envelope.

UI оставлен в полном режиме управления для локальной среды. Это значит, что через него доступны не только просмотр, но и обычные UI-операции управления Kafka-объектами. Публиковать наружу такой UI нельзя.

### Ручная smoke-проверка

1. Поднимите `core + infra` и убедитесь, что backend стартовал с `APP_KAFKA_ENABLED=true`.
2. Откройте Kafka UI и проверьте, что виден кластер `quickmart-local`.
3. Перейдите в topic `quickmart.order-events`.
4. Выполните checkout через API или UI.
5. Убедитесь, что в topic появилось сообщение `order.created`.
6. Измените статус заказа или отмените его.
7. Убедитесь, что появились `order.status_changed` или `order.cancelled`.
8. Проверьте consumer group `quickmart-order-audit`.
9. Запросите `GET /api/admin/orders/{id}/events` и сверяйте историю с сообщениями в UI.

## Покрытие тестами

Для текущего Kafka MVP теперь зафиксированы отдельные документы:

- формализованная test matrix: `docs/KAFKA_TEST_CASES.md`
- профессиональный implementation prompt для дальнейших Kafka-задач: `docs/KAFKA_IMPLEMENTATION_PROMPT.md`

Backend-автотесты application-side Kafka-сценариев находятся в `app/backend/src/test/kotlin/com/quickmart/kafka`.

## Ограничения

Текущая интеграция осознанно ограничена:

- рассчитана на локальную/dev/test среду;
- использует single-broker Kafka setup;
- не включает production-hardening;
- не использует отдельную auth-защиту Kafka UI;
- не использует schema registry;
- не реализует outbox-таблицу и гарантированную доставку между БД и Kafka;
- хранит payload как JSON без отдельной схемы версионирования кроме `payloadVersion`.

Для production такой сетап нужно усиливать отдельно.

## Troubleshooting

### Kafka выключена

Симптомы:

- topic пустой;
- consumer group не появляется;
- `GET /api/admin/orders/{id}/events` возвращает пустую историю.

Проверьте:

- что выставлен `APP_KAFKA_ENABLED=true`;
- что backend действительно перезапущен после изменения переменной;
- что в логах backend нет строки о запуске с `app.kafka.enabled=false`.

### Kafka UI не видит кластер

Проверьте:

- что поднят профиль `infra`;
- что контейнер `quickmart-kafka` находится в состоянии `Up`;
- что контейнер `quickmart-kafka-ui` находится в состоянии `Up`;
- что `docker compose --profile core --profile infra config` содержит сервис `kafka-ui`.

### Broker недоступен

Проверьте:

- что порт `9092` не занят другим процессом;
- что `KAFKA_PORT` не переназначен неожиданным значением;
- что backend в Docker использует `kafka:19092`, а не `localhost:9092`.

Если broker стартовал, но контейнерные клиенты не видят его, первым делом проверьте advertised listeners в `docker-compose.yml`.

### Сообщения есть в topic, но audit trail пустой

Проверьте:

- существует ли consumer group `quickmart-order-audit` в Kafka UI;
- нет ли ошибок десериализации в логах backend;
- нет ли ошибок записи в БД;
- действительно ли запрос идет к правильному `orderId`.

Также учитывайте, что между публикацией и чтением есть небольшая асинхронная задержка.

### Consumer не читает сообщения

Проверьте:

- что backend запущен с включенной Kafka;
- что topic называется именно `quickmart.order-events`;
- что consumer group не зависла на старом окружении;
- что в логах backend есть записи обработки событий;
- что Kafka UI показывает движение offsets.

## Где смотреть код

- `app/backend/src/main/kotlin/com/quickmart/events`
- `app/backend/src/main/kotlin/com/quickmart/kafka`
- `app/backend/src/main/kotlin/com/quickmart/service/OrderEventAuditService.kt`
- `app/backend/src/main/kotlin/com/quickmart/controller/admin/AdminOrderController.kt`
- `app/backend/src/main/resources/db/migration/V4__order_event_audit.sql`
