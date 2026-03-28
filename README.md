# Quickmart MVP

Quickmart MVP — монорепозиторий сервиса быстрой доставки продуктов и товаров для дома.

## Простая структура

```text
project2/
  app/
    backend/    Kotlin + Spring Boot API
    frontend/   React + TypeScript SPA
  tests/        API и UI автотесты (Kotlin)
  docs/         краткая документация
  build.gradle.kts
  settings.gradle.kts
  docker-compose.yml
```

## Быстрый старт

### Docker Compose

```bash
docker compose up --build -d
```

После запуска:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Локальный запуск

Backend:

```bash
.\gradlew.bat :backend:bootRun
```

Frontend:

```bash
cd app/frontend
npm install
npm run dev
```

## Основные команды

Frontend:

```bash
cd app/frontend
npm run build
```

Backend:

```bash
.\gradlew.bat :backend:bootRun
.\gradlew.bat :backend:bootJar
```

## Демо-учетки

- `admin@quickmart.local` / `password`
- `anna@example.com` / `password`
- `ivan@example.com` / `password`

## Seed-данные

Инициализируются миграцией `V2__seed_data.sql`:

- категории
- товары
- промокоды
- delivery slots
- стартовые остатки

## Документация

- [Структура проекта](docs/PROJECT_STRUCTURE.md)

## Важно

- runtime-код лежит только в `app/backend` и `app/frontend`
- тестовые модули: `:api-tests` (`tests/backend`) и `:ui-tests` (`tests/frontend`)
- корневой `Gradle` остается entrypoint для backend и тестовых задач (`apiTest`, `uiTest`)
