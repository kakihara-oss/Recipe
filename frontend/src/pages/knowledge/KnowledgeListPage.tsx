import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useCategories, useArticles } from '../../hooks/useKnowledge'
import Pagination from '../../components/common/Pagination'

export default function KnowledgeListPage() {
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined)
  const [page, setPage] = useState(0)
  const { data: categories } = useCategories()
  const { data, isLoading } = useArticles({ categoryId, page, size: 20 })

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">ナレッジベース</h2>
        <div className="flex gap-2">
          <Link to="/knowledge/search" className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            検索
          </Link>
          <Link to="/knowledge/new" className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
            + 記事作成
          </Link>
        </div>
      </div>

      {/* Category tabs */}
      <div className="mb-4 flex flex-wrap gap-1 rounded-lg bg-gray-100 p-1">
        <button
          onClick={() => { setCategoryId(undefined); setPage(0) }}
          className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
            categoryId === undefined ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-600 hover:text-gray-800'
          }`}
        >
          全て
        </button>
        {categories?.map((cat) => (
          <button
            key={cat.id}
            onClick={() => { setCategoryId(cat.id); setPage(0) }}
            className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
              categoryId === cat.id ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-600 hover:text-gray-800'
            }`}
          >
            {cat.name}
          </button>
        ))}
      </div>

      {/* Articles */}
      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : data && data.content.length > 0 ? (
        <>
          <div className="space-y-3">
            {data.content.map((article) => (
              <Link
                key={article.id}
                to={`/knowledge/${article.id}`}
                className="block rounded-lg border border-gray-200 bg-white p-4 transition-shadow hover:shadow-md"
              >
                <h3 className="mb-1 text-base font-semibold text-gray-800">{article.title}</h3>
                <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500">
                  <span className="rounded bg-blue-50 px-2 py-0.5 text-blue-700">{article.categoryName}</span>
                  {article.tags && (
                    <span>{article.tags.split(',').map((t) => t.trim()).join(' / ')}</span>
                  )}
                  <span>投稿者: {article.authorName}</span>
                  <span>{new Date(article.updatedAt).toLocaleDateString('ja-JP')}</span>
                </div>
              </Link>
            ))}
          </div>
          <Pagination currentPage={data.number} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="py-12 text-center text-gray-400">記事がありません</div>
      )}
    </div>
  )
}
