INSERT INTO users (id, email, password_hash, full_name, role, active, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000001', 'admin@quickmart.local', '$2a$10$O3wniFImwouffEAlzgKvm.w5VkEeduk22wodf/obJFD4RBPF3E5sC', 'Quickmart Admin', 'ADMIN', TRUE, now(), now()),
('00000000-0000-0000-0000-000000000002', 'anna@example.com', '$2a$10$O3wniFImwouffEAlzgKvm.w5VkEeduk22wodf/obJFD4RBPF3E5sC', 'Anna Petrova', 'CUSTOMER', TRUE, now(), now()),
('00000000-0000-0000-0000-000000000003', 'ivan@example.com', '$2a$10$O3wniFImwouffEAlzgKvm.w5VkEeduk22wodf/obJFD4RBPF3E5sC', 'Ivan Smirnov', 'CUSTOMER', TRUE, now(), now());

INSERT INTO carts (id, user_id, active, created_at, updated_at)
VALUES
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', TRUE, now(), now()),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', TRUE, now(), now()),
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', TRUE, now(), now());

INSERT INTO addresses (id, user_id, label, city, street, house, apartment, entrance, floor, comment, is_default, created_at, updated_at)
VALUES
('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'Home', 'Ekaterinburg', 'Lenina', '10', '25', '2', '7', NULL, TRUE, now(), now()),
('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'Home', 'Ekaterinburg', 'Malysheva', '55', '11', '1', '3', NULL, TRUE, now(), now());

INSERT INTO categories (id, name, description, active, created_at, updated_at)
VALUES
('30000000-0000-0000-0000-000000000001', 'Fruits & Vegetables', 'Fresh produce', TRUE, now(), now()),
('30000000-0000-0000-0000-000000000002', 'Dairy & Eggs', 'Milk, yogurt, cheese, eggs', TRUE, now(), now()),
('30000000-0000-0000-0000-000000000003', 'Bakery', 'Bread and pastries', TRUE, now(), now()),
('30000000-0000-0000-0000-000000000004', 'Household', 'Cleaning and home goods', TRUE, now(), now()),
('30000000-0000-0000-0000-000000000005', 'Drinks & Snacks', 'Beverages and snacks', TRUE, now(), now());

INSERT INTO products (id, name, description, price, category_id, image_url, active, created_at, updated_at)
VALUES
('40000000-0000-0000-0000-000000000001', 'Banana', 'Fresh banana 1kg', 129.00, '30000000-0000-0000-0000-000000000001', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000002', 'Apple', 'Red apples 1kg', 149.00, '30000000-0000-0000-0000-000000000001', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000003', 'Cucumber', 'Crunchy cucumbers 500g', 95.00, '30000000-0000-0000-0000-000000000001', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000004', 'Tomato', 'Sweet tomatoes 500g', 139.00, '30000000-0000-0000-0000-000000000001', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000005', 'Potato', 'Young potato 1kg', 89.00, '30000000-0000-0000-0000-000000000001', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000006', 'Milk 2.5%', 'Pasteurized milk 1L', 99.00, '30000000-0000-0000-0000-000000000002', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000007', 'Kefir', 'Classic kefir 930ml', 85.00, '30000000-0000-0000-0000-000000000002', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000008', 'Yogurt', 'Greek yogurt 400g', 119.00, '30000000-0000-0000-0000-000000000002', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000009', 'Cheese', 'Semi-hard cheese 300g', 279.00, '30000000-0000-0000-0000-000000000002', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000010', 'Eggs C1', '10 eggs pack', 129.00, '30000000-0000-0000-0000-000000000002', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000011', 'White Bread', 'Sliced white bread', 59.00, '30000000-0000-0000-0000-000000000003', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000012', 'Rye Bread', 'Traditional rye bread', 69.00, '30000000-0000-0000-0000-000000000003', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000013', 'Croissant', 'Butter croissant 2 pcs', 119.00, '30000000-0000-0000-0000-000000000003', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000014', 'Toilet Paper', '4-roll pack', 199.00, '30000000-0000-0000-0000-000000000004', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000015', 'Dishwashing Liquid', 'Lemon 450ml', 179.00, '30000000-0000-0000-0000-000000000004', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000016', 'Laundry Pods', '20 capsules', 459.00, '30000000-0000-0000-0000-000000000004', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000017', 'Mineral Water', 'Still water 1.5L', 55.00, '30000000-0000-0000-0000-000000000005', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000018', 'Orange Juice', 'Juice 1L', 189.00, '30000000-0000-0000-0000-000000000005', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000019', 'Potato Chips', 'Sour cream 140g', 129.00, '30000000-0000-0000-0000-000000000005', NULL, TRUE, now(), now()),
('40000000-0000-0000-0000-000000000020', 'Dark Chocolate', '70% cocoa 90g', 149.00, '30000000-0000-0000-0000-000000000005', NULL, TRUE, now(), now());

INSERT INTO inventory_stocks (id, product_id, available_quantity, version, created_at, updated_at)
SELECT
    ('50000000-0000-0000-0000-' || LPAD(ROW_NUMBER() OVER (ORDER BY id)::text, 12, '0'))::uuid,
    id,
    50,
    0,
    now(),
    now()
FROM products;

INSERT INTO promo_codes (id, code, type, value, min_order_amount, active, valid_from, valid_to, usage_limit, used_count, created_at, updated_at)
VALUES
('60000000-0000-0000-0000-000000000001', 'WELCOME100', 'FIXED', 100.00, 500.00, TRUE, now() - interval '5 days', now() + interval '60 days', 1000, 0, now(), now()),
('60000000-0000-0000-0000-000000000002', 'SAVE10', 'PERCENT', 10.00, 1000.00, TRUE, now() - interval '5 days', now() + interval '60 days', 1000, 0, now(), now()),
('60000000-0000-0000-0000-000000000003', 'HOUSE300', 'FIXED', 300.00, 2000.00, FALSE, now() - interval '5 days', now() + interval '60 days', NULL, 0, now(), now());

INSERT INTO delivery_slots (id, slot_date, start_time, end_time, order_limit, active, created_at, updated_at)
VALUES
('70000000-0000-0000-0000-000000000001', (current_date + interval '1 day')::date, '08:00', '10:00', 30, TRUE, now(), now()),
('70000000-0000-0000-0000-000000000002', (current_date + interval '1 day')::date, '10:00', '12:00', 30, TRUE, now(), now()),
('70000000-0000-0000-0000-000000000003', (current_date + interval '1 day')::date, '12:00', '14:00', 30, TRUE, now(), now()),
('70000000-0000-0000-0000-000000000004', (current_date + interval '1 day')::date, '14:00', '16:00', 30, TRUE, now(), now()),
('70000000-0000-0000-0000-000000000005', (current_date + interval '1 day')::date, '16:00', '18:00', 30, TRUE, now(), now());
