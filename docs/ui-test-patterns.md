# UI Test Patterns

## Page Object Pattern
Page layer инкапсулирует локаторы и атомарные действия пользователя:
- `LoginPage`
- `RegisterPage`
- `OrdersPage`

Тест не работает с локаторами напрямую.

## Component Object Pattern
Общие элементы UI вынесены в `components`:
- `TopBarComponent`

Это исключает дублирование по `login/register/logout/nav`.

## Flow / Journey Pattern
Сценарная оркестрация вынесена в `flows/AuthFlow`:
- вход пользователя;
- регистрация;
- переходы между auth-экранами.

Тесты читаются как бизнес-сценарии, а не как набор кликов.

## Fixture Pattern
`UiSessionFixture` управляет подготовкой окружения:
- анонимная сессия;
- авторизованная сессия через API login + localStorage state;
- создание пользователя для негативных кейсов duplicate registration.

## Assertion Helper Pattern
`AuthAssertions` содержит переиспользуемые проверки:
- редирект на login;
- доступ к protected route;
- auth/register ошибки;
- состояние топбара (anonymous/customer/admin).

## Config Abstraction
`UiTestEnvironment` использует приоритет:
1. env vars
2. `tests/config/test-environment.properties`
3. кодовые fallback defaults

## Artifact and Diagnostics Strategy
- screenshot/trace/video: `BaseUiSuite` teardown;
- network diagnostics: `NetworkDiagnosticsCollector` + Allure attachment.

## Selector Strategy
Приоритет селекторов:
1. `data-testid`
2. role-based locators
3. semantic text locators

Fragile CSS и `nth-child` не используются.
