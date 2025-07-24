SHOW TABLES;

-- Inserindo valores dos tipos de cada ativo (necessário na inicialização da aplicação)
INSERT INTO asset_type (id, name, type_discriminator) VALUES (1, 'TREASURY_BOUNDS', 'TREASURY_BOUNDS');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (2, 'STOCK', 'STOCK');
INSERT INTO asset_type (id, name, type_discriminator) VALUES (3, 'CRYPTO', 'CRYPTO');