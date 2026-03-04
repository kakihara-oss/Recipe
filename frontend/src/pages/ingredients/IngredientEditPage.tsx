import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useIngredient, useUpdateIngredient, useAddPrice, useUpdateSeasons } from '../../hooks/useIngredients'
import ImageUpload from '../../components/common/ImageUpload'
import type { UpdateIngredientRequest, SupplyStatus, CreateIngredientPriceRequest, UpdateIngredientSeasonRequest } from '../../types'

export default function IngredientEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const ingredientId = Number(id)
  const { data: ingredient, isLoading } = useIngredient(ingredientId)
  const updateMutation = useUpdateIngredient(ingredientId)
  const addPriceMutation = useAddPrice(ingredientId)
  const updateSeasonsMutation = useUpdateSeasons(ingredientId)

  const [name, setName] = useState('')
  const [category, setCategory] = useState('')
  const [standardUnit, setStandardUnit] = useState('')
  const [seasonalFlag, setSeasonalFlag] = useState(false)
  const [supplyStatus, setSupplyStatus] = useState<SupplyStatus>('AVAILABLE')
  const [supplier, setSupplier] = useState('')

  // Price form
  const [priceUnitPrice, setPriceUnitPrice] = useState('')
  const [pricePricePerUnit, setPricePricePerUnit] = useState('')
  const [priceEffectiveFrom, setPriceEffectiveFrom] = useState('')

  // Season form
  const [seasons, setSeasons] = useState<{ month: number; availabilityRank: string; qualityNote: string }[]>([])

  useEffect(() => {
    if (ingredient) {
      setName(ingredient.name)
      setCategory(ingredient.category ?? '')
      setStandardUnit(ingredient.standardUnit ?? '')
      setSeasonalFlag(ingredient.seasonalFlag)
      setSupplyStatus(ingredient.supplyStatus)
      setSupplier(ingredient.supplier ?? '')
      if (ingredient.seasons.length > 0) {
        setSeasons(ingredient.seasons.map((s) => ({
          month: s.month,
          availabilityRank: s.availabilityRank,
          qualityNote: s.qualityNote ?? '',
        })))
      }
    }
  }, [ingredient])

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!ingredient) return <div className="py-12 text-center text-gray-400">食材が見つかりません</div>

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: UpdateIngredientRequest = {
      name,
      category: category || undefined,
      standardUnit: standardUnit || undefined,
      seasonalFlag,
      supplyStatus,
      supplier: supplier || undefined,
    }
    updateMutation.mutate(req, {
      onSuccess: () => navigate(`/ingredients/${ingredientId}`),
    })
  }

  const handleAddPrice = (e: React.FormEvent) => {
    e.preventDefault()
    if (!priceUnitPrice || !priceEffectiveFrom) return
    const req: CreateIngredientPriceRequest = {
      unitPrice: Number(priceUnitPrice),
      pricePerUnit: pricePricePerUnit || undefined,
      effectiveFrom: priceEffectiveFrom,
    }
    addPriceMutation.mutate(req, {
      onSuccess: () => {
        setPriceUnitPrice('')
        setPricePricePerUnit('')
        setPriceEffectiveFrom('')
      },
    })
  }

  const addSeason = () => {
    setSeasons([...seasons, { month: 1, availabilityRank: 'HIGH', qualityNote: '' }])
  }

  const removeSeason = (index: number) => {
    setSeasons(seasons.filter((_, i) => i !== index))
  }

  const handleSaveSeasons = () => {
    const reqs: UpdateIngredientSeasonRequest[] = seasons.map((s) => ({
      month: s.month,
      availabilityRank: s.availabilityRank,
      qualityNote: s.qualityNote || undefined,
    }))
    updateSeasonsMutation.mutate(reqs)
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to={`/ingredients/${ingredientId}`} className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 詳細に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">食材編集</h2>

      {/* Photo */}
      <div className="mb-6">
        <ImageUpload
          target="ingredient"
          targetId={ingredientId}
          currentImageUrl={ingredient.imageUrl}
          label="食材写真"
        />
      </div>

      {/* Basic info form */}
      <form onSubmit={handleSubmit} className="mb-8 space-y-4">
        <h3 className="text-lg font-semibold text-gray-700">基本情報</h3>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">食材名 *</label>
          <input type="text" value={name} onChange={(e) => setName(e.target.value)} maxLength={255} required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">カテゴリ</label>
            <input type="text" value={category} onChange={(e) => setCategory(e.target.value)} maxLength={100}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">標準単位</label>
            <input type="text" value={standardUnit} onChange={(e) => setStandardUnit(e.target.value)} maxLength={50}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">供給状態</label>
            <select value={supplyStatus} onChange={(e) => setSupplyStatus(e.target.value as SupplyStatus)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none">
              <option value="AVAILABLE">供給可能</option>
              <option value="LIMITED">供給不安定</option>
              <option value="UNAVAILABLE">供給停止</option>
              <option value="SEASONAL">季節限定</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">仕入先</label>
            <input type="text" value={supplier} onChange={(e) => setSupplier(e.target.value)} maxLength={200}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <input type="checkbox" id="seasonalFlag" checked={seasonalFlag} onChange={(e) => setSeasonalFlag(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300" />
          <label htmlFor="seasonalFlag" className="text-sm text-gray-700">季節食材</label>
        </div>
        <div className="flex justify-end">
          <button type="submit" disabled={updateMutation.isPending || !name.trim()}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {updateMutation.isPending ? '保存中...' : '基本情報を保存'}
          </button>
        </div>
      </form>

      {/* Price add form */}
      <div className="mb-8 rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">価格追加</h3>
        <form onSubmit={handleAddPrice} className="flex flex-wrap items-end gap-3">
          <div>
            <label className="mb-1 block text-xs text-gray-500">単価 *</label>
            <input type="number" value={priceUnitPrice} onChange={(e) => setPriceUnitPrice(e.target.value)}
              step="0.01" min="0" placeholder="150.00"
              className="w-28 rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="mb-1 block text-xs text-gray-500">単位</label>
            <input type="text" value={pricePricePerUnit} onChange={(e) => setPricePricePerUnit(e.target.value)}
              placeholder="個" maxLength={50}
              className="w-20 rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
          <div>
            <label className="mb-1 block text-xs text-gray-500">有効開始日 *</label>
            <input type="date" value={priceEffectiveFrom} onChange={(e) => setPriceEffectiveFrom(e.target.value)}
              className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none" />
          </div>
          <button type="submit" disabled={addPriceMutation.isPending || !priceUnitPrice || !priceEffectiveFrom}
            className="rounded bg-green-600 px-3 py-1.5 text-sm text-white hover:bg-green-700 disabled:opacity-50">
            追加
          </button>
        </form>
      </div>

      {/* Seasons form */}
      <div className="rounded-lg border border-gray-200 p-4">
        <h3 className="mb-3 text-lg font-semibold text-gray-700">旬情報</h3>
        {seasons.map((season, index) => (
          <div key={index} className="mb-2 flex items-center gap-2">
            <select value={season.month} onChange={(e) => {
              setSeasons(seasons.map((s, i) => i === index ? { month: Number(e.target.value), availabilityRank: s.availabilityRank, qualityNote: s.qualityNote } : s))
            }} className="rounded border border-gray-300 px-2 py-1.5 text-sm">
              {Array.from({ length: 12 }, (_, i) => (
                <option key={i + 1} value={i + 1}>{i + 1}月</option>
              ))}
            </select>
            <select value={season.availabilityRank} onChange={(e) => {
              setSeasons(seasons.map((s, i) => i === index ? { month: s.month, availabilityRank: e.target.value, qualityNote: s.qualityNote } : s))
            }} className="rounded border border-gray-300 px-2 py-1.5 text-sm">
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
            <input type="text" value={season.qualityNote} onChange={(e) => {
              setSeasons(seasons.map((s, i) => i === index ? { month: s.month, availabilityRank: s.availabilityRank, qualityNote: e.target.value } : s))
            }} placeholder="品質メモ" className="flex-1 rounded border border-gray-300 px-2 py-1.5 text-sm" />
            <button type="button" onClick={() => removeSeason(index)} className="text-xs text-red-500 hover:underline">
              削除
            </button>
          </div>
        ))}
        <div className="mt-2 flex items-center gap-3">
          <button type="button" onClick={addSeason} className="text-sm text-blue-600 hover:underline">
            + 月を追加
          </button>
          {seasons.length > 0 && (
            <button type="button" onClick={handleSaveSeasons}
              disabled={updateSeasonsMutation.isPending}
              className="rounded bg-green-600 px-3 py-1 text-sm text-white hover:bg-green-700 disabled:opacity-50">
              旬情報を保存
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
