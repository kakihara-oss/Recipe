-- V6: Add photo support to knowledge articles and ingredients
ALTER TABLE knowledge_articles ADD COLUMN image_url VARCHAR(1024);
ALTER TABLE ingredients ADD COLUMN image_url VARCHAR(1024);
