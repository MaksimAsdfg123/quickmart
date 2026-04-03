# Engineering Code Review Principles

## Назначение документа

Этот документ фиксирует инженерные принципы, которые уже частично или явно реализованы в Quickmart и должны использоваться как практический ориентир при code review.

Документ не является догматическим учебником по SOLID. Его задача:

- дать общий review-язык для backend, frontend и test automation;
- показать принципы на реальных примерах из текущего проекта;
- помочь отличать полезные абстракции от избыточных;
- зафиксировать, что именно стоит проверять в PR, а что не нужно навязывать любой ценой.

Если какой-то пример в проекте демонстрирует принцип частично, это помечается явно. Такой материал нужно использовать как review heuristic, а не как доказательство “идеальной архитектуры”.

## Как использовать документ на code review

Использовать документ стоит как короткий справочник, а не как checklist на все случаи жизни.

Рекомендуемый порядок:

1. Сначала понять, какую бизнес-задачу решает изменение.
2. Затем проверить, не ломает ли изменение существующие границы ответственности и контракты.
3. После этого использовать соответствующие разделы ниже как review-линзы: `SOLID`, `DRY`, `KISS`, `Separation of Concerns`, `Composition over Inheritance`, `Fail Fast`.
4. Если принцип конфликтует с простотой или локальной понятностью, приоритет остается за более ясным и поддерживаемым решением.

Документ особенно полезен для review:

- новых интеграций;
- изменений checkout/order flow;
- изменений в shared-компонентах frontend;
- новых backend component tests и UI automation;
- PR, в которых появляются новые абстракции, base classes, helper layers или policy objects.

## Быстрый checklist для review

### Backend

- Не смешаны ли HTTP, orchestration, бизнес-правила, persistence и mapping в одном классе.
- Зависят ли сервисы от бизнес-контракта, а не от конкретной интеграционной реализации.
- Сохраняются ли инварианты существующих контрактов и результатов.
- Не появляется ли дублирование проверок, invalidation-логики или error mapping.
- Ошибки валидируются и завершаются рано, до появления частичных побочных эффектов.

### Frontend

- Не смешаны ли routing, session control, data fetching и визуальная разметка в одном месте.
- Не превращается ли shared-component в “универсальный комбайн” с множеством несвязанных обязанностей.
- Переиспользование строится через композицию UI building blocks, а не через разрастание inheritance-like abstractions.
- Общие client-side правила и API wiring централизованы, а не копируются по страницам.

### Tests

- Suite-классы остаются короткими и не тащат в себя transport-level детали.
- Повторяемая логика вынесена в `scenario`, `assertion`, `flows`, `fixtures`, `foundation`.
- Base-классы используются только для bootstrap и environment setup, а не как место для накопления бизнес-логики.
- Новый тест проверяет observable behavior, а не случайные внутренние детали реализации.

## SRP: Single Responsibility Principle

### Для чего нужен

SRP помогает локализовать изменения. Когда у класса одна явная ответственность, его легче читать, тестировать и безопасно менять.

### Как реализуется в Quickmart

Хороший backend-пример - разбиение checkout domain flow на несколько служб:

- `CheckoutService` orchestrates checkout и собирает заказ из корзины, адреса, слота доставки, промокода и inventory checks.
- `PaymentProcessingService` отвечает только за интерпретацию payment method и результата внешней оплаты.
- `OrderStatusTransitionService` инкапсулирует только правила допустимых переходов статусов заказа.
- `GlobalExceptionHandler` отдельно отвечает за преобразование внутренних ошибок в HTTP error contract.

Хороший frontend-пример - разделение route guarding и page rendering:

- `ProtectedRoute` решает только вопрос доступа к защищенному маршруту.
- `AdminEntityDrawer` и `AdminPageLayout` отвечают за локальный UI shell и не управляют бизнес-данными.

Хороший test-пример - структура `suite + shared`:

- suite orchestrates сценарий;
- `scenario` собирает действия;
- `assertion` инкапсулирует проверки;
- `foundation` поднимает окружение.

### Что проверять на review

- Есть ли у класса одна доминирующая причина для изменения.
- Можно ли коротко сформулировать ответственность класса одним предложением.
- Не приходится ли менять один и тот же класс одновременно из-за бизнес-правил, интеграции и формата ответа.
- Не превратился ли shared-helper в смесь setup, transport, assertions и domain policy.

### Красные флаги

- Controller начинает содержать бизнес-валидацию, persistence и response mapping одновременно.
- Service одновременно управляет бизнес-процессом, HTTP-клиентом и сериализацией внешнего контракта.
- UI page одновременно решает auth, networking, route protection и layout concerns.
- Test suite напрямую содержит stubs, raw HTTP calls, data factory и assertions в одном классе.

## OCP: Open/Closed Principle

### Для чего нужен

OCP помогает расширять систему без переписывания стабильной бизнес-логики. Это особенно важно для интеграций и feature-flagged сценариев.

### Как реализуется в Quickmart

Основной пример - интеграция mock online payment provider:

- `MockOnlinePaymentGateway` задает стабильный контракт авторизации оплаты.
- `LocalMockOnlinePaymentGateway` и `HttpMockOnlinePaymentGateway` расширяют поведение разными реализациями одного контракта.
- `MockOnlinePaymentClientConfig` выбирает реализацию через configuration-driven binding, не требуя менять `PaymentProcessingService`.

Практический смысл здесь в том, что checkout-логика не переписывается при переходе от локального sandbox-поведения к реальному HTTP provider simulation.

### Что проверять на review

- Можно ли добавить новый вариант поведения через новую реализацию существующего контракта.
- Не потребует ли подключение новой интеграции переписывать стабильный orchestration code.
- Не завязана ли точка расширения на `if/else` по конкретным технологиям там, где уже есть подходящий интерфейс или policy seam.

### Красные флаги

- Для подключения новой реализации приходится менять бизнес-сервис, который должен быть стабильным.
- Новый provider требует прямой проверки `if (httpEnabled)` в domain/service-слое.
- Feature toggle приводит к разрастанию условной логики по нескольким слоям вместо одного binding point.

### Caveat

Quickmart демонстрирует OCP не во всех частях системы одинаково сильно. Это хороший принцип прежде всего для integration seams и policy seams, а не призыв создавать интерфейс для каждого класса.

## LSP: Liskov Substitution Principle

### Для чего нужен

LSP нужен затем, чтобы замена одной реализации другой не ломала поведение потребителя контракта.

### Как реализуется в Quickmart

Наиболее корректный локальный пример - контракт `MockOnlinePaymentGateway`.

Для `PaymentProcessingService` любая реализация gateway должна сохранять одни и те же инварианты:

- успешная авторизация возвращается как `Approved` с usable `paymentReference`;
- бизнес-отказ возвращается как `Rejected` с provider-level причиной;
- техническая недоступность возвращается как `TechnicalFailure` с типизированной причиной;
- реализация не должна заставлять consumer знать детали конкретного transport mechanism.

И `LocalMockOnlinePaymentGateway`, и `HttpMockOnlinePaymentGateway` подчиняются этому ожиданию: сервис обработки оплаты работает только с общим результатом и не меняет поведение в зависимости от конкретного класса.

### Что проверять на review

- Сохраняет ли новая реализация все семантические ожидания существующего consumer.
- Не требует ли подстановка новой реализации дополнительных специальных веток в consumer-коде.
- Не меняет ли реализация смысл уже существующих result types.

### Красные флаги

- После добавления новой реализации приходится писать `when (gateway)` или проверять конкретный класс.
- Одна из реализаций возвращает результат, который формально компилируется, но семантически не соответствует контракту.
- Реализация нарушает ожидания по side effects, статусам ошибок или обязательным полям результата.

### Caveat

В проекте нет формализованного отдельного contract test suite именно на LSP-уровне. Поэтому этот раздел нужно использовать как review-критерий для новых реализаций, а не как утверждение, что контракт уже исчерпывающе доказан тестами.

## ISP: Interface Segregation Principle

### Для чего нужен

ISP снижает связность: потребитель должен зависеть только от того контракта, который ему действительно нужен.

### Как реализуется в Quickmart

`PaymentProcessingService` зависит только от `MockOnlinePaymentGateway`, а не от полного HTTP client stack, `RestClient`, DTO внешнего провайдера и деталей конфигурации.

Похожая идея есть и на уровне тестов:

- test suites не зависят от всего набора инфраструктурных деталей;
- UI suites работают через `flows`, `pages`, `assertions`, а не напрямую через низкоуровневые Playwright-вызовы во всех тестах.

### Что проверять на review

- Не тянет ли новый потребитель слишком широкий интерфейс ради одной операции.
- Можно ли отделить transport/infrastructure API от domain-friendly контракта.
- Не заставляет ли shared abstraction клиентов знать лишние поля, методы или режимы работы.

### Красные флаги

- Сервису нужен один метод, но он получает крупный client/facade с несвязанными возможностями.
- Shared helper начинает обслуживать несколько слоев с несовместимыми обязанностями.
- Новый интерфейс появляется как “god interface”, объединяющий unrelated use cases.

## DIP: Dependency Inversion Principle

### Для чего нужен

DIP позволяет держать бизнес-логику в зависимости от устойчивых доменных контрактов, а не от деталей инфраструктуры.

### Как реализуется в Quickmart

Самый явный пример снова находится в payment integration:

- высокоуровневый `PaymentProcessingService` зависит от абстракции `MockOnlinePaymentGateway`;
- низкоуровневые детали transport, timeout handling, JSON parsing и provider headers находятся в `HttpMockOnlinePaymentGateway`;
- wiring сделан в `MockOnlinePaymentClientConfig`.

Схожая идея есть и в event-driven частях:

- `OrderEventPublisher` публикует внутреннее доменное событие;
- `OrderKafkaPublisher` берет на себя конкретный Kafka transport и включается опционально.

### Что проверять на review

- Не начинает ли high-level service напрямую зависеть от конкретного HTTP/Kafka/DB/mechanism API без необходимости.
- Есть ли в системе один явный composition point для выбора реализации.
- Остается ли бизнес-логика тестируемой без реального transport stack.

### Красные флаги

- Domain service сам создает `RestClient`, сериализует JSON и знает URL внешнего сервиса.
- Feature-flag выбор реализации размазан по нескольким сервисам.
- Бизнес-логика зависит от инфраструктурных типов, которые не выражают бизнес-смысл.

## DRY: Don't Repeat Yourself

### Для чего нужен

DRY нужен не для “минимального количества строк”, а для устранения опасного дублирования правил и повторяемой механики.

### Как реализуется в Quickmart

Хорошие примеры:

- cache invalidation централизована через `CatalogReadCacheInvalidationPublisher` и `CatalogReadCacheInvalidationListener`, а не копируется по каждому месту чтения/очистки кэша;
- frontend использует общий `apiClient` для base URL, headers и auth/session interceptors;
- test automation опирается на shared-слои `scenario`, `assertion`, `flows`, `fixtures`, `foundation` вместо копирования setup и проверок по suite-классам;
- UI auth flow вынесен в `AuthFlow`, а не повторяется в каждом тесте вручную.

### Что проверять на review

- Дублируется ли один и тот же business rule или только похожая механика.
- Появляется ли второй источник правды для одних и тех же правил.
- Есть ли повторяющиеся куски setup или assertions, которые уже живут в shared-слое.

### Красные флаги

- Одинаковая invalidation-логика копируется в нескольких сервисах.
- Один и тот же auth/session wiring повторяется по frontend API functions или page-level effects.
- Несколько тестов вручную повторяют одинаковый flow и одни и те же assertions.

### Caveat

Нельзя превращать DRY в принудительное “слияние всего похожего”. Если два фрагмента пока совпадают случайно и развиваются независимо, локальное дублирование может быть дешевле, чем преждевременная абстракция.

## KISS: Keep It Simple, Stupid

### Для чего нужен

KISS защищает проект от избыточной архитектуры там, где достаточно явной и прямой модели.

### Как реализуется в Quickmart

Примеры:

- `OrderStatusTransitionService` хранит допустимые переходы в простой таблице `Map<OrderStatus, Set<OrderStatus>>` без избыточных state machine frameworks;
- `ProtectedRoute` решает только redirect к login при отсутствии токена, не превращаясь в общий orchestration engine;
- `queryClient` на frontend имеет минимально нужные global defaults без переусложненной policy-конфигурации.

### Что проверять на review

- Решает ли предлагаемая абстракция реальную текущую проблему.
- Нельзя ли выразить правило более простой структурой данных или меньшим числом слоев.
- Появляется ли новая сложность ради гипотетического будущего кейса.

### Красные флаги

- Для простого набора статусов вводится сложный engine или framework-level abstraction.
- Небольшой shared-component перегружается множеством режимов и флагов.
- В PR добавляется универсальная abstraction “на будущее”, но без второго реального use case.

## Separation of Concerns

### Для чего нужен

Separation of Concerns помогает удерживать явные границы между слоями и типами задач: transport, domain, persistence, presentation, test infrastructure.

### Как реализуется в Quickmart

Backend уже разделен на читаемые слои:

- `controller` обрабатывает HTTP surface;
- `service` инкапсулирует бизнес-операции;
- `mapper` преобразует entity и DTO;
- `repository` отвечает за persistence access;
- `config`, `client`, `kafka`, `cache` удерживают инфраструктурные concerns отдельно.

Frontend тоже показывает полезные границы:

- `api/*` отвечает за HTTP access;
- `shared/components/*` держит переиспользуемые building blocks;
- `pages/*` собирает экранный use case;
- `shared/lib/*` хранит stores и утилиты;
- route guards вынесены отдельно от page content.

В test automation эта идея выражена особенно явно через `shared` и `suites`.

### Что проверять на review

- Не протекли ли инфраструктурные детали в user-facing или domain code.
- Не начинает ли mapper принимать бизнес-решения.
- Не начинает ли page-layer напрямую содержать transport wiring и session orchestration.
- Не утекает ли в test suites то, что должно жить в shared-infrastructure.

### Красные флаги

- Repository или mapper содержит бизнес-политику.
- UI component содержит сетевой orchestration code и session recovery rules.
- Test suite напрямую управляет infrastructure bootstrap и integration stubs.

## Composition over Inheritance

### Для чего нужен

Композиция обычно делает систему гибче и прозрачнее, чем глубокие иерархии наследования.

### Как реализуется в Quickmart

Лучшие примеры здесь находятся в test automation и frontend:

- UI automation собирает поведение через `pages`, `components`, `flows`, `assertions`, `fixtures`;
- backend component tests собирают сценарии через `scenario`, `assertion`, `data`, `foundation`;
- `AuthenticationUiTest` использует `AuthFlow`, `AuthAssertions`, `UiSessionFixture`, а не наследует большой “умный” test superclass;
- `MockOnlinePaymentCheckoutTest` опирается на `MockOnlinePaymentCheckoutScenario` и `MockOnlinePaymentAssertions`, сохраняя suite-класс коротким.

Base-классы вроде `BaseUiSuite`, `BaseApiTest` и `BaseMockOnlinePaymentComponentTest` в проекте тоже есть, но они в основном ограничены bootstrap, environment setup и lifecycle hooks.

### Что проверять на review

- Можно ли собрать поведение из нескольких маленьких компонентов вместо расширения базового класса.
- Не превращается ли base class в скрытый контейнер бизнес-логики и helper-магии.
- Остается ли поведение теста или UI flow читаемым из места использования.

### Красные флаги

- Новый base class начинает накапливать сценарии, assertions и test/business policy.
- Из-за наследования становится трудно понять, откуда взялось конкретное поведение.
- Повторное использование достигается через “god superclass” вместо небольших composable blocks.

## Fail Fast и explicit error handling

### Для чего нужен

Fail Fast уменьшает число скрытых побочных эффектов и делает сбои предсказуемыми. Explicit error handling помогает поддерживать стабильный внешний контракт при внутренних ошибках.

### Как реализуется в Quickmart

Хорошие backend-примеры:

- `CheckoutService` завершает сценарий рано, если корзина пуста или товар недоступен;
- `DeliverySlotService` и `InventoryService` валидируют ограничения до продолжения бизнес-операции;
- `PaymentProcessingService` сразу классифицирует rejected/technical failures и переводит их в контролируемые бизнес-ошибки;
- `GlobalExceptionHandler` централизует преобразование исключений в предсказуемый HTTP error contract;
- `CatalogReadCacheInvalidationListener` и `OrderKafkaPublisher` привязаны к `AFTER_COMMIT`, что уменьшает риск видимых побочных эффектов до успешного завершения транзакции.

### Что проверять на review

- Проверяются ли критичные инварианты до необратимых побочных эффектов.
- Есть ли единый путь преобразования ошибок во внешний контракт.
- Не скрываются ли ошибки через молчаливые fallback-ветки там, где нужен явный отказ.
- Не публикуются ли события или invalidation-сигналы до commit.

### Красные флаги

- Побочные эффекты выполняются до завершения ключевых валидаций.
- Ошибки разных типов теряются в generic exception без осмысленного контракта.
- В интеграциях сбой “проглатывается”, но состояние системы уже успело измениться.

## Как применять принципы без догматизма

При review важно не требовать формального соблюдения каждого принципа в каждой строке кода.

Нормальная практика для Quickmart:

- не вводить интерфейс без второго осмысленного сценария или явного integration seam;
- не убирать локальное дублирование ценой неестественной абстракции;
- не усложнять простой бизнес-процесс ради “архитектурной чистоты”;
- не расширять base-классы, если поведение понятнее через композицию;
- не называть любое разбиение на классы “SOLID”, если от него не становится проще сопровождение и review.

Если PR делает систему:

- понятнее;
- локальнее по ответственности;
- безопаснее по контрактам;
- проще для тестирования и дальнейших изменений,

то он, скорее всего, движется в правильную сторону, даже если не демонстрирует “идеальную” академическую форму принципа.

## Связанные документы

- `docs/architecture/system-overview.md` - системные границы и ключевые архитектурные решения.
- `docs/testing/test-authoring-rules.md` - обязательные правила проектирования новых автотестов.
- `docs/testing/test-strategy.md` - роль каждого test layer и текущий coverage profile.
- `docs/integrations/kafka.md` - пример отдельного integration seam с after-commit публикацией событий.
- `docs/integrations/wiremock.md` и `docs/integrations/mock-online-payment-provider-contract.md` - пример transport-level integration boundary и contract-oriented testing.
