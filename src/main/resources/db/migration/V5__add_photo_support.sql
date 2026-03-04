-- V5: Add photo support to recipes, cooking steps, and service designs
ALTER TABLE recipes ADD COLUMN image_url VARCHAR(1024);
ALTER TABLE cooking_steps ADD COLUMN image_url VARCHAR(1024);
ALTER TABLE service_designs ADD COLUMN plating_image_url VARCHAR(1024);
