import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCreateIngredient } from '../../hooks/useIngredients'
import type { CreateIngredientRequest, SupplyStatus } from '../../types'

export default function IngredientCreatePage() {
  const navigate = useNavigate()
  const createMutation = useCreateIngredient()

  const [name, setName] = useState('')
  const [category, setCategory] = useState('')
  const [standardUnit, setStandardUnit] = useState('')
  const [seasonalFlag, setSeasonalFlag] = useState(false)
  const [supplyStatus, setSupplyStatus] = useState<SupplyStatus>('AVAILABLE')
  const [supplier, setSupplier] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: CreateIngredientRequest = {
      name,
      category: category || undefined,
      standardUnit: standardUnit || undefined,
      seasonalFlag,
      supplyStatus,
      supplier: supplier || undefined,
    }
    createMutation.mutate(req, {
      onSuccess: (ingredient) => navigate(`/ingredients/${ingredient.id}`),
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="mb-6 text-2xl font-bold text-gray-800">食材登録</h2>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">食材名 *</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            maxLength={255}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">カテゴリ</label>
            <input
              type="text"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              maxLength={100}
              placeholder="例: 野菜、肉、魚介"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">標準単位</label>
            <input
              type="text"
              value={standardUnit}
              onChange={(e) => setStandardUnit(e.target.value)}
              maxLength={50}
              placeholder="例: g, 個, 本"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">供給状態</label>
            <select
              value={supplyStatus}
              onChange={(e) => setSupplyStatus(e.target.value as SupplyStatus)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value="AVAILABLE">供給可能</option>
              <option value="LIMITED">供給不安定</option>
              <option value="UNAVAILABLE">供給停止</option>
              <option value="SEASONAL">季節限定</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">仕入先</label>
            <input
              type="text"
              value={supplier}
              onChange={(e) => setSupplier(e.target.value)}
              maxLength={200}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="seasonalFlag"
            checked={seasonalFlag}
            onChange={(e) => setSeasonalFlag(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300"
          />
          <label htmlFor="seasonalFlag" className="text-sm text-gray-700">季節食材</label>
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => navigate('/ingredients')}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || !name.trim()}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {createMutation.isPending ? '登録中...' : '登録'}
          </button>
        </div>
      </form>
    </div>
  )
}
