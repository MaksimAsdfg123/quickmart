# Правила для задач по автотестам

- Для любого запроса, в котором нужно создать, изменить или расширить автотесты, сначала прочитать [docs/testing/test-authoring-rules.md](docs/testing/test-authoring-rules.md).
- Этот файл является короткой repo-level точкой входа; подробные обязательные правила находятся только в `docs/testing/test-authoring-rules.md`.
- Новые backend API и backend component tests размещать только в `tests/backend`.
- Новые UI automation tests размещать только в `tests/frontend`.
- Новые performance checks размещать только в `tests/performance`.
- Не переносить активную test automation в runtime-модули `app/backend` и `app/frontend`, если это не отдельный явно разрешенный module-local случай.
- Для новых тестов использовать существующий стиль `suites + shared`: короткие suite-классы и вынесенные `scenario`/`assertion`/`data`/`foundation` слои.
- Не писать ad-hoc тесты в стиле “все в одном классе” с дублируемым setup, transport-логикой и assertions вперемешку.
- Для backend external integration tests использовать transport-level simulation вроде WireMock, если проверяется реальный HTTP contract.
- Для UI tests использовать разделение `pages` / `components` / `flows` / `assertions` / `fixtures`.
