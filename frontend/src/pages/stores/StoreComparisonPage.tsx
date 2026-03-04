import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useStoreComparison } from '../../hooks/useSales'

export default function StoreComparisonPage() {
  const currentMonth = new Date().toISOString().slice(0, 7)
  const [targetMonth, setTargetMonth] = useState(currentMonth)
  const { data: comparisonData, isLoading } = useStoreComparison(targetMonth)

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">店舗間比較</h2>

      <div className="mb-6 flex items-center gap-3">
        <label className="text-sm text-gray-600">対象月:</label>
        <input
          type="month"
          value={targetMonth}
          onChange={(e) => setTargetMonth(e.target.value)}
          className="rounded border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-gray-600">店舗名</th>
                <th className="px-4 py-3 text-right text-gray-600">理論原価</th>
                <th className="px-4 py-3 text-right text-gray-600">売上合計</th>
                <th className="px-4 py-3 text-right text-gray-600">理論原価率</th>
              </tr>
            </thead>
            <tbody>
              {comparisonData && comparisonData.length > 0 ? (
                comparisonData.map((fc) => (
                  <tr key={fc.id} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <Link to={`/stores/${fc.storeId}`} className="text-blue-600 hover:underline">
                        {fc.storeName}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-right">¥{fc.theoreticalFoodCost.toLocaleString()}</td>
                    <td className="px-4 py-3 text-right">¥{fc.totalSales.toLocaleString()}</td>
                    <td className="px-4 py-3 text-right">
                      <span className={`font-medium ${fc.theoreticalFoodCostRate > 35 ? 'text-red-600' : fc.theoreticalFoodCostRate > 30 ? 'text-yellow-600' : 'text-green-600'}`}>
                        {fc.theoreticalFoodCostRate}%
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
                    {targetMonth} のデータがありません
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
