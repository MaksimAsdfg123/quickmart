# Структура проекта

## Верхний уровень

```text
project2/
  app/
  tests/
  docs/
  build.gradle.kts
  settings.gradle.kts
  docker-compose.yml
```

## app

`app` содержит весь runtime-код проекта.

### `app/backend`

Spring Boot backend.

- `src/main/kotlin/com/quickmart/config` — инфраструктурная и Spring-конфигурация
- `src/main/kotlin/com/quickmart/controller` — публичные HTTP endpoints
- `src/main/kotlin/com/quickmart/controller/admin` — административные endpoints
- `src/main/kotlin/com/quickmart/domain` — сущности и enum-ы домена
- `src/main/kotlin/com/quickmart/dto` — request/response DTO
- `src/main/kotlin/com/quickmart/exception` — обработка ошибок
- `src/main/kotlin/com/quickmart/mapper` — преобразование entity ↔ DTO
- `src/main/kotlin/com/quickmart/repository` — доступ к данным
- `src/main/kotlin/com/quickmart/security` — JWT и авторизация
- `src/main/kotlin/com/quickmart/service` — бизнес-логика
- `src/main/resources/db/migration` — миграции базы данных

### `app/frontend`

React frontend.

- `src/api` — HTTP-клиенты к backend
- `src/app` — корневое приложение и роутинг
- `src/pages` — экранные компоненты
- `src/pages/admin` — административные экраны
- `src/shared/components` — layout и общие компоненты
- `src/shared/components/ui` — базовые UI primitives
- `src/shared/lib` — утилиты и клиентская логика
- `src/shared/types` — общие типы

## tests

`tests` — пока только каркас под будущие автотесты.

### `tests/backend`

- `api` — будущие API/contract тесты
- `integration` — будущие интеграционные тесты
- `unit` — будущие unit-тесты
- `support` — будущий общий test support

### `tests/frontend`

- `e2e` — будущие UI end-to-end сценарии
- `fixtures` — будущие фикстуры
- `pages` — будущие page objects
- `support` — будущие helper-утилиты

## Структурные правила

- новый runtime-код добавляется только в `app/backend` или `app/frontend`
- `tests/` не должен смешиваться с runtime-кодом
- корневой `Gradle` остается единым entrypoint для backend
