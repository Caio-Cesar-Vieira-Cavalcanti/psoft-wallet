SHOW TABLES;

-- Tipos de ativos
INSERT INTO asset_type (id, name, type_discriminator) VALUES (1, 'TREASURY_BOUNDS', 'TREASURY_BOUNDS');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (2, 'STOCK', 'STOCK');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (3, 'CRYPTO', 'CRYPTO');

-- Carteira
INSERT INTO wallet_model (id, budget) VALUES ('1afcd28d-967e-4dd5-be8a-d692d15ac32d', 1000.0);

-- Usuário
INSERT INTO user_model (user_type, id, access_code, email, full_name, city, country, neighborhood, number, state, street, zip_code, plan_type, wallet_id) VALUES ('C', '8e83cd10-520a-49f4-9b65-7c3e963e2968', '789032', 'walber@gmail.com', 'Walber Wesley', 'Campina Grande', 'Brasil', 'Centro', '123', 'PB', 'Rua Projetada', '58400-000', 0, '1afcd28d-967e-4dd5-be8a-d692d15ac32d');

-- Ativo
INSERT INTO asset (id, name, asset_type_id, description, is_active, quotation, quota_quantity) VALUES ('22222222-2222-2222-2222-222222222222', 'Tesla Stock', 2, 'Ações da Tesla', true, 850.0, 1000);

-- Trnsações
INSERT INTO transaction_model (id, date, quantity, asset_id, wallet_id) VALUES ('44444444-4444-4444-4444-444444444444', '2025-07-20', 10, '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
INSERT INTO purchase_model (id, acquisition_price, state) VALUES ('44444444-4444-4444-4444-444444444444', 850.0, 'IN_WALLET');

INSERT INTO transaction_model (id, date, quantity, asset_id, wallet_id) VALUES ('55555555-5555-5555-5555-555555555555', '2025-07-25', 5, '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
INSERT INTO purchase_model (id, acquisition_price, state) VALUES ('55555555-5555-5555-5555-555555555555', 850.0, 'IN_WALLET');

INSERT INTO transaction_model (id, date, quantity, asset_id, wallet_id) VALUES ('66666666-6666-6666-6666-666666666666', '2025-07-30', 3, '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
INSERT INTO purchase_model (id, acquisition_price, state) VALUES ('66666666-6666-6666-6666-666666666666', 850.0, 'REQUESTED');

INSERT INTO asset (id, name, asset_type_id, description, is_active, quotation, quota_quantity) VALUES ('11111111-1111-1111-1111-111111111111', 'Tesla Stock', 2, 'Ações da Tesla', false, 85.0, 1);