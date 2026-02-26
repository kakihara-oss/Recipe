import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useFeedbacks, useDeleteFeedback } from '../../hooks/useFeedbacks'
import { useAuth } from '../../contexts/AuthContext'
import { canCreateFeedback } from '../../utils/permissions'
import Pagination from '../../components/common/Pagination'
import { COLLECTION_METHOD_LABELS } from '../../constants'

export default function FeedbackListPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const { data, isLoading } = useFeedbacks({ page, size: 20 })
  const deleteMutation = useDeleteFeedback()

  const handleDelete = (id: number) => {
    if (confirm('このフィードバックを削除しますか？')) {
      deleteMutation.mutate(id)
    }
  }

  const renderStars = (score: number | null) => {
    if (score === null || score === undefined) return '-'
    return '★'.repeat(score) + '☆'.repeat(5 - score)
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">フィードバック一覧</h2>
        <div className="flex gap-2">
          <Link to="/feedbacks/summaries" className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            サマリー
          </Link>
          <Link to="/feedbacks/trend" className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            トレンド
          </Link>
          {user && canCreateFeedback(user.role) && (
            <Link to="/feedbacks/new" className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
              + 新規登録
            </Link>
          )}
        </div>
      </div>

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : data && data.content.length > 0 ? (
        <>
          <div className="overflow-x-auto rounded-lg border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-gray-600">レシピ</th>
                  <th className="px-4 py-3 text-left text-gray-600">店舗</th>
                  <th className="px-4 py-3 text-left text-gray-600">期間</th>
                  <th className="px-4 py-3 text-left text-gray-600">収集方法</th>
                  <th className="px-4 py-3 text-center text-gray-600">満足度</th>
                  <th className="px-4 py-3 text-center text-gray-600">感動度</th>
                  <th className="px-4 py-3 text-right text-gray-600">操作</th>
                </tr>
              </thead>
              <tbody className="bg-white">
                {data.content.map((fb) => (
                  <tr key={fb.id} className="border-t border-gray-100">
                    <td className="px-4 py-3 font-medium text-gray-800">{fb.recipeTitle}</td>
                    <td className="px-4 py-3 text-gray-600">{fb.storeName ?? '-'}</td>
                    <td className="px-4 py-3 text-gray-600">{fb.periodStart} ~ {fb.periodEnd}</td>
                    <td className="px-4 py-3 text-gray-600">{COLLECTION_METHOD_LABELS[fb.collectionMethod]}</td>
                    <td className="px-4 py-3 text-center text-yellow-500">{renderStars(fb.satisfactionScore)}</td>
                    <td className="px-4 py-3 text-center text-yellow-500">{renderStars(fb.emotionScore)}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => handleDelete(fb.id)}
                        className="text-xs text-red-500 hover:underline"
                      >
                        削除
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination currentPage={data.number} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="py-12 text-center text-gray-400">フィードバックがありません</div>
      )}
    </div>
  )
}
