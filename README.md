# 感動創出レシピツール（Kando Recipe Manager）

料理とサービスを通じてお客様の人生に感動を届けるためのレシピ管理プラットフォーム。

## 技術スタック

- Java 17
- Spring Boot 3.x
- PostgreSQL
- Flyway（DBマイグレーション）
- Spring Security + Google OAuth2/OIDC
- JWT認証
- Gradle

## 前提条件

- Java 17以上
- PostgreSQL 14以上
- Google Cloud Platformプロジェクト（OAuth2クライアントID発行済み）

## セットアップ

### 1. データベースの準備

PostgreSQLにデータベースとユーザーを作成します。

```bash
psql -U postgres
```

```sql
CREATE DATABASE recipe_manager;
CREATE USER recipe_user WITH PASSWORD 'recipe_pass';
GRANT ALL PRIVILEGES ON DATABASE recipe_manager TO recipe_user;
```

### 2. Google OAuth2の設定

[Google Cloud Console](https://console.cloud.google.com/) でOAuth2クライアントIDを作成します。

1. 「APIとサービス」>「認証情報」からOAuth 2.0クライアントIDを作成
2. アプリケーションの種類: ウェブアプリケーション
3. 承認済みリダイレクトURI: `http://localhost:8080/login/oauth2/code/google`
4. クライアントIDとクライアントシークレットを控えておく

### 3. 環境変数の設定

以下の環境変数を設定します。`.env`ファイルを作成するか、シェルで直接exportしてください。

```bash
# Google OAuth2（必須）
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret

# 許可ドメイン（Google Workspaceのドメイン）
export ALLOWED_DOMAIN=your-company.com

# JWT設定（本番環境では必ず変更）
export JWT_SECRET=your-256-bit-secret-key-here
export JWT_EXPIRATION_MS=86400000

# データベース（デフォルト値を変更する場合）
export DB_USERNAME=recipe_user
export DB_PASSWORD=recipe_pass

# フロントエンドURL
export FRONTEND_URL=http://localhost:3000
```

### 4. ビルドと起動

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション起動（Flywayマイグレーションは起動時に自動実行）
./gradlew bootRun
```

サーバーは `http://localhost:8080` で起動します。

## 認証フロー

1. ブラウザで `http://localhost:8080/oauth2/authorization/google` にアクセス
2. Googleアカウントでログイン（`ALLOWED_DOMAIN`で設定したドメインのアカウントのみ許可）
3. 初回ログイン時にユーザーが自動作成される
4. 認証成功後、フロントエンドにJWTトークンが返される
5. 以降のAPIリクエストには`Authorization: Bearer <JWT>`ヘッダーを付与する

## API一覧

ベースURL: `http://localhost:8080`

### ヘルスチェック

| メソッド | パス | 説明 | 認証 |
|---------|------|------|------|
| GET | `/api/health` | ヘルスチェック | 不要 |

### ユーザー管理

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| GET | `/api/users` | ユーザー一覧取得 | 全ロール |
| GET | `/api/users/me` | ログインユーザー情報取得 | 全ロール |
| PUT | `/api/users/{id}/role` | ロール変更 | PRODUCER |

### レシピ管理

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| POST | `/api/recipes` | レシピ作成 | CHEF, PRODUCER |
| GET | `/api/recipes` | レシピ一覧取得 | 全ロール |
| GET | `/api/recipes/{id}` | レシピ詳細取得 | 全ロール |
| PUT | `/api/recipes/{id}` | レシピ更新 | CHEF, PRODUCER |
| PUT | `/api/recipes/{id}/service-design` | サービス設計更新 | CHEF, SERVICE, PRODUCER |
| PUT | `/api/recipes/{id}/experience-design` | 体験設計更新 | CHEF, SERVICE, PRODUCER |
| PUT | `/api/recipes/{id}/status` | ステータス変更 | CHEF, PRODUCER |
| DELETE | `/api/recipes/{id}` | レシピ削除（論理削除） | CHEF, PRODUCER |
| GET | `/api/recipes/{id}/history` | 変更履歴取得 | 全ロール |

レシピ一覧のクエリパラメータ:
- `category` - カテゴリでフィルタ（任意）
- `status` - ステータスでフィルタ: `DRAFT`, `PUBLISHED`, `ARCHIVED`（任意）
- `page` - ページ番号（デフォルト: 0）
- `size` - ページサイズ（デフォルト: 20、最大: 100）

### ナレッジ管理

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| GET | `/api/knowledge/categories` | カテゴリ一覧取得 | 全ロール |
| POST | `/api/knowledge/articles` | 記事作成 | 全ロール |
| GET | `/api/knowledge/articles` | 記事一覧取得 | 全ロール |
| GET | `/api/knowledge/articles/{id}` | 記事詳細取得 | 全ロール |
| GET | `/api/knowledge/articles/search?keyword=xxx` | 記事検索 | 全ロール |
| PUT | `/api/knowledge/articles/{id}` | 記事更新 | 投稿者本人 or PRODUCER |
| DELETE | `/api/knowledge/articles/{id}` | 記事削除 | 投稿者本人 or PRODUCER |

### AI相談

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| POST | `/api/ai/threads` | 相談スレッド作成 | 全ロール |
| GET | `/api/ai/threads` | 自分のスレッド一覧取得 | 全ロール |
| GET | `/api/ai/threads/{threadId}` | スレッド詳細取得 | スレッド作成者 |
| GET | `/api/ai/threads/{threadId}/messages` | メッセージ一覧取得 | スレッド作成者 |
| POST | `/api/ai/threads/{threadId}/messages` | メッセージ送信 | スレッド作成者 |

### フィードバック

| メソッド | パス | 説明 | 権限 |
|---------|------|------|------|
| POST | `/api/feedbacks` | フィードバック登録 | CHEF, SERVICE, PRODUCER |
| GET | `/api/feedbacks` | フィードバック一覧取得 | 全ロール |
| GET | `/api/feedbacks/{id}` | フィードバック詳細取得 | 全ロール |
| DELETE | `/api/feedbacks/{id}` | フィードバック削除 | 登録者本人 or PRODUCER |
| POST | `/api/feedbacks/summaries/generate` | サマリー生成 | 全ロール |
| GET | `/api/feedbacks/summaries?recipeId=xxx` | サマリー一覧取得 | 全ロール |
| GET | `/api/feedbacks/summaries/{id}` | サマリー詳細取得 | 全ロール |
| GET | `/api/feedbacks/summaries/trend?recipeId=xxx` | 推移取得 | 全ロール |

## APIリクエスト例

### レシピ作成

```bash
curl -X POST http://localhost:8080/api/recipes \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "季節の前菜盛り合わせ",
    "description": "旬の食材を活かした五感で楽しむ前菜",
    "category": "前菜",
    "servings": 2,
    "concept": "季節の移ろいを一皿で表現する",
    "story": "日本の四季を感じる食材と調理法の融合",
    "cookingSteps": [
      {
        "stepNumber": 1,
        "description": "季節の野菜を丁寧に下処理する",
        "durationMinutes": 15,
        "tips": "野菜は氷水でシャキッと仕上げる"
      }
    ],
    "ingredients": [
      {
        "ingredientId": 1,
        "quantity": 100,
        "unit": "g",
        "preparationNote": "薄切り"
      }
    ]
  }'
```

### AI相談スレッド作成

```bash
curl -X POST http://localhost:8080/api/ai/threads \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "theme": "新メニュー開発",
    "recipeId": 1,
    "initialMessage": "春の記念日コースに合う前菜のアイデアを相談したいです"
  }'
```

### フィードバック登録

```bash
curl -X POST http://localhost:8080/api/feedbacks \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipeId": 1,
    "storeId": 1,
    "periodStart": "2026-02-01",
    "periodEnd": "2026-02-28",
    "satisfactionScore": 4,
    "emotionScore": 5,
    "comment": "盛り付けの演出にお客様が感動されていた",
    "collectionMethod": "SURVEY"
  }'
```

## ロールと権限

| ロール | 説明 |
|--------|------|
| CHEF | 調理の専門家。レシピの作成・編集・削除が可能 |
| SERVICE | 接客の専門家。サービス設計・体験設計の編集とフィードバック登録が可能 |
| PURCHASER | 食材調達の専門家。食材マスタ・原価情報の管理が可能 |
| PRODUCER | 総合プロデューサー。全操作が可能。ロール管理を含む |

## テスト

```bash
# 全テスト実行
./gradlew test

# テスト結果はビルドレポートで確認
# build/reports/tests/test/index.html
```

テストではH2インメモリデータベースを使用するため、PostgreSQLの起動は不要です。
