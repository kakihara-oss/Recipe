-- ナレッジカテゴリ
CREATE TABLE knowledge_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初期カテゴリデータ
INSERT INTO knowledge_categories (name, description, sort_order) VALUES
    ('調理技法', '食材の活かし方、調理の工夫、プロの技', 1),
    ('演出方法', '盛り付け、照明、音楽、香り、サプライズ演出', 2),
    ('サービス手法', '接客話法、おもてなしの心得、クレーム対応の好事例', 3),
    ('発信方法', 'SNS映え、メニュー説明文の書き方、ストーリーの伝え方', 4),
    ('歴史・文化', '料理の起源、食文化、地域性、食材の物語', 5),
    ('感動事例', '実際にお客様に感動を届けた事例、成功パターン', 6);

-- ナレッジ記事
CREATE TABLE knowledge_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES knowledge_categories(id),
    tags VARCHAR(500),
    author_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_articles_category ON knowledge_articles (category_id);
CREATE INDEX idx_knowledge_articles_author ON knowledge_articles (author_id);

-- ナレッジ記事×レシピ連携
CREATE TABLE knowledge_article_recipes (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
    recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    UNIQUE(article_id, recipe_id)
);

-- AI相談スレッド
CREATE TABLE ai_consultation_threads (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    recipe_id BIGINT REFERENCES recipes(id),
    theme VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_threads_user ON ai_consultation_threads (user_id);

-- AI相談メッセージ
CREATE TABLE ai_consultation_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL REFERENCES ai_consultation_threads(id) ON DELETE CASCADE,
    sender_type VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_messages_thread ON ai_consultation_messages (thread_id);

-- メッセージ×参照ナレッジ
CREATE TABLE ai_message_knowledge_refs (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES ai_consultation_messages(id) ON DELETE CASCADE,
    article_id BIGINT NOT NULL REFERENCES knowledge_articles(id),
    UNIQUE(message_id, article_id)
);
