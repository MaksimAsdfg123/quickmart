# UI Test Architecture

## Назначение
Модуль `:ui-tests` реализует production-like UI automation для блока аутентификации (login/register/logout/protected access) на Kotlin + Playwright.

## Физическая структура (suite + shared)
```text
tests/
  config/
    test-environment.properties
  frontend/
    suites/
      kotlin/com/quickmart/test/suites/ui/auth/
        AuthenticationUiTest.kt
    shared/
      kotlin/com/quickmart/test/shared/ui/
        api/
        assertions/
        base/
        components/
        config/
        constants/
        data/
        fixtures/
        flows/
        helpers/
        listeners/
        pages/
    resources/
      allure.properties
      junit-platform.properties
      logback-test.xml
```

## Структура слоев
- `config` — централизованная загрузка окружения и runtime-настроек.
- `base` — жизненный цикл Playwright (`BrowserContext`, `Page`), trace/video/screenshot hooks.
- `fixtures` — подготовка анонимной и авторизованной сессии.
- `api` — API-хелпер для подготовки данных (login/register) без UI-шага.
- `pages` — page objects для страниц auth и orders.
- `components` — переиспользуемые UI-компоненты (`TopBarComponent`).
- `flows` — бизнес-последовательности пользовательских действий.
- `assertions` — доменные проверки результата.
- `data` — фабрики и модели тестовых данных.
- `listeners` — diagnostics listeners (network logging).
- `suites` — короткие сценарные тесты.

## Политика config/resources
- `tests/config` содержит только кросс-проектный контракт окружения (`test-environment.properties`).
- `tests/frontend/resources` содержит только UI-модульные runtime-файлы (`allure.properties`, `junit-platform.properties`, `logback-test.xml`).
- Дублирование в `resources` между backend и ui не допускается без явной причины.

## Принципы
- Тесты не содержат raw selectors и длинных технических шагов.
- Один тест = один читаемый пользовательский сценарий.
- Состояние между тестами изолировано отдельным `BrowserContext`.
- Mutable test data создаются уникально для parallel-safe запуска.

## Артефакты
Хранятся в `tests/frontend/artifacts`:
- `screenshots` — скриншот при падении;
- `videos` — видео выполнения теста;
- `traces` — Playwright trace (`.zip`);
- `logs` — network diagnostics (`*-network.json`).

## Allure
- Ключевые действия оформлены через `Allure.step(...)`.
- В отчет прикладываются screenshot/trace/video/network log.
- Для тестов используются метки: `Epic`, `Feature`, `Story`, `Severity`, `Owner`, `layer=ui`.
