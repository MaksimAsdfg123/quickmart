# Правила написания новых автотестов

## 1. Назначение и область действия

Документ фиксирует обязательные правила написания новых автотестов в репозитории Quickmart. Он распространяется на все тестовые слои проекта:

- `tests/backend`
- `tests/frontend`
- `tests/performance`

Документ предназначен одновременно для инженеров и для агентных запросов. Его задача — обеспечить единый стиль, предсказуемую структуру тестов и отсутствие ad-hoc автоматизации, не встроенной в архитектуру проекта.

При review test-related PR этот документ нужно использовать вместе с `docs/engineering/code-review-principles.md`: текущий файл задает обязательные правила тестовой архитектуры, а engineering guide помогает оценивать качество абстракций, границы ответственности и уместность переиспользования.

## 2. Иерархия инструкций

Приоритет правил следующий:

1. `AGENTS.md`
2. `docs/testing/test-authoring-rules.md`
3. `docs/engineering/code-review-principles.md` — review-oriented guide по SOLID, DRY, KISS, SoC и другим инженерным принципам, применяемым к runtime- и test-коду
4. layer-specific документы:
   - `docs/testing/test-strategy.md`
   - `docs/testing/backend-api-tests.md`
   - `docs/testing/ui-tests.md`
   - профильные integration docs, если тест касается Kafka, WireMock, cache или другой отдельной подсистемы

`AGENTS.md` служит только repo-level entrypoint. Полная спецификация находится в текущем документе.

## 3. Как выбирать слой теста

| Слой | Когда использовать | Где размещать | Какой task является целевым |
| --- | --- | --- | --- |
| Backend black-box API | Проверяется публичный REST contract уже запущенного backend | `tests/backend/suites/.../api/...` | `:api-tests:apiTest`, `:api-tests:apiSmokeTest` |
| Backend component / integration | Проверяется внутренняя backend-цепочка, self-contained Spring context, cache/Kafka/WireMock/integration behavior | `tests/backend/suites/.../<domain>/...` | отдельный tagged task, например `:api-tests:kafkaTest`, `:api-tests:cacheTest`, `:api-tests:wiremockTest` |
| UI automation | Проверяется пользовательский сценарий через браузер | `tests/frontend/suites/...` | `:ui-tests:uiTest`, `:ui-tests:uiSmokeTest` |
| Performance smoke | Проверяется нагрузочный или smoke-load контур | `tests/performance` | профильный performance task / script |

Правила выбора:

- Если нужен публичный API contract, использовать black-box API suite.
- Если нужен Spring context, repositories, Embedded Kafka, WireMock или self-contained integration path, использовать backend component suites.
- Если нужен пользовательский flow через браузер, использовать UI automation.
- Если задача про throughput, latency или smoke-load, использовать performance-layer.

Нельзя смешивать эти слои в одном тесте.

## 4. Обязательные правила размещения

- Новые backend API и component tests создаются только в `tests/backend`.
- Новые UI tests создаются только в `tests/frontend`.
- Новые performance scripts и performance assertions создаются только в `tests/performance`.
- Новая активная test automation не размещается в `app/backend/src/test` и `app/frontend`.
- Runtime-модули не должны становиться основным местом для regression suites.

Исключение допускается только для отдельного явно разрешенного module-local тестового случая, когда это требуется самой runtime-сборкой и не подменяет test architecture проекта.

## 5. Общие стандарты для всех новых автотестов

### 5.1 Что должен проверять тест

- Тест должен проверять бизнес-результат или внешний observable behavior.
- Тест не должен быть завязан на внутреннюю реализацию без необходимости.
- Assertions должны объяснять бизнес-смысл, а не только технический факт вызова метода.

### 5.2 Стиль suite-классов

- Suite-классы должны быть короткими.
- В suite-классе допустимы только orchestration шага, вызов scenario/flow и domain-level assertions.
- Низкоуровневый setup, builders, HTTP details, WireMock stubs, raw JSON, page locators и повторяемые assertion-блоки должны быть вынесены в shared-слой.

### 5.3 Naming и аннотации

- Имена test methods — на английском, в стиле `should...`.
- `@DisplayName` — человекочитаемый и описывает ожидаемое поведение.
- Использовать `@Tag` по слою или специализированному task contract.
- Для backend и UI suites использовать согласованный набор Allure-аннотаций:
  - `@Epic`
  - `@Feature`
  - `@Story`
  - `@Severity`
  - `@Owner`
- Не создавать новые naming conventions поверх уже существующих, если текущий слой проекта уже задает устойчивый шаблон.

### 5.4 Shared-слой обязателен

Если в тестах появляется повторяемая логика, она должна быть вынесена в shared-слой, а не копироваться по suite-классам.

Базовые типы shared-компонентов:

- `foundation` — base classes, environment, lifecycle, embedded infrastructure
- `scenario` / `flows` — повторяемые бизнес-последовательности
- `assertion` — domain-level reusable assertions
- `data` — object mother, test data factory, builders
- `clients` / `api` / `pages` / `components` — transport/UI abstractions без бизнес-assertions

## 6. Правила `suites + shared`

Проект следует паттерну `suites + shared`.

### 6.1 Что находится в `suites`

- Только исполняемые test classes.
- Минимум логики.
- Понятный сценарий Arrange / Act / Assert.

### 6.2 Что находится в `shared`

- Все переиспользуемые слои.
- Все инфраструктурные абстракции.
- Все factories/builders/assertions/scenarios/pages/components/fixtures.

### 6.3 Когда нужно создавать новый shared-компонент

Создавать новый `scenario`, `assertion`, `data factory` или `foundation` нужно, если выполняется хотя бы одно условие:

- логика повторяется в двух и более тестах;
- suite-класс начинает содержать transport-level детали;
- test setup становится длинным и шумным;
- одна и та же доменная проверка или последовательность используется повторно;
- требуется изолировать внешний simulator, embedded infrastructure или page interactions.

## 7. Правила для backend API tests

### 7.1 Назначение

Backend API tests — это black-box проверки публичного REST contract против уже доступного backend.

### 7.2 Обязательные требования

- Размещать в `tests/backend/suites/.../api/...`.
- Использовать существующие foundation/client/scenario/assertion слои.
- Не строить black-box API suites напрямую на raw Rest Assured в каждом тесте.
- Не смешивать setup environment, request building и assertions в одном методе.

### 7.3 Что запрещено

- Писать black-box API tests как component tests со Spring context.
- Добавлять в black-box suite прямую зависимость на repositories и service beans.
- Использовать black-box слой для проверки внутренних implementation details.

## 8. Правила для backend component / integration tests

### 8.1 Назначение

Backend component tests используются, когда нужно проверить self-contained backend behavior внутри Spring test context.

Подходящие случаи:

- cache behavior
- Kafka integration
- WireMock-backed HTTP integration
- integration chains, требующие repositories, services и embedded infrastructure

### 8.2 Обязательные требования

- Размещать в `tests/backend/suites/...`, не в runtime-модуле.
- Использовать `Spring profile test`.
- Использовать отдельные `@Tag` и соответствующий Gradle task.
- Делать suite self-contained.
- Выносить bootstrap/infrastructure в `foundation`.
- Использовать simulators уровня transport/integration, если проверяется контракт внешней зависимости.

### 8.3 Интеграционные симуляторы

Если проверяется интеграционный контракт, использовать соответствующий transport-level simulator:

- HTTP integration -> WireMock
- Kafka integration -> Embedded Kafka
- cache integration -> self-contained cache setup / Spring test profile

Если проверяется HTTP contract, нельзя подменять его hand-made mock-объектом вместо WireMock.

### 8.4 Assertions

- Проверять бизнес-результат.
- Проверять side effects и rollback semantics там, где это важно.
- Проверять исходящий request contract, если речь идет о внешнем интеграционном вызове.

## 9. Правила для UI automation

### 9.1 Назначение

UI tests проверяют пользовательский сценарий через реальный браузер и не должны превращаться в набор хаотичных Playwright-вызовов.

### 9.2 Обязательные требования

- Размещать в `tests/frontend/suites/...`.
- Использовать `BaseUiSuite`.
- Использовать разделение:
  - `pages`
  - `components`
  - `flows`
  - `assertions`
  - `fixtures`
- В suite-классе оставлять только высокоуровневый сценарий.

### 9.3 Что запрещено

- Длинные цепочки raw locator calls в suite-классе.
- Повторять page interactions в нескольких тестах вместо выделения flow.
- Смешивать UI assertions и API setup в одном месте без fixture/flow abstraction.

## 10. Правила для performance-layer

### 10.1 Назначение

Performance-layer служит только для нагрузочных и smoke-load проверок.

### 10.2 Обязательные требования

- Размещать только в `tests/performance`.
- Не переносить туда функциональные regression suites.
- Не смешивать performance assertions с UI/browser automation или backend component logic.

### 10.3 Scope

Для текущего проекта performance-layer должен оставаться минимальным и операционно понятным. Это не место для продуктового e2e functional coverage.

## 11. Правила для test data

- Тестовые данные должны быть уникальными там, где возможен конфликт по email, idempotency key, business key или имени.
- Тестовые данные должны быть детерминированными и контролируемыми.
- Shared mutable state между тестами запрещен.
- Новые builders / object mothers / factories должны жить в `shared/.../data`.
- Нельзя разбрасывать inline hardcoded payload builders по suite-классам.

## 12. Anti-patterns

Запрещенные паттерны:

- тест в стиле “все в одном классе”;
- длинный suite-класс с raw setup, assertions и infrastructure code одновременно;
- дублирование WireMock stubs, Kafka bootstrap, Rest Assured setup или Playwright steps по разным test classes;
- смешивание black-box API и component-test подходов в одном suite;
- размещение новых активных тестов в runtime-модулях;
- проверка только внутреннего вызова вместо бизнес-результата;
- hand-made mocks вместо integration simulator, если проверяется реальный transport contract;
- копирование одинаковых assertion-блоков вместо отдельного assertion helper;
- прямое добавление локаторов и браузерной механики в UI suite без page/flow abstraction.

## 13. Чеклист автора нового теста

Перед добавлением нового автотеста необходимо подтвердить:

- выбран корректный test layer;
- тест размещается в правильном `tests/*` модуле;
- suite-класс короткий и не содержит низкоуровневого шума;
- повторяемая логика вынесена в `shared`;
- используются существующие naming conventions, tags и Allure-аннотации;
- assertions проверяют бизнес-результат;
- test data уникальны и не создают скрытых зависимостей между тестами;
- при интеграционном сценарии используется подходящий simulator;
- новый тест можно запустить существующим task contract без ручных нестандартных действий.

## 14. Что читать дальше

- `docs/testing/test-strategy.md` — когда выбирать конкретный слой тестирования
- `docs/engineering/code-review-principles.md` — как оценивать SOLID, DRY, KISS, SoC, композицию и error handling при review test-related PR
- `docs/testing/backend-api-tests.md` — устройство backend API/component automation
- `docs/testing/ui-tests.md` — устройство UI automation
- профильные integration docs в `docs/integrations/*` — правила конкретных интеграционных контуров
