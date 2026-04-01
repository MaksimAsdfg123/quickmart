# UI tests

## Назначение

Модуль `:ui-tests` (`tests/frontend`) содержит Playwright-based UI automation на Kotlin. Текущий scope этого слоя - auth/access scenarios: логин, регистрация, logout, duplicate registration и поведение protected routes.

Базовый стандарт для всех новых UI автотестов зафиксирован в [test-authoring-rules.md](test-authoring-rules.md). Текущий документ описывает только устройство и scope модуля `:ui-tests`.

## Структура модуля

```text
tests/frontend/
  build.gradle.kts
  suites/kotlin/com/quickmart/test/suites/ui/
    auth/
      AuthenticationUiTest.kt
  shared/kotlin/com/quickmart/test/shared/ui/
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
  ../config/
    test-environment.properties
```

## Gradle tasks

| Task | Назначение |
| --- | --- |
| `:ui-tests:installUiBrowsers` / root `installUiBrowsers` | установка браузеров Playwright |
| `:ui-tests:uiSmokeTest` / root `uiSmokeTest` | smoke subset текущего UI suite |
| `:ui-tests:uiTest` / root `uiTest` | полный текущий UI suite |
| `:ui-tests:uiTestHeaded` / root `uiTestHeaded` | headed run c `UI_HEADLESS=false` |
| `:ui-tests:uiTestDebug` | headed debug run c `DEBUG=pw:api` и `UI_SLOW_MO_MS` |

Пример debug запуска одного теста:

```powershell
$env:UI_SLOW_MO_MS="500"
.\gradlew.bat :ui-tests:uiTestDebug --tests com.quickmart.test.suites.ui.auth.AuthenticationUiTest.shouldLoginCustomerSuccessfully
```

## Текущий suite inventory

`AuthenticationUiTest` покрывает:

- login customer;
- login admin;
- redirect на login для anonymous user и возврат после входа;
- успешную регистрацию нового пользователя;
- logout и потерю доступа к protected routes;
- ошибку при неверном пароле;
- валидацию login/register forms;
- ошибку при duplicate registration.

## Patterns и слои

- `pages` - page objects, инкапсулирующие локаторы и атомарные действия;
- `components` - переиспользуемые UI fragments вроде top bar;
- `flows` - бизнес-последовательности без низкоуровневых кликов в тесте;
- `fixtures` - подготовка anonymous/authenticated sessions;
- `assertions` - reusable доменные проверки;
- `listeners` - network diagnostics и attach в Allure;
- `base/BaseUiSuite` - жизненный цикл браузера, `BrowserContext`, trace, screenshot и video.

## Environment loading

UI automation использует порядок:

1. env vars;
2. `tests/config/test-environment.properties`;
3. fallback values в `UiTestEnvironment`.

Ключевые значения:

- `UI_BASE_URL`
- `API_BASE_URL`
- `E2E_CUSTOMER_EMAIL`
- `E2E_CUSTOMER_PASSWORD`
- `E2E_ADMIN_EMAIL`
- `E2E_ADMIN_PASSWORD`
- `UI_HEADLESS`
- `UI_BROWSER`
- `UI_SLOW_MO_MS`
- `UI_ACTION_TIMEOUT_MS`
- `UI_NAVIGATION_TIMEOUT_MS`

## Артефакты и диагностика

Модуль сохраняет:

- screenshots;
- videos;
- Playwright traces;
- network diagnostics logs.

Пути:

- `tests/frontend/build/allure-results`
- `tests/frontend/build/test-results`
- `tests/frontend/build/reports/tests`
- `tests/frontend/artifacts`

Открыть trace:

```powershell
npx playwright show-trace tests/frontend/artifacts/traces/<trace-file>.zip
```

## Retry и parallel execution

- retry настраивается через `org.gradle.test-retry`;
- local runs используют `1` retry;
- при `CI=true` retries увеличиваются до `2`;
- JUnit parallel execution включен на уровне модуля, а отдельные тесты изолируются собственным `BrowserContext`.

## Текущее ограничение покрытия

UI automation пока не покрывает:

- каталог и продуктовые сценарии;
- корзину и checkout;
- административные экраны beyond auth/access;
- заказный поток end-to-end.

Это означает, что изменения в этих областях желательно дополнять либо manual smoke, либо новыми suite-ами в `tests/frontend`.
