# Архитектура API automation (Auth block)

## Назначение
Данный слой реализует production-like основу API автотестов для блока аутентификации (`/api/auth/**`) и связанных проверок авторизации на защищенных endpoint'ах.

## Физическая структура

```text
tests/
  config/
    test-environment.properties
  backend/
    api/
      kotlin/com/quickmart/test/api/test/
        AuthApiTest.kt
    support/
      kotlin/com/quickmart/test/support/
        config/
        base/
        spec/
        listener/
        client/
        model/
        data/
        scenario/
        assertion/
        util/
    resources/
      application-test.yml
      allure.properties
      junit-platform.properties
```

## Ответственность слоев

- `config`: централизованная загрузка test environment (`ApiTestEnvironmentLoader`) с приоритетом `ENV -> properties -> fallback`.
- `base`: общий bootstrap для тестов (`BaseApiTest`), инициализация specs/clients/scenarios.
- `spec`: единые request/response specifications Rest Assured, таймауты, базовые headers.
- `listener`: централизованный HTTP filter (`AllureHttpLoggingFilter`) с вложениями request/response в Allure.
- `client`: endpoint-ориентированные клиенты без бизнес-assertions (`AuthApiClient`, `CartApiClient`, `AdminProductsApiClient`).
- `model`: test DTO для wire-contract parsing.
- `data`: Object Mother + Builder (`AuthTestDataFactory`, `RegisterRequestBuilder`, `LoginRequestBuilder`).
- `scenario`: orchestration бизнес-шагов, композиция вызовов API.
- `assertion`: reusable бизнес-проверки response payload и error contract.
- `test`: короткие, декларативные сценарии верхнего уровня.

## Реализованный набор сценариев

### Позитивные
- регистрация нового пользователя `CUSTOMER` с выдачей JWT;
- создание доступной корзины для нового пользователя после регистрации;
- login customer с нормализацией email по регистру;
- login admin с ролью `ADMIN`;
- повторяемая успешная регистрация + контроль отсутствия конфликтов в независимых тестах.

### Негативные
- login с неверным паролем (`401`);
- duplicate registration email (`409`);
- валидационная ошибка регистрации (`400` + `fieldErrors`);
- доступ к защищенному endpoint без токена (`401`);
- доступ customer к admin endpoint отклоняется (`401/403` в зависимости от security pipeline).

## Принципы расширения

- Новый endpoint добавляется через `client` + `scenario` + `assertion` без дублирования low-level HTTP кода в тестах.
- Новый негативный сценарий должен использовать существующие helpers ошибок (`ErrorAssertions`) или расширять их.
- Любой mutating сценарий обязан использовать уникальные тестовые данные для parallel-safe исполнения.

