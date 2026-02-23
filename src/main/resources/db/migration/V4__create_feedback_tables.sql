-- 店舗マスタ（フェーズ7で拡張予定、ここでは最小限のカラムのみ）
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    store_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 商品フィードバック
CREATE TABLE product_feedbacks (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id),
    store_id BIGINT REFERENCES stores(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    satisfaction_score INTEGER NOT NULL,
    emotion_score INTEGER,
    comment TEXT,
    collection_method VARCHAR(50) NOT NULL,
    registered_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_feedbacks_recipe ON product_feedbacks (recipe_id);
CREATE INDEX idx_product_feedbacks_store ON product_feedbacks (store_id);
CREATE INDEX idx_product_feedbacks_period ON product_feedbacks (period_start, period_end);

-- フィードバック集計
CREATE TABLE feedback_summaries (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    avg_satisfaction DECIMAL(4,2) NOT NULL,
    avg_emotion DECIMAL(4,2),
    feedback_count INTEGER NOT NULL DEFAULT 0,
    main_comment_trend TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedback_summaries_recipe ON feedback_summaries (recipe_id);
CREATE INDEX idx_feedback_summaries_period ON feedback_summaries (period_start, period_end);
