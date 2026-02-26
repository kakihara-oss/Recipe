import { useState } from 'react'
import { useRecipes } from '../../hooks/useRecipes'
import { useTrend } from '../../hooks/useFeedbacks'

export default function FeedbackTrendPage() {
  const { data: recipesData } = useRecipes({ size: 100, status: 'PUBLISHED' })
  const [recipeId, setRecipeId] = useState('')
  const numRecipeId = Number(recipeId) || 0
  const { data: trend, isLoading } = useTrend(numRecipeId)

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">フィードバックトレンド</h2>

      <div className="mb-6">
        <label className="mb-1 block text-sm font-medium text-gray-700">レシピ *</label>
        <select
          value={recipeId}
          onChange={(e) => setRecipeId(e.target.value)}
          className="w-full max-w-md rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        >
          <option value="">レシピを選択</option>
          {recipesData?.content.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : numRecipeId > 0 && trend ? (
        trend.length > 0 ? (
          <div className="rounded-lg border border-gray-200 bg-white p-6">
            {/* Simple table-based trend visualization */}
            <div className="mb-4 flex gap-6 text-sm">
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-blue-500" />
                <span className="text-gray-600">満足度</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-orange-500" />
                <span className="text-gray-600">感動度</span>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-gray-600">期間</th>
                    <th className="px-4 py-2 text-center text-gray-600">満足度</th>
                    <th className="px-4 py-2 text-center text-gray-600">感動度</th>
                    <th className="px-4 py-2 text-center text-gray-600">件数</th>
                    <th className="px-4 py-2 text-left text-gray-600">グラフ</th>
                  </tr>
                </thead>
                <tbody>
                  {trend.map((s) => {
                    const satisfaction = Number(s.avgSatisfaction) || 0
                    const emotion = s.avgEmotion ? Number(s.avgEmotion) : null
                    return (
                      <tr key={s.id} className="border-t border-gray-100">
                        <td className="px-4 py-2 text-gray-800">{s.periodStart}</td>
                        <td className="px-4 py-2 text-center font-medium text-blue-600">{s.avgSatisfaction}</td>
                        <td className="px-4 py-2 text-center font-medium text-orange-600">{s.avgEmotion ?? '-'}</td>
                        <td className="px-4 py-2 text-center text-gray-600">{s.feedbackCount}</td>
                        <td className="px-4 py-2">
                          <div className="flex items-center gap-1">
                            <div className="h-4 rounded bg-blue-400" style={{ width: `${satisfaction * 20}%`, minWidth: '4px' }} />
                            {emotion !== null && (
                              <div className="h-4 rounded bg-orange-400" style={{ width: `${emotion * 20}%`, minWidth: '4px' }} />
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <div className="py-12 text-center text-gray-400">トレンドデータがありません</div>
        )
      ) : (
        <div className="py-12 text-center text-gray-400">レシピを選択してください</div>
      )}
    </div>
  )
}
