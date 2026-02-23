# CLAUDE.md - レシピ管理システム（Recipe Manager）

## プロジェクト概要

外食企業向けのレシピ管理アプリケーション。
シェフがレシピを作成・入力し、食材情報・調理手順・サービス方法を一元管理する。
シェフ以外の社員も随時レシピを更新でき、全変更は履歴として記録される。

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

- Recipe: レシピ本体（タイトル、説明、カテゴリ、人数、ステータス）
- RecipeIngredient: 食材情報（食材名、分量、単位、下処理メモ）
- CookingStep: 調理手順（手順番号、説明、所要時間、温度、コツ）
- ServingInstruction: サービス方法（盛り付け・提供の指示）
- User: ユーザー（CHEF / STAFF / ADMIN）
- RecipeHistory: 更新履歴（誰が・いつ・何を変更したか）

## 権限モデル

| 操作 | CHEF | STAFF | ADMIN |
|------|------|-------|-------|
| レシピ作成 | o | x | o |
| レシピ更新 | o | o | o |
| レシピ削除 | o | x | o |
| ステータス変更（公開） | o | x | o |
| レシピ閲覧 | o | o | o |

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
