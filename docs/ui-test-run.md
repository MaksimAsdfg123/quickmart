# UI Test Run Guide

## Предусловия
- Backend запущен и доступен по `API_BASE_URL`.
- Frontend запущен и доступен по `UI_BASE_URL`.
- Установлена Java 21.

## Конфигурация
Общий файл: `tests/config/test-environment.properties`.
UI-модуль читает значения в порядке: `env vars -> tests/config/test-environment.properties -> fallback в коде`.

Ключи:
- `UI_BASE_URL`
- `API_BASE_URL`
- `E2E_CUSTOMER_EMAIL` / `E2E_CUSTOMER_PASSWORD`
- `E2E_ADMIN_EMAIL` / `E2E_ADMIN_PASSWORD`
- `UI_HEADLESS`
- `UI_BROWSER`

## Команды
Установка браузеров Playwright:
```bash
./gradlew installUiBrowsers
```

Запуск UI suite:
```bash
./gradlew uiTest
```

Запуск напрямую модулем:
```bash
./gradlew :ui-tests:uiTest
```

Headed запуск:
```bash
UI_HEADLESS=false ./gradlew :ui-tests:uiTest
```

CI-профиль (через переменную CI=true):
- retries автоматически увеличиваются до 2.

## Allure
Результаты:
- `tests/frontend/build/allure-results`

Генерация отчета:
```bash
allure generate tests/frontend/build/allure-results --clean -o tests/frontend/build/allure-report
allure open tests/frontend/build/allure-report
```

## Retry
Retry централизован в `tests/frontend/build.gradle.kts` через `org.gradle.test-retry`.
- local: 1 retry;
- CI: 2 retries.

Retry используется только как защита от эпизодических факторов, не как замена стабильности тестов.

## Parallel-safe правила
- каждый тест запускается в отдельном `BrowserContext`;
- тесты используют уникальные данные регистрации;
- общий mutable seed-state не используется для mutating сценариев.

## Failure diagnostics
При падении доступны:
- screenshot: `tests/frontend/artifacts/screenshots/*.png`;
- trace: `tests/frontend/artifacts/traces/*.zip`;
- video: `tests/frontend/artifacts/videos/*.webm`;
- network log: `tests/frontend/artifacts/logs/*-network.json`.

Открыть trace:
```bash
npx playwright show-trace tests/frontend/artifacts/traces/<trace-file>.zip
```

Для расследования в первую очередь проверяются:
1. шаг Allure, на котором произошел fail;
2. network log (ошибочные `/api/*` ответы);
3. screenshot + video;
4. trace timeline.
