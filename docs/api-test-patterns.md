# Паттерны API automation

## 1. Client Pattern
HTTP-детали инкапсулированы в `client`-слое.

- Клиент отвечает только за отправку запроса и возврат `Response`.
- Клиент не содержит бизнес-assertions.
- Endpoint path, query params, headers и serialization изолированы от тестового слоя.

Результат: минимизация дублирования и единая точка изменения при изменении API-контракта.

## 2. Base Specification Pattern
`ApiSpecifications` формирует единый контракт request/response:

- `baseUrl`, таймауты, `Content-Type`, `Accept`;
- auth/unauth variations;
- подключение диагностических filter-ов.

Результат: тесты и клиенты не управляют transport-конфигурацией локально.

## 3. Builder + Object Mother Pattern
Тестовые payload формируются через:

- `RegisterRequestBuilder`, `LoginRequestBuilder`;
- `AuthTestDataFactory`.

Результат: отсутствуют магические строки в тестах, сценарии остаются краткими.

## 4. Scenario Pattern
`AuthScenario` агрегирует последовательности бизнес-шагов:

- регистрация + получение корзины;
- регистрация + повторный запрос для duplicate case;
- login + доступ к admin endpoint.

Результат: тесты описывают поведение системы, а не низкоуровневые HTTP шаги.

## 5. Assertion Helper Pattern
Проверки вынесены в reusable assertion-слой:

- `AuthAssertions`;
- `CartAssertions`;
- `ErrorAssertions`.

Результат: единый стиль проверок и повторное использование правил в разных тестах.

## 6. Reporting Pattern (Allure)
Шаги и диагностика фиксируются централизованно:

- `allureStep(...)` для читабельных шагов на русском;
- `AllureHttpLoggingFilter` добавляет request/response attachments автоматически;
- body ошибок дублируется в attachment через `@Attachment`.

Результат: при падении доступен полный технический контекст без дополнительного repro.

## 7. Parallel-safe Execution Pattern
Безопасность параллельного прогона обеспечивается правилами:

- независимые тестовые данные (`uniqueEmail`);
- отсутствие shared mutable state между тестами;
- mutating сценарии выполняются только с локально созданными данными.

Результат: suite готов к параллельному запуску на уровне JUnit 5 без order dependency.

