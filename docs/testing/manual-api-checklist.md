# Ручная проверка backend API

## Назначение

Документ нужен для короткой ручной smoke-проверки backend API и для сверки demo-данных. Канонический контракт endpoint-ов остается в Swagger/OpenAPI.

## Адреса локальной среды

- Backend API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Demo accounts

- Администратор: `admin@quickmart.local` / `password`
- Покупатель: `anna@example.com` / `password`
- Покупатель: `ivan@example.com` / `password`

## Полезные seed-данные

- активные промокоды: `WELCOME100`, `SAVE10`
- у seeded customer accounts есть адреса доставки
- delivery slot IDs не фиксированы; актуальные значения нужно получать через `GET /api/delivery-slots`

## Минимальный customer smoke

1. Выполнить `POST /api/auth/login` и получить JWT customer.
2. Проверить публичные endpoints:
   - `GET /api/products`
   - `GET /api/categories`
   - `GET /api/delivery-slots`
3. Проверить защищенный customer flow:
   - `GET /api/addresses`
   - `POST /api/cart/items`
   - `GET /api/cart`
   - `POST /api/orders/checkout`
   - `GET /api/orders`
   - `GET /api/orders/{id}`

## Минимальный admin smoke

1. Выполнить `POST /api/auth/login` под `admin@quickmart.local`.
2. Проверить:
   - `GET /api/admin/products`
   - `GET /api/admin/categories`
   - `GET /api/admin/inventory`
   - `GET /api/admin/orders`
   - `GET /api/admin/promocodes`
3. При необходимости выполнить мутацию:
   - создать или обновить товар;
   - обновить остаток через `PUT /api/admin/inventory/{productId}`;
   - обновить статус заказа через `PUT /api/admin/orders/{id}/status`.

## Дополнительная проверка Kafka

Если backend запущен с `APP_KAFKA_ENABLED=true`:

1. Выполнить checkout.
2. Изменить статус заказа или отменить заказ.
3. Проверить `GET /api/admin/orders/{id}/events`.
4. При использовании Docker Compose c `infra` профилем сверить данные в Kafka UI.

## Что важно помнить

- административные endpoints требуют JWT с ролью `ADMIN`;
- пользовательские защищенные endpoints требуют JWT текущего пользователя;
- формат ошибок описывается `ApiErrorResponse` в Swagger.
