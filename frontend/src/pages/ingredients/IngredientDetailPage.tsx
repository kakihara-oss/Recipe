import { useParams, Link } from 'react-router-dom'
import { useIngredient, useAffectedRecipes } from '../../hooks/useIngredients'
import { useAuth } from '../../contexts/AuthContext'
import ImageUpload from '../../components/common/ImageUpload'
import type { SupplyStatus } from '../../types'

const SUPPLY_STATUS_LABELS: Record<SupplyStatus, string> = {
  AVAILABLE: '供給可能',
  LIMITED: '供給不安定',
  UNAVAILABLE: '供給停止',
  SEASONAL: '季節限定',
}

const MONTH_NAMES = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

export default function IngredientDetailPage() {
  const { id } = useParams<{ id: string }>()
  const ingredientId = Number(id)
  const { data: ingredient, isLoading } = useIngredient(ingredientId)
  const { data: affectedRecipes } = useAffectedRecipes(ingredientId)
  const { user } = useAuth()

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!ingredient) return <div className="py-12 text-center text-gray-400">食材が見つかりません</div>

  const canEdit = user?.role === 'PURCHASER' || user?.role === 'PRODUCER'

  return (
    <div className="mx-auto max-w-3xl">
      <Link to="/ingredients" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 食材一覧に戻る
      </Link>

      <div className="mb-6 flex items-start justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">{ingredient.name}</h2>
          <div className="mt-1 flex items-center gap-3 text-sm text-gray-500">
            {ingredient.category && <span>カテゴリ: {ingredient.category}</span>}
            {ingredient.standardUnit && <span>単位: {ingredient.standardUnit}</span>}
            <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
              ingredient.supplyStatus === 'AVAILABLE' ? 'bg-green-100 text-green-800' :
              ingredient.supplyStatus === 'LIMITED' ? 'bg-yellow-100 text-yellow-800' :
              ingredient.supplyStatus === 'UNAVAILABLE' ? 'bg-red-100 text-red-800' :
              'bg-blue-100 text-blue-800'
            }`}>
              {SUPPLY_STATUS_LABELS[ingredient.supplyStatus]}
            </span>
          </div>
        </div>
        {canEdit && (
          <Link
            to={`/ingredients/${ingredientId}/edit`}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            編集
          </Link>
        )}
      </div>

      {/* Photo */}
      {canEdit && (
        <div className="mb-6">
          <ImageUpload
            target="ingredient"
            targetId={ingredientId}
            currentImageUrl={ingredient.imageUrl}
            label="食材写真"
          />
        </div>
      )}
      {!canEdit && ingredient.imageUrl && (
        <div className="mb-6">
          <img src={ingredient.imageUrl} alt={ingredient.name} className="h-48 w-48 rounded-lg object-cover" />
        </div>
      )}

      {/* Basic info */}
      <div className="mb-6 rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">基本情報</h3>
        <dl className="grid grid-cols-2 gap-3 text-sm">
          <div>
            <dt className="text-gray-500">仕入先</dt>
            <dd className="text-gray-800">{ingredient.supplier || '-'}</dd>
          </div>
          <div>
            <dt className="text-gray-500">季節食材</dt>
            <dd className="text-gray-800">{ingredient.seasonalFlag ? 'はい' : 'いいえ'}</dd>
          </div>
          <div>
            <dt className="text-gray-500">現在単価</dt>
            <dd className="text-gray-800">
              {ingredient.currentPrice
                ? `¥${ingredient.currentPrice.unitPrice.toLocaleString()}${ingredient.currentPrice.pricePerUnit ? ` / ${ingredient.currentPrice.pricePerUnit}` : ''}`
                : '未設定'}
            </dd>
          </div>
        </dl>
      </div>

      {/* Price history */}
      <div className="mb-6 rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">価格履歴</h3>
        {ingredient.prices.length === 0 ? (
          <p className="text-sm text-gray-400">価格履歴がありません</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b">
                <th className="py-2 text-left text-gray-600">単価</th>
                <th className="py-2 text-left text-gray-600">単位</th>
                <th className="py-2 text-left text-gray-600">有効開始</th>
                <th className="py-2 text-left text-gray-600">有効終了</th>
              </tr>
            </thead>
            <tbody>
              {ingredient.prices.map((price) => (
                <tr key={price.id} className="border-b last:border-0">
                  <td className="py-2">¥{price.unitPrice.toLocaleString()}</td>
                  <td className="py-2 text-gray-600">{price.pricePerUnit || '-'}</td>
                  <td className="py-2 text-gray-600">{price.effectiveFrom}</td>
                  <td className="py-2 text-gray-600">{price.effectiveTo || '現在'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Seasons */}
      <div className="mb-6 rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">旬情報</h3>
        {ingredient.seasons.length === 0 ? (
          <p className="text-sm text-gray-400">旬情報が登録されていません</p>
        ) : (
          <div className="grid grid-cols-4 gap-2">
            {ingredient.seasons.map((season) => (
              <div key={season.id} className="rounded border p-2 text-center text-sm">
                <div className="font-medium">{MONTH_NAMES[season.month - 1]}</div>
                <div className="text-xs text-gray-500">{season.availabilityRank}</div>
                {season.qualityNote && <div className="mt-1 text-xs text-gray-400">{season.qualityNote}</div>}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Affected recipes */}
      <div className="rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">使用レシピ</h3>
        {!affectedRecipes || affectedRecipes.length === 0 ? (
          <p className="text-sm text-gray-400">この食材を使用するレシピはありません</p>
        ) : (
          <ul className="space-y-1">
            {affectedRecipes.map((r) => (
              <li key={r.recipeId}>
                <Link to={`/recipes/${r.recipeId}`} className="text-sm text-blue-600 hover:underline">
                  {r.recipeTitle}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
