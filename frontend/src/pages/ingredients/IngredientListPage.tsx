import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useIngredients } from '../../hooks/useIngredients'
import { useAuth } from '../../contexts/AuthContext'
import type { SupplyStatus } from '../../types'

const SUPPLY_STATUS_LABELS: Record<SupplyStatus, string> = {
  AVAILABLE: '供給可能',
  LIMITED: '供給不安定',
  UNAVAILABLE: '供給停止',
  SEASONAL: '季節限定',
}

const SUPPLY_STATUS_COLORS: Record<SupplyStatus, string> = {
  AVAILABLE: 'bg-green-100 text-green-800',
  LIMITED: 'bg-yellow-100 text-yellow-800',
  UNAVAILABLE: 'bg-red-100 text-red-800',
  SEASONAL: 'bg-blue-100 text-blue-800',
}

export default function IngredientListPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [supplyStatusFilter, setSupplyStatusFilter] = useState<SupplyStatus | ''>('')

  const { data, isLoading } = useIngredients({
    keyword: keyword || undefined,
    supplyStatus: supplyStatusFilter || undefined,
    page,
    size: 20,
  })

  const canCreate = user?.role === 'PURCHASER' || user?.role === 'PRODUCER'

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">食材マスタ</h2>
        {canCreate && (
          <Link
            to="/ingredients/new"
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            + 食材登録
          </Link>
        )}
      </div>

      {/* Filters */}
      <div className="mb-4 flex flex-wrap gap-3">
        <input
          type="text"
          placeholder="食材名で検索..."
          value={keyword}
          onChange={(e) => { setKeyword(e.target.value); setPage(0) }}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
        <select
          value={supplyStatusFilter}
          onChange={(e) => { setSupplyStatusFilter(e.target.value as SupplyStatus | ''); setPage(0) }}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
        >
          <option value="">全ての供給状態</option>
          <option value="AVAILABLE">供給可能</option>
          <option value="LIMITED">供給不安定</option>
          <option value="UNAVAILABLE">供給停止</option>
          <option value="SEASONAL">季節限定</option>
        </select>
      </div>

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : !data || data.content.length === 0 ? (
        <div className="py-12 text-center text-gray-400">食材が登録されていません</div>
      ) : (
        <>
          <div className="overflow-x-auto rounded-lg border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">食材名</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">カテゴリ</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">標準単位</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">供給状態</th>
                  <th className="px-4 py-3 text-right font-medium text-gray-600">現在単価</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {data.content.map((ingredient) => (
                  <tr key={ingredient.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <Link to={`/ingredients/${ingredient.id}`} className="flex items-center gap-2 text-blue-600 hover:underline">
                        {ingredient.imageUrl && (
                          <img src={ingredient.imageUrl} alt="" className="h-8 w-8 rounded object-cover" />
                        )}
                        {ingredient.name}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-gray-600">{ingredient.category || '-'}</td>
                    <td className="px-4 py-3 text-gray-600">{ingredient.standardUnit || '-'}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${SUPPLY_STATUS_COLORS[ingredient.supplyStatus]}`}>
                        {SUPPLY_STATUS_LABELS[ingredient.supplyStatus]}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right text-gray-700">
                      {ingredient.currentUnitPrice != null ? `¥${ingredient.currentUnitPrice.toLocaleString()}` : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {data.totalPages > 1 && (
            <div className="mt-4 flex items-center justify-center gap-2">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={data.first}
                className="rounded border px-3 py-1 text-sm disabled:opacity-50"
              >
                前へ
              </button>
              <span className="text-sm text-gray-600">
                {data.number + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage(page + 1)}
                disabled={data.last}
                className="rounded border px-3 py-1 text-sm disabled:opacity-50"
              >
                次へ
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
