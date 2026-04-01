# Стратегия тестирования

## Обзор

Quickmart использует несколько независимых слоев верификации. Это важно для понимания проекта: названия задач `apiTest`, `uiTest` и `backend:test` относятся к разным типам проверки и не эквивалентны по глубине и способу запуска.

## Verification layers

| Слой | Модуль / задача | Требует заранее поднятый runtime stack | Текущий scope |
| --- | --- | --- | --- |
| Backend module tests | `:backend:test` | Нет | модульный backend gate; активное покрытие в runtime-модуле сейчас минимально |
| API smoke | `:api-tests:apiSmokeTest` / root `apiSmokeTest` | Да | auth/access smoke against running backend |
| API suite | `:api-tests:apiTest` / root `apiTest` | Да | текущий black-box API suite, ориентированный на auth/access contract |
| Backend cache component suites | `:api-tests:cacheTest` | Нет | Caffeine cache behavior и invalidation на Spring test profile |
| Backend Kafka component suites | `:api-tests:kafkaTest` | Нет | order lifecycle events, audit trail, disabled mode, failure scenarios |
| Backend WireMock component suites | `:api-tests:wiremockTest` | Нет | external HTTP payment integration, request contract, failure handling, rollback semantics |
| UI smoke | `:ui-tests:uiSmokeTest` / root `uiSmokeTest` | Да | auth smoke в реальном браузере |
| UI suite | `:ui-tests:uiTest` / root `uiTest` | Да | текущий UI suite для auth/access flows |
| Performance smoke | `tests/performance/smoke-load.js` / nightly workflow | Да | health + login load smoke |

## Что считается текущим покрытием

### Сильные стороны текущей автоматизации

- auth/access contract покрыт и на API-уровне, и на UI-уровне;
- public catalog caching покрыт отдельными component suites;
- Kafka-интеграция покрыта flow, payload, failure, disabled-mode и audit-сценариями;
- внешние HTTP integration paths покрыты WireMock-backed component suites с проверкой outgoing request и rollback side effects;
- UI diagnostics и Allure artifacts уже встроены в базовую инфраструктуру.

### Что покрыто ограниченно

- black-box API automation не является полным регрессом всех бизнес-эндпоинтов;
- UI automation пока не покрывает catalog/cart/checkout/admin flows, кроме auth и protected access;
- runtime backend module test layer (`app/backend/src/test`) не является основным носителем проектной автоматизации;
- Kafka/cache/WireMock component suites важны для change-driven verification, но пока не входят отдельными задачами в стандартный GitHub Actions pipeline.

## Рекомендуемый набор проверок по типу изменения

| Тип изменений | Рекомендуемый минимум |
| --- | --- |
| README / docs only | выборочная верификация ссылок, команд и путей |
| Frontend UI | `cd app/frontend && npm run build`, при изменении auth/routes - `.\gradlew.bat uiSmokeTest` |
| Backend REST / auth | `.\gradlew.bat apiSmokeTest` или `.\gradlew.bat apiTest` |
| Cache layer | `.\gradlew.bat :api-tests:cacheTest` |
| Kafka layer | `.\gradlew.bat :api-tests:kafkaTest` |
| External HTTP integration layer | `.\gradlew.bat :api-tests:wiremockTest` |
| Infra / compose / multi-surface changes | соответствующие smoke/full команды + проверка Docker Compose |

## Artifacts и отчеты

| Слой | Артефакты |
| --- | --- |
| API automation | `tests/backend/build/test-results`, `tests/backend/build/reports/tests`, `tests/backend/build/allure-results` |
| UI automation | `tests/frontend/build/test-results`, `tests/frontend/build/reports/tests`, `tests/frontend/build/allure-results`, `tests/frontend/artifacts` |
| Performance | `tests/performance/results` |

## Detailed guides

- [backend-api-tests.md](backend-api-tests.md) - структура, tasks и текущие suites модуля `:api-tests`
- [ui-tests.md](ui-tests.md) - Playwright tasks, artifacts и scope UI automation
- [manual-api-checklist.md](manual-api-checklist.md) - ручная проверка cart/checkout/admin flow и seed-данных
- [../integrations/kafka.md](../integrations/kafka.md) - архитектура и эксплуатация Kafka-интеграции
- [../integrations/wiremock.md](../integrations/wiremock.md) - WireMock-backed strategy для external HTTP integration testing

## Практические выводы для ревью и онбординга

- задачи `apiTest` и `uiTest` нужно читать как "полный запуск текущего реализованного набора suite-ов", а не как исчерпывающий регресс всего продукта;
- для backend business changes важно смотреть не только на black-box API tests, но и на component suites;
- manual smoke по cart/checkout/admin flow остается полезным дополнением к automation, потому что автоматизация пока не закрывает весь пользовательский путь end-to-end.
