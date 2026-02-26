import { useParams, Link } from 'react-router-dom'
import { useRecipe, useRecipeHistory } from '../../hooks/useRecipes'

const changeTypeLabels: Record<string, string> = {
  CREATE: '作成',
  UPDATE: '更新',
  STATUS_CHANGE: 'ステータス変更',
  DELETE: '削除',
}

export default function RecipeHistoryPage() {
  const { id } = useParams<{ id: string }>()
  const recipeId = Number(id)
  const { data: recipe } = useRecipe(recipeId)
  const { data: history, isLoading } = useRecipeHistory(recipeId)

  return (
    <div className="mx-auto max-w-2xl">
      <Link to={`/recipes/${recipeId}`} className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 詳細に戻る
      </Link>
      <h2 className="mb-1 text-2xl font-bold text-gray-800">変更履歴</h2>
      {recipe && <p className="mb-6 text-sm text-gray-500">{recipe.title}</p>}

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : history && history.length > 0 ? (
        <div className="space-y-4">
          {[...history].reverse().map((entry) => (
            <div key={entry.id} className="rounded-lg border border-gray-200 bg-white p-4">
              <div className="mb-1 flex items-center justify-between">
                <span className="text-sm font-medium text-gray-800">
                  {new Date(entry.changedAt).toLocaleString('ja-JP')}
                </span>
                <span className="text-sm text-gray-500">{entry.changedByName}</span>
              </div>
              <p className="text-sm text-gray-600">
                <span className="mr-2 inline-block rounded bg-gray-100 px-2 py-0.5 text-xs font-medium">
                  {changeTypeLabels[entry.changeType] ?? entry.changeType}
                </span>
                {entry.changedFields && (
                  <span className="text-gray-400">{entry.changedFields}</span>
                )}
              </p>
            </div>
          ))}
        </div>
      ) : (
        <div className="py-12 text-center text-gray-400">変更履歴がありません</div>
      )}
    </div>
  )
}
