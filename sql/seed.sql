USE retail_db;
INSERT INTO categories(name) VALUES ('General') ON DUPLICATE KEY UPDATE name=VALUES(name);
-- The first administrator is created securely by the application's first-run setup.
