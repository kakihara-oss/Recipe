# CLAUDE.md - レシピ管理システム（Recipe Manager）

## プロジェクト概要

外食企業向けのレシピ管理アプリケーション。
シェフがレシピを作成・入力し、食材情報・調理手順・サービス方法を一元管理する。
シェフ以外の社員も随時レシピを更新でき、全変更は履歴として記録される。
食材調達係が食材マスタ・原価情報を管理し、季節変動や価格変動に応じて
レシピ情報を最小限の手間で最適化できる仕組みを提供する。

- バックエンド: Spring Boot 3.x / Java 17 / Gradle
- データベース: PostgreSQL + Flyway（マイグレーション）
- 認証: Spring Security + JWT
- API形式: REST API（フロントエンドはReact別リポジトリ）

## パッケージ構成

```
com.recipe.manager
├── config/          # Security, Web, DB設定
├── controller/      # REST Controller
├── service/         # ビジネスロジック
├── repository/      # Spring Data JPA Repository
├── entity/          # JPA Entity
├── dto/
│   ├── request/     # リクエストDTO
│   └── response/    # レスポンスDTO
├── exception/       # カスタム例外
├── security/        # JWT, 認証・認可
└── common/          # 定数, ユーティリティ
```

## 主要エンティティ

### レシピ関連
- Recipe: レシピ本体（タイトル、説明、カテゴリ、人数、ステータス）
- RecipeIngredient: レシピ内の食材情報（食材マスタへの参照、使用量、単位、下処理メモ、代替食材）
- CookingStep: 調理手順（手順番号、説明、所要時間、温度、コツ）
- ServingInstruction: サービス方法（盛り付け・提供の指示）

### 食材マスタ関連
- Ingredient: 食材マスタ（食材名、カテゴリ、標準単位、季節フラグ、供給状態、仕入先）
- IngredientPrice: 食材価格履歴（単価、有効開始日、有効終了日） ※時系列で価格変動を追跡
- IngredientSeason: 食材の旬情報（対象月、入手性ランク、品質メモ）

### 原価・売価関連
- RecipeCost: レシピ原価情報（食材原価合計、目標粗利率、推奨売価、現在売価、最終計算日時）

### ユーザー・履歴関連
- User: ユーザー（CHEF / STAFF / PURCHASER / ADMIN）
- RecipeHistory: 更新履歴（誰が・いつ・何を変更したか）

### ステータス管理
- Recipeステータス: DRAFT → PUBLISHED ←→ ARCHIVED → DELETED（論理削除）
  - DRAFT: 下書き。作成中のレシピ
  - PUBLISHED: 公開中。店舗で使用中
  - ARCHIVED: アーカイブ。季節メニュー終了時など一時的に非公開。再公開可能
  - DELETED: 論理削除。通常のクエリでは取得されない
- Ingredient供給状態: AVAILABLE（供給可能）/ LIMITED（供給不安定）/ UNAVAILABLE（供給停止）/ SEASONAL（季節限定）

## 権限モデル

| 操作 | CHEF | STAFF | PURCHASER | ADMIN |
|------|------|-------|-----------|-------|
| レシピ作成 | o | x | x | o |
| レシピ更新 | o | o | x | o |
| レシピ削除 | o | x | x | o |
| ステータス変更（公開・アーカイブ） | o | x | x | o |
| レシピ閲覧 | o | o | o | o |
| 食材マスタ登録・更新 | x | x | o | o |
| 食材価格更新 | x | x | o | o |
| 食材供給状態変更 | x | x | o | o |
| 原価・売価情報の閲覧 | o | x | o | o |
| 売価の更新 | o | x | o | o |
| 影響レシピ一覧の取得 | o | o | o | o |

## コーディング規約

### 命名規則

- 変数名・メソッド名: lowerCamelCase
  - 良い例: recipeId, getIngredientList(), isPublished
  - 悪い例: recipe_id, RecipeID, get_ingredient_list()
- クラス名: UpperCamelCase
  - 良い例: RecipeService, CookingStepRepository
  - 悪い例: recipeService, recipe_service
- 定数: UPPER_SNAKE_CASE
  - 良い例: MAX_RECIPE_TITLE_LENGTH, DEFAULT_PAGE_SIZE
  - 悪い例: maxRecipeTitleLength, max_recipe_title_length

### 例外処理

- 素のRuntimeExceptionは禁止。必ずカスタム例外を使う
- 例外の種類によってHTTPステータスコードを自動マッピングする:
  - BusinessLogicException → 400 Bad Request
  - ResourceNotFoundException → 404 Not Found
  - UnauthorizedException → 401 Unauthorized
  - ForbiddenException → 403 Forbidden
  - ApplicationException → 500 Internal Server Error

### Repository層

- 戻り値はOptional型で統一する
  - 良い例: Optional<Recipe> findByRecipeId(Long recipeId)
  - 悪い例: Recipe findByRecipeId(Long recipeId)

## アーキテクチャ

- Controller → Service → Repository の3層構造を守る
- 各層の責務:
  - Controller: リクエストの受付、バリデーション、レスポンス整形
  - Service: ビジネスロジック、権限チェック、トランザクション管理
  - Repository: データアクセス
- DTOとEntityは必ず分離する。Entityを直接レスポンスに使わない
- レシピ更新時は必ずRecipeHistoryに変更履歴を記録する

### 食材マスタと原価管理のルール

- 食材はマスタ（Ingredient）で一元管理し、RecipeIngredientはマスタへの参照を持つ
  - 同じ食材を複数レシピで共有し、価格変更を1箇所で反映可能にする
- 食材価格はIngredientPriceで時系列管理する（有効開始日〜有効終了日）
  - 現在有効な価格 = 有効開始日 <= 現在日 かつ 有効終了日がnullまたは現在日以降
- レシピ原価は自動計算する:
  - レシピ原価 = Σ（各食材の現在単価 × 使用量）
  - 推奨売価 = レシピ原価 ÷ (1 - 目標粗利率)
- 食材の価格・供給状態が変更された場合、影響を受けるレシピの一覧を取得できるAPIを提供する
- 食材変更時の通知フロー:
  1. 調達係が食材マスタ（価格・供給状態）を更新
  2. 影響を受けるレシピの原価を再計算
  3. 粗利率が目標を下回るレシピを警告リストとして返す
  4. シェフが警告リストを確認し、レシピ見直し or 売価変更を判断

### アーカイブ機能のルール

- アーカイブは論理的な非公開状態であり、データは保持される
- アーカイブされたレシピは通常の一覧APIでは返さない（フィルタで取得可能）
- アーカイブからPUBLISHEDへの復元が可能（季節メニューの再開など）
- アーカイブ時・復元時もRecipeHistoryに記録する
- 食材のUNAVAILABLE化に伴い、該当食材を使うレシピの一括アーカイブ候補を提示できる

## テスト方針

- Service層: JUnit 5 + Mockito で単体テスト必須
- Controller層: @WebMvcTest でAPIテスト
- Repository層: @DataJpaTest でクエリ検証
- テストメソッド名は日本語可: `レシピ作成_正常系_シェフが作成できる()`

## ビルド・実行コマンド

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション起動
./gradlew bootRun

# Flywayマイグレーション
./gradlew flywayMigrate
```

## 禁止事項

- System.out.println()でのログ出力 → SLF4J（@Slf4j）を使うこと
- マジックナンバーの直書き → 定数クラスに定義すること
- SQLの直書き → Spring Data JPAのRepositoryメソッドを使うこと
- Controllerから直接Repositoryを呼ぶこと → 必ずServiceを経由すること
- Entityをレスポンスに直接返すこと → 必ずDTOに変換すること
- レシピ更新時に履歴記録を省略すること → 必ずRecipeHistoryに記録すること
- RecipeIngredientに食材名を直接保持すること → 必ずIngredient（食材マスタ）への参照を使うこと
- 食材価格をIngredientエンティティに直接保持すること → 必ずIngredientPrice（価格履歴）で時系列管理すること
- 原価計算をController層で行うこと → 必ずService層で計算ロジックを実装すること
- レシピの物理削除 → 必ず論理削除（ステータスをDELETEDに変更）を使うこと
