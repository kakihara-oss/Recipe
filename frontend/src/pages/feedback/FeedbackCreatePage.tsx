import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCreateFeedback } from '../../hooks/useFeedbacks'
import { useRecipes } from '../../hooks/useRecipes'
import type { CreateProductFeedbackRequest, CollectionMethod } from '../../types'

const collectionMethods: { value: CollectionMethod; label: string }[] = [
  { value: 'SURVEY', label: 'アンケート' },
  { value: 'INTERVIEW', label: 'インタビュー' },
  { value: 'SNS', label: 'SNS' },
  { value: 'DIRECT', label: '直接' },
  { value: 'OTHER', label: 'その他' },
]

export default function FeedbackCreatePage() {
  const navigate = useNavigate()
  const createMutation = useCreateFeedback()
  const { data: recipesData } = useRecipes({ size: 100, status: 'PUBLISHED' })

  const [recipeId, setRecipeId] = useState('')
  const [periodStart, setPeriodStart] = useState('')
  const [periodEnd, setPeriodEnd] = useState('')
  const [collectionMethod, setCollectionMethod] = useState<CollectionMethod>('SURVEY')
  const [satisfactionScore, setSatisfactionScore] = useState(3)
  const [emotionScore, setEmotionScore] = useState<number | null>(null)
  const [comment, setComment] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: CreateProductFeedbackRequest = {
      recipeId: Number(recipeId),
      periodStart,
      periodEnd,
      satisfactionScore,
      emotionScore: emotionScore ?? undefined,
      comment: comment || undefined,
      collectionMethod,
    }
    createMutation.mutate(req, {
      onSuccess: () => navigate('/feedbacks'),
    })
  }

  const StarInput = ({ value, onChange, label }: { value: number; onChange: (v: number) => void; label: string }) => (
    <div>
      <label className="mb-1 block text-sm font-medium text-gray-700">{label}</label>
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => onChange(star)}
            className={`text-2xl ${star <= value ? 'text-yellow-400' : 'text-gray-300'}`}
          >
            ★
          </button>
        ))}
      </div>
    </div>
  )

  return (
    <div className="mx-auto max-w-2xl">
      <Link to="/feedbacks" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 一覧に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">フィードバック登録</h2>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">レシピ *</label>
          <select
            value={recipeId}
            onChange={(e) => setRecipeId(e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="">レシピを選択</option>
            {recipesData?.content.map((r) => (
              <option key={r.id} value={r.id}>{r.title}</option>
            ))}
          </select>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">開始日 *</label>
            <input
              type="date"
              value={periodStart}
              onChange={(e) => setPeriodStart(e.target.value)}
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">終了日 *</label>
            <input
              type="date"
              value={periodEnd}
              onChange={(e) => setPeriodEnd(e.target.value)}
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">収集方法 *</label>
          <select
            value={collectionMethod}
            onChange={(e) => setCollectionMethod(e.target.value as CollectionMethod)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            {collectionMethods.map((m) => (
              <option key={m.value} value={m.value}>{m.label}</option>
            ))}
          </select>
        </div>
        <StarInput value={satisfactionScore} onChange={setSatisfactionScore} label="満足度 * (1-5)" />
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">感動度 (1-5, 任意)</label>
          <div className="flex items-center gap-2">
            <div className="flex gap-1">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setEmotionScore(star)}
                  className={`text-2xl ${emotionScore !== null && star <= emotionScore ? 'text-yellow-400' : 'text-gray-300'}`}
                >
                  ★
                </button>
              ))}
            </div>
            {emotionScore !== null && (
              <button type="button" onClick={() => setEmotionScore(null)} className="text-xs text-gray-500 hover:underline">
                クリア
              </button>
            )}
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">コメント</label>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="flex justify-end gap-3 pt-4">
          <button type="button" onClick={() => navigate('/feedbacks')} className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            キャンセル
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || !recipeId || !periodStart || !periodEnd}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {createMutation.isPending ? '登録中...' : '登録'}
          </button>
        </div>
      </form>
    </div>
  )
}
