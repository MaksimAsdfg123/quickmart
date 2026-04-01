# CI и GitHub Actions

## Workflow map

| Workflow | Trigger | Что запускается | Фактический stack |
| --- | --- | --- | --- |
| `pr-ci.yml` | `pull_request` | `backend-unit`, `frontend-build`, `api-smoke`, `ui-smoke` | `core` для smoke jobs |
| `main-ci.yml` | `push` в `main`, `workflow_dispatch` | `api-full`, `ui-full` | `core` |
| `nightly-integration.yml` | `schedule`, `workflow_dispatch` | текущие full API/UI suites + observability checks | `core + infra` |
| `nightly-performance.yml` | `schedule`, `workflow_dispatch` | k6 load smoke | `core` |

## Job breakdown

### PR CI

| Job | Команда | Назначение |
| --- | --- | --- |
| `pr / backend-unit` | `./gradlew :backend:test --no-daemon` | модульный backend gate; объем покрытия зависит от содержимого `app/backend/src/test` |
| `pr / frontend-build` | `npm ci && npm run build` | compile/build gate для frontend |
| `pr / api-smoke` | `./gradlew :api-tests:apiSmokeTest --no-daemon` | быстрый black-box smoke текущего API suite |
| `pr / ui-smoke` | `./gradlew :ui-tests:installUiBrowsers` + `./gradlew :ui-tests:uiSmokeTest --no-daemon` | быстрый Playwright smoke текущего UI suite |

### Main CI

| Job | Команда | Назначение |
| --- | --- | --- |
| `api-full` | `./gradlew :api-tests:apiTest --no-daemon` | полный текущий black-box API suite |
| `ui-full` | `./gradlew :ui-tests:uiTest --no-daemon` | полный текущий UI suite |

### Nightly integration

| Job | Команда | Назначение |
| --- | --- | --- |
| `nightly / integration-full` | `./gradlew :api-tests:apiTest :ui-tests:uiTest --no-daemon` | запуск full текущих API/UI suites на `core + infra` стеке плюс диагностические проверки |

Важно: этот workflow поднимает `infra`, но не запускает `:api-tests:kafkaTest` и `:api-tests:cacheTest` как отдельные задачи.

### Nightly performance

| Job | Команда | Назначение |
| --- | --- | --- |
| `nightly / performance` | `grafana/k6:0.53.0 run /work/smoke-load.js` | health + login load smoke c публикацией summary |

## CI environment contract

Во всех workflow задается:

- `CI=true`
- runtime URLs и demo accounts через `vars/secrets` c fallback-значениями
- `UI_HEADLESS=true` для UI automation

### GitHub Variables

Рекомендуемые variables:

- `API_BASE_URL`
- `UI_BASE_URL`
- `AUTH_ADMIN_EMAIL`
- `AUTH_CUSTOMER_EMAIL`
- `E2E_ADMIN_EMAIL`
- `E2E_CUSTOMER_EMAIL`

### GitHub Secrets

Рекомендуемые secrets:

- `AUTH_ADMIN_PASSWORD`
- `AUTH_CUSTOMER_PASSWORD`
- `E2E_ADMIN_PASSWORD`
- `E2E_CUSTOMER_PASSWORD`

## Docker Compose usage in CI

| Сценарий | Compose profile |
| --- | --- |
| API smoke / full | `core` |
| UI smoke / full | `core` |
| Nightly integration | `core + infra` |
| Nightly performance | `core` |

## Публикуемые артефакты

| Job type | Основные артефакты |
| --- | --- |
| backend-unit | `app/backend/build/test-results/**`, `app/backend/build/reports/tests/**` |
| api-smoke / api-full | `tests/backend/build/test-results/**`, `tests/backend/build/reports/tests/**`, `tests/backend/build/allure-results/**`, docker logs |
| ui-smoke / ui-full | `tests/frontend/build/test-results/**`, `tests/frontend/build/reports/tests/**`, `tests/frontend/build/allure-results/**`, `tests/frontend/artifacts/**`, docker logs |
| nightly integration | API + UI test reports, Allure results, Playwright artifacts, full stack docker logs |
| nightly performance | `tests/performance/results/**`, docker logs |

## Что CI не покрывает напрямую

На текущий момент в GitHub Actions нет отдельных job'ов для:

- `:api-tests:kafkaTest`
- `:api-tests:cacheTest`

Эти suites остаются важной частью локальной и change-driven верификации, особенно при изменениях Kafka/cache кода, но не входят в стандартный PR/main pipeline.

## Branch protection checks

Для PR имеет смысл считать обязательными:

- `pr / backend-unit`
- `pr / frontend-build`
- `pr / api-smoke`
- `pr / ui-smoke`

## Практические замечания

- job names `api-full` и `ui-full` означают полный запуск текущих реализованных suite-ов, а не полный регресс всего продукта;
- nightly integration дополнительно проверяет observability-сигналы: наличие Allure results, Playwright traces и API HTTP trace logs;
- performance summary публикуется в `GITHUB_STEP_SUMMARY` на основе `tests/performance/results/summary.json`.
