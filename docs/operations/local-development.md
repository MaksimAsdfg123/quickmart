# Локальная разработка и запуск

## Предварительные требования

- Java 21
- Node.js 22 и `npm`
- Docker Desktop / Docker Engine с поддержкой `docker compose`
- при работе с UI automation: браузеры Playwright через `.\gradlew.bat installUiBrowsers`
- при работе с Allure-отчетами: установленный `allure` CLI

## Режимы запуска

### 1. Полный стек в Docker

Самый простой способ получить рабочую среду:

```powershell
docker compose --profile core up --build -d
```

Что запускается:

- `postgres`
- `backend`
- `frontend`

Когда использовать:

- быстрый smoke локально;
- демонстрация проекта;
- запуск black-box API/UI tests на готовом локальном стеке.

### 2. Полный стек с optional infra

PowerShell:

```powershell
$env:APP_KAFKA_ENABLED="true"
docker compose --profile core --profile infra up --build -d
```

Bash:

```bash
export APP_KAFKA_ENABLED=true
docker compose --profile core --profile infra up --build -d
```

Дополнительно к `core` запускаются:

- `kafka`
- `kafka-ui`
- `redis`
- `wiremock`

Когда использовать:

- локальная проверка Kafka integration;
- ручная диагностика через Kafka UI;
- подготовка окружения под более широкие integration сценарии, включая mock-online payment provider.

Важно: backend начинает реально публиковать и читать Kafka-события только при `APP_KAFKA_ENABLED=true`.
Аналогично, `MOCK_ONLINE` checkout начинает ходить в WireMock sandbox provider только при `APP_MOCK_ONLINE_PAYMENT_ENABLED=true`.

### 3. Hybrid local development

Если backend и frontend нужно запускать локально из IDE/терминала, а инфраструктуру оставить в контейнерах:

```powershell
docker compose --profile core up -d postgres
.\gradlew.bat :backend:bootRun
cd app/frontend
npm ci
npm run dev
```

Это удобный режим для активной разработки UI или backend, потому что:

- Postgres остается одинаковым с Docker-окружением;
- backend можно отлаживать локально;
- frontend работает через Vite dev server с hot reload.

### 4. Local backend + containerized Kafka

Если нужен локальный `bootRun`, но Kafka тоже нужна:

PowerShell:

```powershell
$env:APP_KAFKA_ENABLED="true"
docker compose --profile infra up -d kafka kafka-ui
.\gradlew.bat :backend:bootRun
```

Bash:

```bash
export APP_KAFKA_ENABLED=true
docker compose --profile infra up -d kafka kafka-ui
./gradlew :backend:bootRun
```

В этом режиме backend использует `spring.kafka.bootstrap-servers=localhost:9092`, что соответствует конфигурации по умолчанию.

### 5. Local backend + containerized WireMock provider

PowerShell:

```powershell
$env:APP_MOCK_ONLINE_PAYMENT_ENABLED="true"
docker compose --profile infra up -d wiremock
.\gradlew.bat :backend:bootRun
```

Bash:

```bash
export APP_MOCK_ONLINE_PAYMENT_ENABLED=true
docker compose --profile infra up -d wiremock
./gradlew :backend:bootRun
```

В этом режиме backend использует `APP_MOCK_ONLINE_PAYMENT_BASE_URL=http://localhost:8089` по умолчанию. Это удобно для отладки `MOCK_ONLINE` checkout и для ручной проверки HTTP integration без Dockerized backend.

## Полезные адреса

| Сервис | Адрес по умолчанию |
| --- | --- |
| Frontend | [http://localhost:5173](http://localhost:5173) |
| Backend API | [http://localhost:8080](http://localhost:8080) |
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Healthcheck | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |
| Kafka UI | [http://localhost:8090](http://localhost:8090) |
| WireMock | [http://localhost:8089](http://localhost:8089) |

## Остановка и очистка

Остановить сервисы:

```powershell
docker compose --profile core down --remove-orphans
docker compose --profile core --profile infra down --remove-orphans
```

Сбросить данные Postgres volume:

```powershell
docker compose --profile core down -v --remove-orphans
docker compose --profile core --profile infra down -v --remove-orphans
```

Используйте `-v` осознанно: это удалит локальный volume `pgdata` и приведет к повторному применению миграций и seed-данных при следующем запуске.

## Практические рекомендации

- для frontend используйте `npm ci`, а не `npm install`: именно этот путь закреплен в CI и соответствует lockfile;
- для black-box API и UI automation удобнее сначала поднять `core` стек через Docker Compose;
- для `:api-tests:kafkaTest`, `:api-tests:cacheTest` и `:api-tests:wiremockTest` внешний Docker stack не нужен: это self-contained component suites;
- `wiremock` в `infra` profile может использоваться не только для manual diagnostics, но и как local sandbox provider для `MOCK_ONLINE` payment flow;
- `redis` остается опциональной инфраструктурой, на которую runtime-логика сейчас не опирается.

## Где искать детали

- настройки env vars: [configuration.md](configuration.md)
- CI workflows: [ci.md](ci.md)
- test layer и команды запуска: [../testing/test-strategy.md](../testing/test-strategy.md)
- Kafka integration: [../integrations/kafka.md](../integrations/kafka.md)
- WireMock integration: [../integrations/wiremock.md](../integrations/wiremock.md)
