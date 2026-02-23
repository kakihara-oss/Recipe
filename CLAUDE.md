# CLAUDE.md - 工数管理システム（TimeTracker）

## プロジェクト概要

Spring Boot 3.x Java 17 で構築する社内工数管理ツール。
REST APIを提供し、フロントエンドはReact（別リポジトリ）から呼び出す。

## コーディング規約

### 命名規則

- 変数名・メソッド名: lowerCamelCase
  - 良い例: userId, getOrderCount(), isActive
  - 悪い例: user_id, UserID, get_order_count()
- クラス名: UpperCamelCase
  - 良い例: UserService, OrderRepository
  - 悪い例: userService, user_service
- 定数: UPPER_SNAKE_CASE
  - 良い例: MAX_RETRY_COUNT, DEFAULT_PAGE_SIZE
  - 悪い例: maxRetryCount, max_retry_count

### 例外処理

- 素のRuntimeExceptionは禁止。必ずCustomExceptionを継承した例外を使う
- 理由: 例外の種類によってHTTPステータスコードを自動マッピングするため
  - BusinessLogicException → 400 Bad Request
  - ResourceNotFoundException → 404 Not Found
  - ApplicationException → 500 Internal Server Error

### Repository層

- 戻り値はOptional型で統一する
  - 良い例: Optional<User> findByUserId(String userId)
  - 悪い例: User findByUserId(String userId) // nullを返す可能性

## アーキテクチャ

- Controller → Service → Repository の3層構造を守る
- 各層の責務:
  - Controller: リクエストの受付、バリデーション、レスポンス整形
  - Service: ビジネスロジック
  - Repository: データアクセス
- DTOとEntityは必ず分離する。Entityを直接レスポンスに使わない

## 禁止事項

- System.out.println()でのログ出力 → SLF4J（@Slf4j）を使うこと
- マジックナンバーの直書き → 定数クラスに定義すること
- SQLの直書き → Spring Data JPAのRepositoryメソッドを使うこと
- Controllerから直接Repositoryを呼ぶこと → 必ずServiceを経由すること
