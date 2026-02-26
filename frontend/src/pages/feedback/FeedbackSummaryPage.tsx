import { useState } from 'react'
import { useRecipes } from '../../hooks/useRecipes'
import { useGenerateSummary, useSummaries } from '../../hooks/useFeedbacks'
import Pagination from '../../components/common/Pagination'

export default function FeedbackSummaryPage() {
  const { data: recipesData } = useRecipes({ size: 100, status: 'PUBLISHED' })
  const generateMutation = useGenerateSummary()

  const [recipeId, setRecipeId] = useState('')
  const [periodStart, setPeriodStart] = useState('')
  const [periodEnd, setPeriodEnd] = useState('')
  const [page, setPage] = useState(0)

  const numRecipeId = Number(recipeId) || 0
  const { data: summaries } = useSummaries(numRecipeId, { page, size: 10 })

  const handleGenerate = (e: React.FormEvent) => {
    e.preventDefault()
    generateMutation.mutate({
      recipeId: Number(recipeId),
      periodStart,
      periodEnd,
    })
  }

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">フィードバックサマリー</h2>

      {/* Generate form */}
      <form onSubmit={handleGenerate} className="mb-8 rounded-lg border border-gray-200 bg-white p-4">
        <div className="flex flex-wrap items-end gap-4">
          <div className="flex-1">
            <label className="mb-1 block text-sm font-medium text-gray-700">レシピ *</label>
            <select
              value={recipeId}
              onChange={(e) => { setRecipeId(e.target.value); setPage(0) }}
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value="">レシピを選択</option>
              {recipesData?.content.map((r) => (
                <option key={r.id} value={r.id}>{r.title}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">開始日 *</label>
            <input
              type="date"
              value={periodStart}
              onChange={(e) => setPeriodStart(e.target.value)}
              required
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">終了日 *</label>
            <input
              type="date"
              value={periodEnd}
              onChange={(e) => setPeriodEnd(e.target.value)}
              required
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <button
            type="submit"
            disabled={generateMutation.isPending || !recipeId || !periodStart || !periodEnd}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {generateMutation.isPending ? '集計中...' : '集計する'}
          </button>
        </div>
      </form>

      {/* Latest generation result */}
      {generateMutation.data && (
        <div className="mb-8 rounded-lg border border-green-200 bg-green-50 p-4">
          <h3 className="mb-2 text-sm font-semibold text-green-800">集計結果</h3>
          <div className="grid grid-cols-3 gap-4 text-sm">
            <div>
              <span className="text-gray-600">平均満足度:</span>
              <span className="ml-1 font-bold text-gray-800">{generateMutation.data.avgSatisfaction}</span>
            </div>
            <div>
              <span className="text-gray-600">平均感動度:</span>
              <span className="ml-1 font-bold text-gray-800">{generateMutation.data.avgEmotion ?? '-'}</span>
            </div>
            <div>
              <span className="text-gray-600">件数:</span>
              <span className="ml-1 font-bold text-gray-800">{generateMutation.data.feedbackCount}件</span>
            </div>
          </div>
          {generateMutation.data.mainCommentTrend && (
            <p className="mt-2 text-sm text-gray-700">{generateMutation.data.mainCommentTrend}</p>
          )}
        </div>
      )}

      {/* Past summaries */}
      {numRecipeId > 0 && summaries && summaries.content.length > 0 && (
        <div>
          <h3 className="mb-3 text-lg font-semibold text-gray-700">過去のサマリー一覧</h3>
          <div className="space-y-3">
            {summaries.content.map((s) => (
              <div key={s.id} className="rounded-lg border border-gray-200 bg-white p-4">
                <div className="mb-2 flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-800">
                    {s.periodStart} ~ {s.periodEnd}
                  </span>
                  <span className="text-xs text-gray-400">
                    {new Date(s.createdAt).toLocaleDateString('ja-JP')}
                  </span>
                </div>
                <div className="flex gap-6 text-sm text-gray-600">
                  <span>満足度: <strong>{s.avgSatisfaction}</strong></span>
                  <span>感動度: <strong>{s.avgEmotion ?? '-'}</strong></span>
                  <span>件数: <strong>{s.feedbackCount}</strong></span>
                </div>
                {s.mainCommentTrend && (
                  <p className="mt-1 text-xs text-gray-500">{s.mainCommentTrend}</p>
                )}
              </div>
            ))}
          </div>
          <Pagination currentPage={summaries.number} totalPages={summaries.totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  )
}
