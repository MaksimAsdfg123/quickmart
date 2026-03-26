# Запуск API automation

## Предусловия

- Backend запущен и доступен по `API_BASE_URL` (по умолчанию `http://127.0.0.1:8080`).
- В БД загружены миграции и seed-данные (`admin@quickmart.local`, `anna@example.com`).
- Java 21 установлена.

## Конфигурация окружения

Общий файл: `tests/config/test-environment.properties`.

Поддерживается переопределение через переменные окружения:

- `API_BASE_URL`
- `API_CONNECT_TIMEOUT_MS`
- `API_READ_TIMEOUT_MS`
- `AUTH_ADMIN_EMAIL`
- `AUTH_ADMIN_PASSWORD`
- `AUTH_CUSTOMER_EMAIL`
- `AUTH_CUSTOMER_PASSWORD`

Приоритет: `ENV -> test-environment.properties -> fallback`.

## Команды запуска

### Локальный запуск API-suite

```bash
.\gradlew.bat :backend:apiTest
```

### Полный backend test task

```bash
.\gradlew.bat :backend:test
```

### CI запуск (пример)

```bash
./gradlew :backend:apiTest --no-daemon --stacktrace
```

## Parallel execution

Файл: `tests/backend/resources/junit-platform.properties`.

Включено:

- `junit.jupiter.execution.parallel.enabled=true`
- concurrent режим для методов и классов.

Требования для безопасного параллельного прогона:

- использовать уникальные тестовые данные;
- не переиспользовать изменяемые сущности между тестами;
- не опираться на порядок выполнения тестов.

## Allure отчет

### Генерация результатов

Результаты сохраняются в:

`app/backend/build/allure-results`

через `tests/backend/resources/allure.properties`.

### Построение отчета

Если установлен Allure CLI:

```bash
allure generate app/backend/build/allure-results --clean -o app/backend/build/allure-report
allure open app/backend/build/allure-report
```

## Failure diagnostics

На каждом HTTP вызове автоматически прикладываются:

- attachment `HTTP Request: <METHOD> <URL>`
  - method, URL, headers, query params, path params, body;
- attachment `HTTP Response: <STATUS> <METHOD> <URL>`
  - status, headers, response body, response time.

Для error-case дополнительно прикладывается:

- attachment `Ошибка API` с raw JSON ответа ошибки.

Это позволяет расследовать падения без ручного включения debug-логов и без повторного прогона для сбора transport-контекста.
