import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useStore, useUpdateStore } from '../../hooks/useStores'
import { useMonthlySales, useCalculateFoodCost, useStoreTrend } from '../../hooks/useSales'
import { useAuth } from '../../contexts/AuthContext'
import { canManageUsers } from '../../utils/permissions'

export default function StoreDetailPage() {
  const { id } = useParams<{ id: string }>()
  const storeId = Number(id)
  const { user } = useAuth()
  const { data: store, isLoading } = useStore(storeId)
  const updateMutation = useUpdateStore(storeId)
  const calculateCostMutation = useCalculateFoodCost()

  const isProducer = user?.role ? canManageUsers(user.role) : false

  // Monthly sales query
  const currentMonth = new Date().toISOString().slice(0, 7) // YYYY-MM
  const [selectedMonth, setSelectedMonth] = useState(currentMonth)
  const { data: salesData } = useMonthlySales(storeId, selectedMonth)

  // Trend query
  const sixMonthsAgo = (() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 6)
    return d.toISOString().slice(0, 7)
  })()
  const { data: trendData } = useStoreTrend(storeId, sixMonthsAgo, currentMonth)

  // Edit state
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState('')
  const [editLocation, setEditLocation] = useState('')

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!store) return <div className="py-12 text-center text-gray-400">店舗が見つかりません</div>

  const handleUpdate = () => {
    updateMutation.mutate(
      { name: editName || undefined, location: editLocation || undefined },
      {
        onSuccess: () => setEditing(false),
      }
    )
  }

  return (
    <div>
      <Link to="/stores" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 店舗一覧に戻る
      </Link>

      {/* Store info */}
      <div className="mb-6 rounded-lg border border-gray-200 bg-white p-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-800">{store.name}</h2>
            <p className="text-sm text-gray-500">コード: {store.storeCode} | 所在地: {store.location ?? '未設定'}</p>
          </div>
          {isProducer && !editing && (
            <button
              onClick={() => { setEditing(true); setEditName(store.name); setEditLocation(store.location ?? '') }}
              className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50"
            >
              編集
            </button>
          )}
        </div>

        {editing && (
          <div className="border-t pt-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="mb-1 block text-sm text-gray-600">店舗名</label>
                <input type="text" value={editName} onChange={(e) => setEditName(e.target.value)}
                  className="w-full rounded border border-gray-300 px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="mb-1 block text-sm text-gray-600">所在地</label>
                <input type="text" value={editLocation} onChange={(e) => setEditLocation(e.target.value)}
                  className="w-full rounded border border-gray-300 px-3 py-2 text-sm" />
              </div>
            </div>
            <div className="mt-3 flex gap-2">
              <button onClick={handleUpdate} disabled={updateMutation.isPending}
                className="rounded bg-green-600 px-3 py-1.5 text-sm text-white hover:bg-green-700 disabled:opacity-50">
                保存
              </button>
              <button onClick={() => setEditing(false)}
                className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50">
                キャンセル
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Monthly sales */}
      <div className="mb-6 rounded-lg border border-gray-200 bg-white p-6">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-700">月次売上データ</h3>
          <div className="flex items-center gap-2">
            <input type="month" value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value)}
              className="rounded border border-gray-300 px-2 py-1.5 text-sm" />
            <button
              onClick={() => calculateCostMutation.mutate({ storeId, targetMonth: selectedMonth })}
              disabled={calculateCostMutation.isPending}
              className="rounded bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700 disabled:opacity-50">
              {calculateCostMutation.isPending ? '計算中...' : '理論原価計算'}
            </button>
          </div>
        </div>

        {calculateCostMutation.data && (
          <div className="mb-4 rounded-lg bg-blue-50 p-4">
            <h4 className="mb-2 text-sm font-semibold text-blue-800">理論原価計算結果</h4>
            <dl className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <dt className="text-blue-600">理論原価合計</dt>
                <dd className="text-lg font-medium">¥{calculateCostMutation.data.theoreticalFoodCost.toLocaleString()}</dd>
              </div>
              <div>
                <dt className="text-blue-600">売上合計</dt>
                <dd className="text-lg font-medium">¥{calculateCostMutation.data.totalSales.toLocaleString()}</dd>
              </div>
              <div>
                <dt className="text-blue-600">理論原価率</dt>
                <dd className="text-lg font-medium">{calculateCostMutation.data.theoreticalFoodCostRate}%</dd>
              </div>
            </dl>
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-3 py-2 text-left text-gray-600">レシピ</th>
                <th className="px-3 py-2 text-right text-gray-600">出数</th>
                <th className="px-3 py-2 text-right text-gray-600">売上金額</th>
              </tr>
            </thead>
            <tbody>
              {salesData && salesData.length > 0 ? (
                salesData.map((s) => (
                  <tr key={s.id} className="border-t">
                    <td className="px-3 py-2">
                      <Link to={`/recipes/${s.recipeId}`} className="text-blue-600 hover:underline">
                        {s.recipeTitle}
                      </Link>
                    </td>
                    <td className="px-3 py-2 text-right">{s.quantity}</td>
                    <td className="px-3 py-2 text-right">¥{s.salesAmount.toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="px-3 py-6 text-center text-gray-400">
                    売上データがありません
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Trend */}
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <h3 className="mb-4 text-lg font-semibold text-gray-700">理論原価率推移（過去6ヶ月）</h3>
        {trendData && trendData.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left text-gray-600">月</th>
                  <th className="px-3 py-2 text-right text-gray-600">理論原価</th>
                  <th className="px-3 py-2 text-right text-gray-600">売上</th>
                  <th className="px-3 py-2 text-right text-gray-600">原価率</th>
                </tr>
              </thead>
              <tbody>
                {trendData.map((fc) => (
                  <tr key={fc.id} className="border-t">
                    <td className="px-3 py-2">{fc.targetMonth}</td>
                    <td className="px-3 py-2 text-right">¥{fc.theoreticalFoodCost.toLocaleString()}</td>
                    <td className="px-3 py-2 text-right">¥{fc.totalSales.toLocaleString()}</td>
                    <td className="px-3 py-2 text-right font-medium">{fc.theoreticalFoodCostRate}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-400">理論原価データがありません</p>
        )}
      </div>
    </div>
  )
}
