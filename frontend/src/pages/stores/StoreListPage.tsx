import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useStores, useCreateStore } from '../../hooks/useStores'
import { useAuth } from '../../contexts/AuthContext'
import { canManageUsers } from '../../utils/permissions'

export default function StoreListPage() {
  const { user } = useAuth()
  const { data: stores, isLoading } = useStores()
  const createMutation = useCreateStore()
  const [showForm, setShowForm] = useState(false)
  const [storeCode, setStoreCode] = useState('')
  const [name, setName] = useState('')
  const [location, setLocation] = useState('')

  const isProducer = user?.role ? canManageUsers(user.role) : false

  const handleCreate = () => {
    if (!storeCode.trim() || !name.trim()) return
    createMutation.mutate(
      { storeCode: storeCode.trim(), name: name.trim(), location: location.trim() || undefined },
      {
        onSuccess: () => {
          setShowForm(false)
          setStoreCode('')
          setName('')
          setLocation('')
        },
      }
    )
  }

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">店舗マスタ</h2>
        {isProducer && (
          <button
            onClick={() => setShowForm(!showForm)}
            className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            {showForm ? 'キャンセル' : '+ 店舗追加'}
          </button>
        )}
      </div>

      {showForm && (
        <div className="mb-6 rounded-lg border border-gray-200 bg-white p-4">
          <h3 className="mb-3 text-lg font-semibold text-gray-700">新規店舗登録</h3>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <div>
              <label className="mb-1 block text-sm text-gray-600">店舗コード *</label>
              <input
                type="text"
                value={storeCode}
                onChange={(e) => setStoreCode(e.target.value)}
                placeholder="STORE001"
                className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">店舗名 *</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="本店"
                className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm text-gray-600">所在地</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="東京都..."
                className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </div>
          </div>
          <div className="mt-3 flex justify-end">
            <button
              onClick={handleCreate}
              disabled={createMutation.isPending || !storeCode.trim() || !name.trim()}
              className="rounded bg-green-600 px-4 py-2 text-sm text-white hover:bg-green-700 disabled:opacity-50"
            >
              {createMutation.isPending ? '登録中...' : '登録'}
            </button>
          </div>
        </div>
      )}

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-gray-600">店舗コード</th>
              <th className="px-4 py-3 text-left text-gray-600">店舗名</th>
              <th className="px-4 py-3 text-left text-gray-600">所在地</th>
              <th className="px-4 py-3 text-left text-gray-600">操作</th>
            </tr>
          </thead>
          <tbody>
            {stores && stores.length > 0 ? (
              stores.map((store) => (
                <tr key={store.id} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-sm">{store.storeCode}</td>
                  <td className="px-4 py-3 font-medium">{store.name}</td>
                  <td className="px-4 py-3 text-gray-500">{store.location ?? '-'}</td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/stores/${store.id}`}
                      className="text-blue-600 hover:underline"
                    >
                      詳細
                    </Link>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
                  店舗が登録されていません
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
