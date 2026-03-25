# Quickmart MVP

Monorepo MVP сервиса быстрой доставки продуктов и товаров для дома.

## Технологии

- Backend: Kotlin, Spring Boot, Spring Web, Spring Data JPA, Spring Security (JWT), Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, React Router, React Query, Zustand, React Hook Form, Axios
- Инфраструктура: Docker Compose (`postgres`, `backend`, `frontend`)
- Тесты: JUnit 5, Testcontainers

## Структура

```text
project2/
  backend/
    src/main/kotlin/com/quickmart/
      config/
      controller/
      controller/admin/
      service/
      domain/entity/
      domain/enums/
      repository/
      dto/
      mapper/
      security/
      exception/
    src/main/resources/
      application.yml
      db/migration/
    src/test/kotlin/com/quickmart/
      service/
      integration/
  frontend/
    src/
      app/
      pages/
      pages/admin/
      shared/
      api/
  docker-compose.yml
```

## Seed-данные

Заполняются автоматически миграцией `V2__seed_data.sql`:

- Пользователи:
  - `admin@quickmart.local` (ADMIN)
  - `anna@example.com` (CUSTOMER)
  - `ivan@example.com` (CUSTOMER)
- Пароль для всех seed-пользователей: `password`
- 5 категорий
- 20 товаров
- 3 промокода: `WELCOME100`, `SAVE10`, `HOUSE300`
- 5 delivery slots на ближайшую дату
- Начальные остатки: 50 единиц на товар

## Быстрый запуск через Docker Compose

Из корня проекта:

```bash
docker compose up --build -d
```

Повторный запуск без пересборки:

```bash
docker compose up -d
```

После запуска:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

Остановить и удалить контейнеры:

```bash
docker compose down
```

## Локальный запуск без Docker Compose

### Backend

```bash
cd backend
./gradlew bootRun
```

Windows:

```bash
cd backend
gradlew.bat bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Тесты

Backend-тесты:

```bash
cd backend
./gradlew test
```

Если Docker daemon недоступен, Testcontainers-интеграционные тесты будут пропущены.

## Единый стиль и проверки

В репозитории зафиксированы общие правила редактирования через корневой `.editorconfig`.

### Frontend

```bash
cd frontend
npm run lint
npm run lint:fix
npm run format
npm run format:check
```

- `lint` проверяет TypeScript/React-код через ESLint
- `format` и `format:check` используют Prettier для единых отступов, импортов и форматирования UI-слоя

### Backend

```bash
cd backend
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew test
```

- `spotlessApply` и `spotlessCheck` используют Spotless + ktlint для Kotlin и Gradle Kotlin DSL
- `test` остается обязательной регрессионной проверкой после стилевых и продуктовых изменений

Перед отправкой изменений базовый минимальный набор проверок должен быть:

```bash
cd frontend && npm run lint && npm run format:check && npm run build
cd backend && ./gradlew spotlessCheck && ./gradlew test
```

## Бизнес-правила (реализовано)

- Одна активная корзина на пользователя
- Контроль остатков при добавлении в корзину и checkout
- Checkout:
  - запрет пустой корзины
  - проверка адреса пользователя
  - проверка доступности слота и лимита слота
  - проверка промокода
  - фиксация цены в `OrderItem`
  - атомарное списание остатков
  - расчёт `subtotal`, `discount`, `deliveryFee`, `total`
  - очистка корзины после успешного заказа
- Промокоды:
  - `FIXED` и `PERCENT`
  - active/inactive
  - validFrom/validTo
  - minOrderAmount
  - usageLimit
- Lifecycle заказа:
  - `CREATED -> CONFIRMED`
  - `CONFIRMED -> ASSEMBLING`
  - `ASSEMBLING -> OUT_FOR_DELIVERY`
  - `OUT_FOR_DELIVERY -> DELIVERED`
  - `CREATED|CONFIRMED -> CANCELLED`
  - клиент не может отменять после `ASSEMBLING`
- Разделение ролей:
  - CUSTOMER/ADMIN
  - `/api/admin/**` только для ADMIN

## API-группы

- `/api/auth/**`
- `/api/products/**`
- `/api/categories/**`
- `/api/cart/**`
- `/api/orders/**`
- `/api/addresses/**`
- `/api/delivery-slots/**`
- `/api/admin/products/**`
- `/api/admin/categories/**`
- `/api/admin/orders/**`
- `/api/admin/promocodes/**`
- `/api/admin/inventory/**`

## Демо-сценарии

### Customer

1. Войти под `anna@example.com / password`
2. Добавить товары в корзину
3. Перейти в Checkout
4. Выбрать адрес, слот и способ оплаты
5. Оформить заказ
6. Проверить историю заказов и детали заказа

### Admin

1. Войти под `admin@quickmart.local / password`
2. Перейти в "Админка"
3. Управлять категориями, товарами, остатками, промокодами
4. Открыть заказы и изменить статус заказа

## Оптимизации старта

- Dockerfile backend использует кэш Gradle и собирает `bootJar` без тестов
- Dockerfile frontend собирает production-бандл и отдаёт его через nginx
- В `application.yml` включена lazy initialization по умолчанию (`SPRING_MAIN_LAZY_INITIALIZATION=true`)
- В `docker-compose.yml` добавлены healthchecks и запуск сервисов по готовности зависимостей
