import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useRecipe, useUpdateRecipeStatus, useDeleteRecipe } from '../../hooks/useRecipes'
import { useAuth } from '../../contexts/AuthContext'
import {
  canEditRecipe,
  canEditServiceDesign,
  canEditExperienceDesign,
  canChangeRecipeStatus,
  canDeleteRecipe,
} from '../../utils/permissions'
import RecipeStatusBadge from '../../components/recipe/RecipeStatusBadge'
import type { RecipeStatus } from '../../types'

type Tab = 'basic' | 'steps' | 'service' | 'experience'

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const recipeId = Number(id)
  const { data: recipe, isLoading } = useRecipe(recipeId)
  const statusMutation = useUpdateRecipeStatus(recipeId)
  const deleteMutation = useDeleteRecipe()
  const [tab, setTab] = useState<Tab>('basic')

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!recipe) return <div className="py-12 text-center text-gray-400">レシピが見つかりません</div>

  const role = user?.role

  const handleStatusChange = (newStatus: RecipeStatus) => {
    if (confirm(`ステータスを変更しますか？`)) {
      statusMutation.mutate({ status: newStatus })
    }
  }

  const handleDelete = () => {
    if (confirm('このレシピを削除しますか？この操作は取り消せません。')) {
      deleteMutation.mutate(recipeId, {
        onSuccess: () => navigate('/recipes'),
      })
    }
  }

  const tabs: { key: Tab; label: string }[] = [
    { key: 'basic', label: '基本情報' },
    { key: 'steps', label: '調理手順' },
    { key: 'service', label: 'サービス設計' },
    { key: 'experience', label: '体験設計' },
  ]

  return (
    <div>
      <Link to="/recipes" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 一覧に戻る
      </Link>

      {/* Header */}
      <div className="mb-6">
        <div className="mb-2 flex flex-wrap items-center gap-3">
          <h2 className="text-2xl font-bold text-gray-800">{recipe.title}</h2>
          <RecipeStatusBadge status={recipe.status} />
        </div>
        <p className="text-sm text-gray-500">
          {recipe.category && `${recipe.category} | `}
          {recipe.servings && `${recipe.servings}人前 | `}
          作成者: {recipe.createdBy.name} | 更新: {new Date(recipe.updatedAt).toLocaleDateString('ja-JP')}
        </p>
      </div>

      {/* Action buttons */}
      <div className="mb-6 flex flex-wrap gap-2">
        {role && canEditRecipe(role) && (
          <Link to={`/recipes/${recipeId}/edit`} className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50">
            編集
          </Link>
        )}
        {role && canEditServiceDesign(role) && (
          <Link to={`/recipes/${recipeId}/service-design/edit`} className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50">
            サービス設計編集
          </Link>
        )}
        {role && canEditExperienceDesign(role) && (
          <Link to={`/recipes/${recipeId}/experience-design/edit`} className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50">
            体験設計編集
          </Link>
        )}
        {role && canChangeRecipeStatus(role) && recipe.status === 'DRAFT' && (
          <button onClick={() => handleStatusChange('PUBLISHED')} className="rounded bg-green-600 px-3 py-1.5 text-sm text-white hover:bg-green-700">
            公開する
          </button>
        )}
        {role && canChangeRecipeStatus(role) && recipe.status === 'PUBLISHED' && (
          <button onClick={() => handleStatusChange('ARCHIVED')} className="rounded bg-yellow-600 px-3 py-1.5 text-sm text-white hover:bg-yellow-700">
            アーカイブする
          </button>
        )}
        {role && canChangeRecipeStatus(role) && recipe.status === 'ARCHIVED' && (
          <button onClick={() => handleStatusChange('PUBLISHED')} className="rounded bg-green-600 px-3 py-1.5 text-sm text-white hover:bg-green-700">
            再公開する
          </button>
        )}
        {role && canDeleteRecipe(role) && (
          <button onClick={handleDelete} className="rounded bg-red-600 px-3 py-1.5 text-sm text-white hover:bg-red-700">
            削除
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="mb-4 border-b border-gray-200">
        <div className="flex gap-0">
          {tabs.map((t) => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`border-b-2 px-4 py-2 text-sm font-medium transition-colors ${
                tab === t.key
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tab content */}
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        {tab === 'basic' && (
          <div className="space-y-4">
            {recipe.description && (
              <div>
                <h4 className="mb-1 text-sm font-semibold text-gray-600">説明</h4>
                <p className="whitespace-pre-wrap text-gray-800">{recipe.description}</p>
              </div>
            )}
            {recipe.concept && (
              <div>
                <h4 className="mb-1 text-sm font-semibold text-gray-600">コンセプト</h4>
                <p className="whitespace-pre-wrap text-gray-800">{recipe.concept}</p>
              </div>
            )}
            {recipe.story && (
              <div>
                <h4 className="mb-1 text-sm font-semibold text-gray-600">ストーリー</h4>
                <p className="whitespace-pre-wrap text-gray-800">{recipe.story}</p>
              </div>
            )}
            {recipe.ingredients.length > 0 && (
              <div>
                <h4 className="mb-2 text-sm font-semibold text-gray-600">食材</h4>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-3 py-2 text-left text-gray-600">食材名</th>
                        <th className="px-3 py-2 text-left text-gray-600">数量</th>
                        <th className="px-3 py-2 text-left text-gray-600">単位</th>
                        <th className="px-3 py-2 text-left text-gray-600">下処理メモ</th>
                        <th className="px-3 py-2 text-left text-gray-600">代替食材</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recipe.ingredients.map((ing) => (
                        <tr key={ing.id} className="border-t">
                          <td className="px-3 py-2">{ing.ingredientName}</td>
                          <td className="px-3 py-2">{ing.quantity ?? '-'}</td>
                          <td className="px-3 py-2">{ing.unit ?? '-'}</td>
                          <td className="px-3 py-2">{ing.preparationNote ?? '-'}</td>
                          <td className="px-3 py-2">{ing.substitutes ?? '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}

        {tab === 'steps' && (
          <div>
            {recipe.cookingSteps.length > 0 ? (
              <ol className="space-y-4">
                {recipe.cookingSteps
                  .sort((a, b) => a.stepNumber - b.stepNumber)
                  .map((step) => (
                    <li key={step.id} className="flex gap-4">
                      <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-sm font-bold text-blue-700">
                        {step.stepNumber}
                      </div>
                      <div className="flex-1">
                        <p className="text-gray-800">{step.description}</p>
                        <div className="mt-1 flex flex-wrap gap-3 text-xs text-gray-500">
                          {step.durationMinutes && <span>所要時間: {step.durationMinutes}分</span>}
                          {step.temperature && <span>温度: {step.temperature}</span>}
                          {step.tips && <span>コツ: {step.tips}</span>}
                        </div>
                      </div>
                    </li>
                  ))}
              </ol>
            ) : (
              <p className="text-gray-400">調理手順が登録されていません</p>
            )}
          </div>
        )}

        {tab === 'service' && (
          <div className="space-y-4">
            {recipe.serviceDesign ? (
              <>
                {recipe.serviceDesign.platingInstructions && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">盛り付け指示</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.platingInstructions}</p></div>
                )}
                {recipe.serviceDesign.serviceMethod && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">提供方法</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.serviceMethod}</p></div>
                )}
                {recipe.serviceDesign.customerScript && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">声かけスクリプト</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.customerScript}</p></div>
                )}
                {recipe.serviceDesign.stagingMethod && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">演出方法</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.stagingMethod}</p></div>
                )}
                {recipe.serviceDesign.timing && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">タイミング</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.timing}</p></div>
                )}
                {recipe.serviceDesign.storytelling && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">ストーリーテリング</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.serviceDesign.storytelling}</p></div>
                )}
              </>
            ) : (
              <p className="text-gray-400">サービス設計が登録されていません</p>
            )}
          </div>
        )}

        {tab === 'experience' && (
          <div className="space-y-4">
            {recipe.experienceDesign ? (
              <>
                {recipe.experienceDesign.targetScene && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">ターゲットシーン</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.experienceDesign.targetScene}</p></div>
                )}
                {recipe.experienceDesign.emotionalKeyPoints && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">感動ポイント</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.experienceDesign.emotionalKeyPoints}</p></div>
                )}
                {recipe.experienceDesign.specialOccasionSupport && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">記念日対応</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.experienceDesign.specialOccasionSupport}</p></div>
                )}
                {recipe.experienceDesign.seasonalPresentation && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">季節演出</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.experienceDesign.seasonalPresentation}</p></div>
                )}
                {recipe.experienceDesign.sensoryAppeal && (
                  <div><h4 className="mb-1 text-sm font-semibold text-gray-600">五感への訴求</h4><p className="whitespace-pre-wrap text-gray-800">{recipe.experienceDesign.sensoryAppeal}</p></div>
                )}
              </>
            ) : (
              <p className="text-gray-400">体験設計が登録されていません</p>
            )}
          </div>
        )}
      </div>

      <div className="mt-4">
        <Link to={`/recipes/${recipeId}/history`} className="text-sm text-blue-600 hover:underline">
          変更履歴を見る
        </Link>
      </div>
    </div>
  )
}
