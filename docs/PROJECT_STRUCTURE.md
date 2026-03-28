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

Gradle modules:
- `:backend` -> `app/backend`
- `:api-tests` -> `tests/backend`
- `:ui-tests` -> `tests/frontend`

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

`tests` содержит отдельные модули API и UI автотестов.

### `tests/backend`

- `build.gradle.kts` — сборка модуля `:api-tests`
- `suites` — сценарные API suite-тесты
- `shared` — переиспользуемая тестовая инфраструктура и хелперы
- `resources` — backend test resources (`allure.properties`, `application-test.yml`, `junit-platform.properties`)

### `tests/frontend`

- `build.gradle.kts` — сборка модуля `:ui-tests`
- `suites` — UI suite-тесты
- `shared` — page objects, flows, fixtures, listeners и helpers
- `resources` — UI test resources (`allure.properties`, `junit-platform.properties`, `logback-test.xml`)

## Структурные правила

- новый runtime-код добавляется только в `app/backend` или `app/frontend`
- `tests/` не должен смешиваться с runtime-кодом
- корневой `Gradle` остается единым entrypoint для backend и тестовых задач
