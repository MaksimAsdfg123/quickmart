# Kafka Test Cases

Этот документ фиксирует полный набор тест-кейсов для текущего Kafka MVP в Quickmart. Он покрывает application-side сценарии, которые автоматизируются в backend, и manual smoke-проверки для Docker/Kafka UI.

## Flow

### KAFKA-FLOW-001
- Goal: подтвердить, что успешный checkout публикует `order.created`, а consumer сохраняет audit trail.
- Preconditions: `app.kafka.enabled=true`, broker доступен, у пользователя есть активная корзина с валидным товаром и слотом доставки.
- Steps:
  1. Выполнить checkout.
  2. Дождаться обработки Kafka consumer-ом.
  3. Прочитать `order_event_audit` по `orderId`.
- Expected result:
  1. В audit trail есть одна запись.
  2. `eventType=order.created`.
  3. `orderId` и aggregate совпадают с созданным заказом.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-FLOW-002
- Goal: подтвердить публикацию follow-up события при valid status transition `CREATED -> CONFIRMED`.
- Preconditions: существует заказ после успешного checkout.
- Steps:
  1. Выполнить `updateStatus(orderId, CONFIRMED)`.
  2. Дождаться обработки consumer-ом.
  3. Прочитать историю событий заказа.
- Expected result:
  1. История содержит `order.created`, затем `order.status_changed`.
  2. В payload второго события `previousStatus=CREATED`, `currentStatus=CONFIRMED`.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-FLOW-003
- Goal: подтвердить публикацию `order.cancelled` при customer cancellation.
- Preconditions: существует заказ в статусе `CONFIRMED`, принадлежащий пользователю.
- Steps:
  1. Подтвердить заказ.
  2. Выполнить `cancelMyOrder`.
  3. Дождаться обработки consumer-ом.
- Expected result:
  1. История содержит `order.cancelled`.
  2. В payload `previousStatus=CONFIRMED`, `currentStatus=CANCELLED`.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-FLOW-004
- Goal: подтвердить публикацию `order.cancelled` при admin cancellation через status update.
- Preconditions: существует заказ в статусе `CONFIRMED`.
- Steps:
  1. Подтвердить заказ.
  2. Выполнить `updateStatus(orderId, CANCELLED)`.
  3. Дождаться обработки consumer-ом.
- Expected result:
  1. В истории появляется `order.cancelled`.
  2. Статус заказа становится `CANCELLED`.
  3. Если оплата была `PENDING`, статус оплаты в payload отражает отмену.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-FLOW-005
- Goal: подтвердить, что для заказа с `CASH` финальная доставка публикует событие с `paymentStatus=PAID`.
- Preconditions: существует cash-order после checkout.
- Steps:
  1. Последовательно перевести заказ в `CONFIRMED`, `ASSEMBLING`, `OUT_FOR_DELIVERY`, `DELIVERED`.
  2. Дождаться обработки consumer-ом.
  3. Прочитать последний audit entry.
- Expected result:
  1. Последнее событие имеет тип `order.status_changed`.
  2. В payload `currentStatus=DELIVERED`.
  3. В payload `paymentStatus=PAID`.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

## Payload / Audit

### KAFKA-PAYLOAD-001
- Goal: подтвердить корректность полного event envelope.
- Preconditions: успешный checkout с включенной Kafka.
- Steps:
  1. Выполнить checkout.
  2. Получить первую audit entry.
- Expected result:
  1. Заполнены `eventId`, `eventType`, `aggregateId/orderId`, `occurredAt`, `payloadVersion`, `payload`.
  2. `payloadVersion=1`.
  3. `aggregateId` соответствует заказу.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-PAYLOAD-002
- Goal: подтвердить, что payload отражает фактические данные заказа.
- Preconditions: успешный checkout с известным товаром, количеством и оплатой.
- Steps:
  1. Выполнить checkout.
  2. Получить payload из audit entry.
- Expected result:
  1. `currentStatus`, `paymentMethod`, `paymentStatus`, `total` корректны.
  2. `itemCount` совпадает с суммарным количеством в заказе.
  3. `items[].productId/productName/quantity` совпадают со snapshot заказа.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest`.

### KAFKA-AUDIT-001
- Goal: подтвердить, что история событий возвращается в ожидаемом порядке.
- Preconditions: по одному заказу произошло несколько доменных событий.
- Steps:
  1. Создать заказ.
  2. Выполнить follow-up действия.
  3. Прочитать историю событий по orderId.
- Expected result:
  1. События упорядочены хронологически.
  2. Последовательность соответствует реальному flow заказа.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFlowIntegrationTest` и `OrderEventAuditServiceTest`.

### KAFKA-AUDIT-002
- Goal: подтвердить корректную работу read model `getByOrderId()`.
- Preconditions: в `order_event_audit` уже сохранены записи по заказу.
- Steps:
  1. Сохранить минимум два события по одному orderId.
  2. Вызвать `OrderEventAuditService.getByOrderId(orderId)`.
- Expected result:
  1. Возвращается список в правильном порядке.
  2. `payload` корректно десериализуется в JSON.
  3. Поля в `payload` доступны и соответствуют сохраненным данным.
- Automation status: `AUTO`
- Notes: покрывается `OrderEventAuditServiceTest`.

### KAFKA-AUDIT-003
- Goal: подтвердить корректную ошибку при запросе истории несуществующего заказа.
- Preconditions: orderId отсутствует в БД.
- Steps:
  1. Вызвать `OrderEventAuditService.getByOrderId(randomUuid)`.
- Expected result:
  1. Бросается `NotFoundException`.
  2. Сообщение ошибки соответствует доменному контракту.
- Automation status: `AUTO`
- Notes: покрывается `OrderEventAuditServiceTest`.

## Transactional / Negative

### KAFKA-TX-001
- Goal: подтвердить, что empty cart не приводит к Kafka side effects.
- Preconditions: у пользователя есть активная, но пустая корзина.
- Steps:
  1. Выполнить checkout.
  2. Получить ожидаемую бизнес-ошибку.
  3. Проверить состояние audit storage.
- Expected result:
  1. Бросается `BusinessException`.
  2. Новых audit entries не появляется.
  3. Новый заказ не сохраняется.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFailureIntegrationTest`.

### KAFKA-TX-002
- Goal: подтвердить, что failed `MOCK_ONLINE` payment не публикует событие.
- Preconditions: заказ сформирован так, чтобы итоговая сумма была больше порога отказа онлайн-оплаты.
- Steps:
  1. Выполнить checkout с `MOCK_ONLINE`.
  2. Получить ожидаемую ошибку оплаты.
  3. Проверить состояние audit storage и orders.
- Expected result:
  1. Бросается `BusinessException`.
  2. Kafka/audit side effects отсутствуют.
  3. Заказ не коммитится.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFailureIntegrationTest`.

### KAFKA-TX-003
- Goal: подтвердить, что invalid status transition не публикует новое follow-up событие.
- Preconditions: существует заказ в статусе `CREATED`, его initial `order.created` уже обработан.
- Steps:
  1. Попытаться перевести заказ в недопустимый статус.
  2. Получить ожидаемую ошибку.
  3. Проверить историю событий заказа.
- Expected result:
  1. Бросается `BusinessException`.
  2. Количество audit entries не увеличивается.
  3. В истории нет лишнего follow-up event.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaFailureIntegrationTest`.

## Idempotency / Resilience

### KAFKA-IDEMP-001
- Goal: подтвердить идемпотентность audit storage по `eventId`.
- Preconditions: существует orderId, по которому можно сохранить audit event.
- Steps:
  1. Дважды вызвать `record()` с одним и тем же `eventId`.
  2. Прочитать audit записи по заказу.
- Expected result:
  1. Сохраняется только одна запись.
  2. Duplicate processing не ломает сервис.
- Automation status: `AUTO`
- Notes: покрывается `OrderEventAuditServiceTest`.

### KAFKA-CONFIG-001
- Goal: подтвердить, что при выключенной Kafka основная бизнес-логика продолжает работать.
- Preconditions: `app.kafka.enabled=false`.
- Steps:
  1. Выполнить checkout.
  2. Выполнить valid status update и отмену.
  3. Проверить историю audit trail.
- Expected result:
  1. Checkout и follow-up операции успешны.
  2. Audit trail остается пустым.
  3. Runtime не зависит от поднятого broker.
- Automation status: `AUTO`
- Notes: покрывается `OrderKafkaDisabledIntegrationTest`.

## Manual Smoke

### KAFKA-SMOKE-001
- Goal: подтвердить, что Kafka UI видит локальный кластер.
- Preconditions: подняты `core + infra`, `APP_KAFKA_ENABLED=true`.
- Steps:
  1. Открыть Kafka UI.
  2. Проверить список кластеров.
- Expected result:
  1. Видно `quickmart-local`.
  2. Статус кластера `ONLINE`.
- Automation status: `MANUAL`
- Notes: выполняется при локальном Docker smoke.

### KAFKA-SMOKE-002
- Goal: подтвердить, что topic отображается в Kafka UI.
- Preconditions: Kafka UI и broker доступны.
- Steps:
  1. Открыть раздел `Topics`.
  2. Найти `quickmart.order-events`.
- Expected result:
  1. Topic виден в UI.
  2. Partition/replication отображаются корректно.
- Automation status: `MANUAL`
- Notes: выполняется через Kafka UI.

### KAFKA-SMOKE-003
- Goal: подтвердить, что consumer group отображается в UI.
- Preconditions: backend запущен с включенной Kafka, consumer инициализирован.
- Steps:
  1. Открыть раздел `Consumers`.
  2. Найти `quickmart-order-audit`.
- Expected result:
  1. Consumer group отображается в UI.
  2. Видны offsets и/или lag.
- Automation status: `MANUAL`
- Notes: выполняется через Kafka UI после старта backend.

### KAFKA-SMOKE-004
- Goal: подтвердить соответствие сообщений в topic и admin history endpoint.
- Preconditions: выполнен хотя бы один checkout и одно follow-up действие.
- Steps:
  1. Открыть `quickmart.order-events` в Kafka UI.
  2. Скопировать `orderId` и типы сообщений.
  3. Запросить `GET /api/admin/orders/{id}/events`.
- Expected result:
  1. Типы событий совпадают.
  2. Хронология в UI и audit trail согласована.
  3. Payload логически соответствует данным в admin endpoint.
- Automation status: `MANUAL`
- Notes: manual correlation check для локальной/dev/test среды.
