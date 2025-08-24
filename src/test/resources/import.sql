SHOW TABLES;

-- Inserindo valores dos tipos de cada ativo (necessário na inicialização da aplicação)
INSERT INTO asset_type (id, name, type_discriminator) VALUES (1, 'TREASURY_BOUNDS', 'TREASURY_BOUNDS');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (2, 'STOCK', 'STOCK');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (3, 'CRYPTO', 'CRYPTO');

-- Inserindo admin para testes (usando o mesmo do AdminService)
INSERT INTO user_model (id, full_name, email, access_code, dtype) VALUES 
('00000000-0000-0000-0000-000000000001', 'admin', 'admin@example.com', '123456', 'A');
