-- 店舗マスタ拡張（所在地追加）
ALTER TABLE stores
    ADD COLUMN location VARCHAR(500);

-- 月次売上データ（POSデータCSVから取込）
CREATE TABLE monthly_sales (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    recipe_id BIGINT NOT NULL REFERENCES recipes(id),
    sales_month VARCHAR(7) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    sales_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (store_id, recipe_id, sales_month)
);

CREATE INDEX idx_monthly_sales_store ON monthly_sales (store_id);
CREATE INDEX idx_monthly_sales_recipe ON monthly_sales (recipe_id);
CREATE INDEX idx_monthly_sales_month ON monthly_sales (sales_month);
CREATE INDEX idx_monthly_sales_store_month ON monthly_sales (store_id, sales_month);

-- 店舗月次理論原価（計算結果スナップショット）
CREATE TABLE store_monthly_food_costs (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    sales_month VARCHAR(7) NOT NULL,
    theoretical_food_cost DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_sales DECIMAL(12, 2) NOT NULL DEFAULT 0,
    theoretical_food_cost_rate DECIMAL(5, 2) NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (store_id, sales_month)
);

CREATE INDEX idx_store_monthly_food_costs_store ON store_monthly_food_costs (store_id);
CREATE INDEX idx_store_monthly_food_costs_month ON store_monthly_food_costs (sales_month);
