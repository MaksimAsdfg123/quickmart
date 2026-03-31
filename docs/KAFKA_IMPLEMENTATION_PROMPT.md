# Kafka Implementation Prompt

Этот документ содержит готовый prompt для будущих доработок Kafka-функционала в Quickmart. Он написан так, чтобы исполнитель получил production-grade рамки, понятный quality bar и заранее зафиксированную acceptance matrix.

## Готовый prompt

```text
Работай в репозитории `C:\Users\maksi\IdeaProjects\project2`.

Ты дорабатываешь уже существующую Kafka-интеграцию Quickmart. Это не greenfield-задача: сначала изучи текущий код, только потом вноси изменения.

Контекст проекта:
- Монорепозиторий Quickmart.
- Backend: Kotlin + Spring Boot 3.3 + JPA + PostgreSQL, модуль `app/backend`.
- Frontend: React + TypeScript, модуль `app/frontend`.
- Kafka уже интегрирована в backend как MVP.
- Kafka UI уже существует для локальной/dev/test среды.
- Текущий выбранный бизнес-кейс: `order lifecycle events -> async audit trail`.
- Runtime-контракт уже существует и ломать его нельзя без очень веской причины.

Неподлежащие изменению контракты:
- topic по умолчанию: `quickmart.order-events`
- event envelope:
  - `eventId`
  - `eventType`
  - `aggregateId`
  - `occurredAt`
  - `payloadVersion`
  - `payload`
- admin endpoint истории событий:
  - `GET /api/admin/orders/{id}/events`
- публикация событий должна оставаться только `after commit`
- Kafka должна оставаться опциональной и выключенной по умолчанию

Архитектурные ограничения:
- Не превращай checkout в distributed saga.
- Не внедряй outbox, orchestration layer, schema registry, event bus abstraction или другую “platform” сложность, если это не требуется явно.
- Не ломай существующие REST endpoints и бизнес-логику checkout/order flow.
- Не делай Kafka обязательной частью обычного запуска `core`.
- Сохраняй минимальную инвазивность: усиливай текущую реализацию, а не переписывай проект вокруг Kafka.

Обязательный порядок работы:
1. Сначала изучи текущую реализацию Kafka, checkout, orders, audit trail, docker compose и тесты.
2. Кратко зафиксируй baseline: что уже есть, где риски, что именно собираешься менять.
3. Вноси только те изменения, которые улучшают текущий Kafka MVP.
4. После изменений обязательно прогони релевантные проверки и укажи точные команды.

Что считается хорошим результатом:
- production-style naming и структура кода;
- ясные, поддерживаемые решения без лишней enterprise-сложности;
- отсутствие регрессий в существующем backend-поведении;
- качественная техдокументация для разработчиков и QA;
- полный backend-набор автотестов по application-side Kafka-сценариям;
- явная проверка negative/rollback/idempotency сценариев, а не только happy path.

Обязательные deliverables:
- code changes, если они нужны по задаче;
- Kafka config / docker-compose changes, если они нужны по задаче;
- Kafka UI changes, если они нужны по задаче;
- техдокументация в `docs/`;
- backend tests в `app/backend/src/test/kotlin/com/quickmart/kafka`;
- финальный отчет с командами проверки и остаточными ограничениями.

Acceptance matrix, которую нельзя игнорировать:

| ID | Сценарий | Что обязательно подтвердить |
| --- | --- | --- |
| `KAFKA-FLOW-001` | checkout -> `order.created` | событие публикуется и consumer сохраняет audit entry |
| `KAFKA-FLOW-002` | `CREATED -> CONFIRMED` | публикуется `order.status_changed` с корректными `previousStatus/currentStatus` |
| `KAFKA-FLOW-003` | customer cancel | публикуется `order.cancelled` |
| `KAFKA-FLOW-004` | admin cancel via status update | публикуется `order.cancelled` |
| `KAFKA-FLOW-005` | cash order delivered | follow-up event содержит `paymentStatus=PAID` |
| `KAFKA-PAYLOAD-001` | envelope correctness | поля envelope заполнены корректно |
| `KAFKA-PAYLOAD-002` | payload correctness | `items`, `itemCount`, `payment*`, `status*`, `total` согласованы |
| `KAFKA-AUDIT-001` | ordered history | audit trail возвращается в корректной последовательности |
| `KAFKA-TX-001` | empty cart | exception + отсутствие Kafka/audit side effects |
| `KAFKA-TX-002` | failed online payment | rollback + отсутствие Kafka/audit side effects |
| `KAFKA-TX-003` | invalid status transition | exception + отсутствие нового follow-up event |
| `KAFKA-IDEMP-001` | duplicate processing | duplicate event не создает вторую audit entry |
| `KAFKA-AUDIT-002` | audit read model | `getByOrderId()` корректно маппит payload |
| `KAFKA-AUDIT-003` | missing order | `getByOrderId()` возвращает корректную ошибку |
| `KAFKA-CONFIG-001` | Kafka disabled | бизнес-логика работает, audit trail пуст |

Ожидания по тестам:
- Не ограничивайся одним integration happy path.
- Покрой flow, payload, rollback/no-publish, disabled mode, audit read model и idempotency.
- Предпочитай backend-local тесты с `@SpringBootTest`, `EmbeddedKafka` и сервисными тестами без лишней инфраструктуры, если broker не нужен.
- Не добавляй дублирующий API-suite в `tests/backend`, если это не требуется явно.

Ожидания по документации:
- Любая нетривиальная Kafka-доработка должна сопровождаться обновлением `docs/KAFKA_INTEGRATION.md`.
- Если меняется test matrix или правила выполнения Kafka-работ, обновляй и специализированные документы в `docs/`.
- Пиши доки на русском языке, если задача не требует иного.

Команды проверки по умолчанию:
- `.\gradlew.bat :backend:test --tests "com.quickmart.kafka.*"`
- `.\gradlew.bat :backend:test`
- `.\gradlew.bat :backend:bootJar`
- при необходимости: `docker compose --profile core --profile infra config`

Формат финального отчета:
1. Что изменено.
2. Какие кейсы покрыты тестами.
3. Какие команды проверки запускались.
4. Какие ограничения или follow-up улучшения остались.

Не считай задачу завершенной, если:
- acceptance matrix покрыта только частично;
- negative cases не проверены;
- after-commit semantics не подтверждена;
- документация и тесты не приведены в состояние, пригодное для передачи другому инженеру.
```
