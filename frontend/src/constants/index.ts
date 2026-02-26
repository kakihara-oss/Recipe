import type { Role } from '../types'

export const DEFAULT_PAGE_SIZE = 20
export const MAX_PAGE_SIZE = 100

export const MAX_RECIPE_TITLE_LENGTH = 200
export const MAX_RECIPE_DESCRIPTION_LENGTH = 2000

export const SCORE_MIN = 1
export const SCORE_MAX = 5

export const ROLE_LABELS: Record<Role, string> = {
  CHEF: 'シェフ',
  SERVICE: 'サービス',
  PURCHASER: '食材調達',
  PRODUCER: 'プロデューサー',
}

export const COLLECTION_METHOD_LABELS = {
  SURVEY: 'アンケート',
  INTERVIEW: 'インタビュー',
  SNS: 'SNS',
  DIRECT: '直接',
  OTHER: 'その他',
} as const

export const STATUS_LABELS = {
  DRAFT: '下書き',
  PUBLISHED: '公開中',
  ARCHIVED: 'アーカイブ',
  DELETED: '削除済み',
} as const
