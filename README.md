# Quickmart MVP

Quickmart MVP — монорепозиторий сервиса быстрой доставки продуктов и товаров для дома.

## Что внутри

- `backend` — Kotlin + Spring Boot API
- `frontend` — React + TypeScript SPA
- `docs` — краткая документация по структуре проекта
- корневой `Gradle` — каноничный entrypoint для backend-задач

## Быстрый старт

### Docker Compose

```bash
docker compose up --build -d
```

После запуска:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Локальная разработка

Backend:

```bash
.\gradlew.bat :backend:bootRun
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Основные команды

Frontend:

```bash
cd frontend
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

## Структура репозитория

```text
project2/
  backend/             Spring Boot приложение
  frontend/            React SPA
  docs/                документация по устройству проекта
  build.gradle.kts     корневой Gradle entrypoint
  settings.gradle.kts  описание модулей Gradle
  docker-compose.yml   локальная инфраструктура
```

Подробно:

- [Структура проекта](docs/PROJECT_STRUCTURE.md)

## Seed-данные

Инициализируются миграцией `V2__seed_data.sql`:

- категории
- товары
- промокоды
- delivery slots
- стартовые остатки

## Важно

- backend больше не имеет отдельного Gradle entrypoint — используйте корневой `Gradle`
- frontend содержит только runtime-код и build tooling
- проект собирается только из runtime-кода и build tooling
