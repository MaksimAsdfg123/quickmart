# Структура проекта

## Каноничные entrypoint-ы

- Gradle-задачи запускаются из корня репозитория
- frontend development и build запускаются из `frontend`
- runtime-код хранится только в `backend` и `frontend`

## Дерево верхнего уровня

```text
project2/
  backend/
  frontend/
  docs/
  docker-compose.yml
  build.gradle.kts
  settings.gradle.kts
```

## backend

`backend` — единственный JVM runtime-модуль.

### `src/main/kotlin/com/quickmart`

- `config` — Spring и инфраструктурная конфигурация
- `controller` — публичные HTTP endpoints
- `controller/admin` — административные endpoints
- `domain/entity` — JPA-сущности
- `domain/enums` — бизнес-статусы и справочники
- `dto` — request/response модели API
- `exception` — формат ошибок и глобальная обработка исключений
- `mapper` — преобразование entity ↔ DTO
- `repository` — JPA repositories и custom repository code
- `security` — JWT, аутентификация и авторизация
- `service` — бизнес-логика и orchestration use-cases

### `src/main/resources`

- `application.yml` — runtime-конфигурация backend
- `db/migration` — Flyway migrations

## frontend

`frontend/src` разделен по назначению и содержит только рабочие слои приложения.

- `api` — HTTP-клиенты и запросы к backend
- `app` — корневое приложение и роутинг
- `pages` — page-level screens
- `pages/admin` — административные экраны
- `shared/components` — layout, guards и общие React-компоненты
- `shared/components/ui` — базовые UI primitives
- `shared/lib` — клиентские утилиты и shared state helpers
- `shared/types` — общие frontend-типы

## docs

- `PROJECT_STRUCTURE.md` — карта проекта и роли каталогов

## Структурные правила

- не добавлять runtime-код вне `backend` и `frontend`
- не создавать второй Gradle entrypoint для backend
- не хранить build artifacts в git
- не создавать пустые архитектурные каталоги "на будущее"
