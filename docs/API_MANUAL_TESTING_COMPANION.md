# API: Памятка для ручного тестирования

## Назначение
Документ содержит служебные данные для ручной проверки backend API Quickmart.
Swagger/OpenAPI остается каноническим источником контрактов endpoint-ов.

## Адреса среды
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Тестовые учетные записи
- Администратор: `admin@quickmart.local` / `password`
- Пользователь: `anna@example.com` / `password`
- Пользователь: `ivan@example.com` / `password`

## Стабильные seed-данные
- Промокод `SAVE10`: процентная скидка.
- Промокод `WELCOME100`: фиксированная скидка.
- Для `anna@example.com` и `ivan@example.com` в seed присутствуют адреса доставки.
- Идентификаторы слотов доставки не фиксируются заранее. Получайте актуальный `id` через `GET /api/delivery-slots`.

## Минимальная smoke-последовательность
1. Выполнить `POST /api/auth/login` и получить JWT.
2. Проверить `GET /api/products` и `GET /api/categories`.
3. Проверить `GET /api/delivery-slots` и выбрать активный слот.
4. Проверка пользовательского потока:
   - `GET /api/addresses`
   - `POST /api/cart/items`
   - `POST /api/orders/checkout`
   - `GET /api/orders`
   - `GET /api/orders/{id}`
5. Проверка административного потока:
   - `GET /api/admin/products`
   - создание/обновление товара
   - `PUT /api/admin/inventory/{productId}`
   - `PUT /api/admin/orders/{id}/status`

## Примечания
- Административные endpoint-ы требуют JWT пользователя с ролью `ADMIN`.
- Пользовательские endpoint-ы требуют JWT аутентифицированного пользователя, кроме публичных операций.
- Формат ошибок: `ApiErrorResponse`.
