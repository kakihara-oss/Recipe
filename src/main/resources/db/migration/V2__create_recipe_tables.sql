-- 食材マスタ（最小限。フェーズ6で拡張）
CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- レシピ本体
CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(100),
    servings INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    concept TEXT,
    story TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipes_status ON recipes (status);
CREATE INDEX idx_recipes_created_by ON recipes (created_by);
CREATE INDEX idx_recipes_category ON recipes (category);

-- レシピ食材
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
    quantity DECIMAL(10, 2),
    unit VARCHAR(50),
    preparation_note VARCHAR(500),
    substitutes VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipe_ingredients_recipe ON recipe_ingredients (recipe_id);

-- 調理手順
CREATE TABLE cooking_steps (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER,
    temperature VARCHAR(100),
    tips TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cooking_steps_recipe ON cooking_steps (recipe_id);

-- サービス設計（1レシピにつき1つ）
CREATE TABLE service_designs (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL UNIQUE REFERENCES recipes(id) ON DELETE CASCADE,
    plating_instructions TEXT,
    service_method TEXT,
    customer_script TEXT,
    staging_method TEXT,
    timing TEXT,
    storytelling TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 体験設計（1レシピにつき1つ）
CREATE TABLE experience_designs (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL UNIQUE REFERENCES recipes(id) ON DELETE CASCADE,
    target_scene TEXT,
    emotional_key_points TEXT,
    special_occasion_support TEXT,
    seasonal_presentation TEXT,
    sensory_appeal TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 変更履歴
CREATE TABLE recipe_histories (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id),
    changed_by BIGINT NOT NULL REFERENCES users(id),
    change_type VARCHAR(50) NOT NULL,
    changed_fields TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipe_histories_recipe ON recipe_histories (recipe_id);
CREATE INDEX idx_recipe_histories_changed_at ON recipe_histories (changed_at);
