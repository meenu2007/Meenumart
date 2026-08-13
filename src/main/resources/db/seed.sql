-- Admin seed account (password: Admin@123 — hash generated via PasswordUtil, replace before real use)
INSERT INTO users (name, email, password_hash, role) VALUES
('Admin', 'admin@meenumart.com', '$2a$12$replaceWithRealBcryptHashAtStartup', 'ADMIN');

INSERT INTO users (name, email, password_hash, role) VALUES
('Test Seller', 'seller1@meenumart.com', '$2a$12$replaceWithRealBcryptHashAtStartup', 'SELLER'),
('Test Buyer', 'buyer1@meenumart.com', '$2a$12$replaceWithRealBcryptHashAtStartup', 'BUYER');

INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
(2, 'Wireless Mouse', 'Ergonomic wireless mouse', 799.00, 50, 'Electronics', 'https://example.com/mouse.jpg'),
(2, 'Notebook Set', 'Pack of 3 ruled notebooks', 199.00, 100, 'Stationery', 'https://example.com/notebook.jpg');
