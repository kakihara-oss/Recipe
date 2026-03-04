-- 店舗マスタにlocationカラムを追加
ALTER TABLE stores ADD COLUMN location VARCHAR(500);

-- 月次売上データ（POSデータCSV取込先）
CREATE TABLE monthly_sales (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    recipe_id BIGINT NOT NULL REFERENCES recipes(id),
    target_month VARCHAR(7) NOT NULL,  -- YYYY-MM形式
    quantity INTEGER NOT NULL DEFAULT 0,
    sales_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (store_id, recipe_id, target_month)
);

CREATE INDEX idx_monthly_sales_store ON monthly_sales (store_id);
CREATE INDEX idx_monthly_sales_recipe ON monthly_sales (recipe_id);
CREATE INDEX idx_monthly_sales_month ON monthly_sales (target_month);

-- 店舗月次理論原価
CREATE TABLE store_monthly_food_costs (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    target_month VARCHAR(7) NOT NULL,  -- YYYY-MM形式
    theoretical_food_cost DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_sales DECIMAL(12,2) NOT NULL DEFAULT 0,
    theoretical_food_cost_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (store_id, target_month)
);

CREATE INDEX idx_store_monthly_food_costs_store ON store_monthly_food_costs (store_id);
CREATE INDEX idx_store_monthly_food_costs_month ON store_monthly_food_costs (target_month);
