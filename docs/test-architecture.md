# Архитектура backend API-тестов (Suite + Shared)

## Цель
Backend test layer организован по принципу:
- `suites` — только сценарные тесты;
- `shared` — только переиспользуемая инфраструктура и доменные хелперы.

Это снижает связанность и упрощает навигацию: сначала видно, какие тесты есть, затем чем они поддержаны.

## Физическая структура

```text
tests/
  config/
    test-environment.properties
  backend/
    suites/
      kotlin/com/quickmart/test/suites/
        api/
          auth/
            AuthApiTest.kt
    shared/
      kotlin/com/quickmart/test/shared/
        foundation/
          BaseApiTest.kt
          ApiTestEnvironment.kt
          ApiSpecifications.kt
          AllureHttpLoggingFilter.kt
          AllureSteps.kt
          ResponseMappers.kt
        clients/
          AuthApiClient.kt
          CartApiClient.kt
          AdminProductsApiClient.kt
        auth/
          model/
          data/
          scenario/
          assertion/
        common/
          util/
            RandomDataUtils.kt
    resources/
      application-test.yml
      allure.properties
      junit-platform.properties
```

## Роли слоев
- `suites`: короткие тесты уровня бизнес-сценария.
- `shared/foundation`: общая платформа тестов (конфиг, base class, specs, Allure/logging).
- `shared/clients`: endpoint-ориентированные REST-клиенты без assertions.
- `shared/<domain>`: доменные модели, фабрики данных, сценарии и проверки.
- `shared/common`: общие утилиты, не привязанные к конкретному домену.

## Правила расширения
1. Новый API-тест добавляется в `suites/api/<domain>`.
2. Новый HTTP endpoint оформляется в `shared/clients`.
3. Сложная бизнес-последовательность оформляется в `shared/<domain>/scenario`.
4. Повторяемые проверки выносятся в `shared/<domain>/assertion`.
5. Подготовка payload и генерация данных — в `shared/<domain>/data` и `shared/common/util`.

## Package contract
- Тесты: `com.quickmart.test.suites.*`
- Инфраструктура и хелперы: `com.quickmart.test.shared.*`

