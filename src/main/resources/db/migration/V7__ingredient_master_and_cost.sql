-- =============================================
-- V7: 食材マスタ拡張 & 原価管理
-- =============================================

-- ingredientsテーブル拡張
ALTER TABLE ingredients ADD COLUMN category VARCHAR(100);
ALTER TABLE ingredients ADD COLUMN standard_unit VARCHAR(50);
ALTER TABLE ingredients ADD COLUMN seasonal_flag BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ingredients ADD COLUMN supply_status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE';
ALTER TABLE ingredients ADD COLUMN supplier VARCHAR(200);

CREATE INDEX idx_ingredients_category ON ingredients (category);
CREATE INDEX idx_ingredients_supply_status ON ingredients (supply_status);

-- 食材価格履歴
CREATE TABLE ingredient_prices (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    unit_price DECIMAL(10, 2) NOT NULL,
    price_per_unit VARCHAR(50),
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ingredient_prices_ingredient ON ingredient_prices (ingredient_id);
CREATE INDEX idx_ingredient_prices_effective ON ingredient_prices (ingredient_id, effective_from);

-- 食材旬情報
CREATE TABLE ingredient_seasons (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    month INTEGER NOT NULL CHECK (month >= 1 AND month <= 12),
    availability_rank VARCHAR(20) NOT NULL,
    quality_note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (ingredient_id, month)
);

CREATE INDEX idx_ingredient_seasons_ingredient ON ingredient_seasons (ingredient_id);

-- レシピ原価
CREATE TABLE recipe_costs (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL UNIQUE REFERENCES recipes(id),
    total_ingredient_cost DECIMAL(12, 2),
    target_margin_rate DECIMAL(5, 2),
    recommended_price DECIMAL(12, 2),
    current_price DECIMAL(12, 2),
    last_calculated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipe_costs_recipe ON recipe_costs (recipe_id);
