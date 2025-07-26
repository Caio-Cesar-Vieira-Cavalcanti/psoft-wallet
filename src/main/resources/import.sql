SHOW TABLES;

-- Inserindo valores dos tipos de cada ativo (necessário na inicialização da aplicação)
INSERT INTO asset_type (id, name, type_discriminator) VALUES (1, 'TREASURY_BOUNDS', 'TREASURY_BOUNDS');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (2, 'STOCK', 'STOCK');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (3, 'CRYPTO', 'CRYPTO');

INSERT INTO wallet_model (id) VALUES ('1afcd28d-967e-4dd5-be8a-d692d15ac32d');

INSERT INTO user_model (user_type, id, access_code, email, full_name, city, country, neighborhood, number, state, street, zip_code, budget, plan_type, wallet_id) VALUES ('C', '8e83cd10-520a-49f4-9b65-7c3e963e2968', '789032', 'walber@gmail.com', 'Walber Wesley', 'Campina Grande', 'Brasil', 'Centro', '123', 'PB', 'Rua Projetada', '58400-000', 1000.0, 0, '1afcd28d-967e-4dd5-be8a-d692d15ac32d');

INSERT INTO asset (id, name, asset_type_id, description, is_active, quotation, quota_quantity) VALUES ('22222222-2222-2222-2222-222222222222', 'Tesla Stock', 2, 'Ações da Tesla', true, 850.0, 1000);

INSERT INTO purchase_model (id, date, quantity, state, asset_id, wallet_id) VALUES ('44444444-4444-4444-4444-444444444444', DATE '2025-07-20', 10, 'IN_WALLET', '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
INSERT INTO purchase_model (id, date, quantity, state, asset_id, wallet_id) VALUES ('55555555-5555-5555-5555-555555555555', DATE '2025-07-25', 5, 'IN_WALLET', '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
INSERT INTO purchase_model (id, date, quantity, state, asset_id, wallet_id) VALUES ('66666666-6666-6666-6666-666666666666', DATE '2025-07-30', 3, 'IN_WALLET', '22222222-2222-2222-2222-222222222222', '1afcd28d-967e-4dd5-be8a-d692d15ac32d');
