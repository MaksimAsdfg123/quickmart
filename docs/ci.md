# CI в GitHub Actions

## Workflows
- `pr-ci.yml` (`pull_request`): быстрый gate
  - `pr / backend-unit`
  - `pr / frontend-build`
  - `pr / api-smoke`
  - `pr / ui-smoke`
- `main-ci.yml` (`push` в `main`, `workflow_dispatch`): полный регресс
  - `api-full`
  - `ui-full`
- `nightly-integration.yml` (`schedule`, `workflow_dispatch`): `core + infra` стек, full API/UI, checks на диагностические сигналы.
- `nightly-performance.yml` (`schedule`, `workflow_dispatch`): k6 нагрузка и публикация performance summary.

## CI contract
- Во всех workflow выставляется `CI=true`.
- API/UI URL и креды берутся из GitHub `vars/secrets` (с fallback-значениями по умолчанию для bootstrap).
- Для UI в CI: `UI_HEADLESS=true`.

Рекомендуемые GitHub Variables:
- `API_BASE_URL`
- `UI_BASE_URL`
- `AUTH_ADMIN_EMAIL`
- `AUTH_CUSTOMER_EMAIL`
- `E2E_ADMIN_EMAIL`
- `E2E_CUSTOMER_EMAIL`

Рекомендуемые GitHub Secrets:
- `AUTH_ADMIN_PASSWORD`
- `AUTH_CUSTOMER_PASSWORD`
- `E2E_ADMIN_PASSWORD`
- `E2E_CUSTOMER_PASSWORD`

## Gradle entrypoints
- API:
  - full: `:api-tests:apiTest`
  - smoke: `:api-tests:apiSmokeTest`
- UI:
  - full: `:ui-tests:uiTest`
  - smoke: `:ui-tests:uiSmokeTest`

## Docker Compose profiles
- `core`: `postgres`, `backend`, `frontend`
- `infra`: `kafka`, `redis`, `wiremock`

CI запускает:
- PR/main API: `core` (postgres + backend)
- PR/main UI: `core` (postgres + backend + frontend)
- nightly integration: `core + infra`

Локальный запуск:
- `docker compose --profile core up -d --build`
- `docker compose --profile core --profile infra up -d --build`

## Branch protection checks
Рекомендуемые обязательные checks для PR:
- `pr / backend-unit`
- `pr / frontend-build`
- `pr / api-smoke`
- `pr / ui-smoke`
