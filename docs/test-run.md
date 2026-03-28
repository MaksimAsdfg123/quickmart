# Запуск backend API-тестов (`:api-tests`)

## Предусловия
- Backend запущен и доступен по `API_BASE_URL` (по умолчанию `http://127.0.0.1:8080`).
- Миграции и seed-данные применены.
- Java 21 установлена.

## Конфигурация
- Общий файл: `tests/config/test-environment.properties`.
- Backend test resources: `tests/backend/resources`.

Приоритет значений:
1. Переменные окружения
2. `test-environment.properties`
3. Fallback в коде

Ключевые env-переменные:
- `API_BASE_URL`
- `API_CONNECT_TIMEOUT_MS`
- `API_READ_TIMEOUT_MS`
- `AUTH_ADMIN_EMAIL`
- `AUTH_ADMIN_PASSWORD`
- `AUTH_CUSTOMER_EMAIL`
- `AUTH_CUSTOMER_PASSWORD`

## Команды

### Компиляция тестов
```bash
.\gradlew.bat :api-tests:compileTestKotlin
```

### Только API-suite
```bash
.\gradlew.bat :api-tests:apiTest
```

### Полный backend test layer
```bash
.\gradlew.bat :api-tests:test
```

### Запуск через root shortcut
```bash
.\gradlew.bat apiTest
```

## Parallel execution
- Настройки JUnit 5 лежат в `tests/backend/resources/junit-platform.properties`.
- Включен concurrent запуск классов и методов.
- Для безопасного параллелизма тесты обязаны использовать независимые тестовые данные.
- Task `:api-tests:apiTest` overrides JUnit parallel mode to `false` so HTTP traces are attached to each individual test case in `Test Results`.

## Allure
- Конфиг: `tests/backend/resources/allure.properties`
- Результаты: `tests/backend/build/allure-results`

Пример генерации отчета:
```bash
allure generate tests/backend/build/allure-results --clean -o tests/backend/build/allure-report
allure open tests/backend/build/allure-report
```

## Как добавить новый API-тест
1. Добавить/обновить client в `tests/backend/shared/kotlin/com/quickmart/test/shared/clients`.
2. Добавить доменный сценарий и assertions в `tests/backend/shared/kotlin/com/quickmart/test/shared/<domain>/...`.
3. Добавить тест в `tests/backend/suites/kotlin/com/quickmart/test/suites/api/<domain>`.
4. Запустить `:api-tests:apiTest`.

