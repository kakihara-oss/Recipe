import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useRecipes } from '../../hooks/useRecipes'
import { useAuth } from '../../contexts/AuthContext'
import { canCreateRecipe } from '../../utils/permissions'
import RecipeCard from '../../components/recipe/RecipeCard'
import Pagination from '../../components/common/Pagination'
import type { RecipeStatus } from '../../types'

const statusTabs: { label: string; value: RecipeStatus | '' }[] = [
  { label: '全て', value: '' },
  { label: '下書き', value: 'DRAFT' },
  { label: '公開中', value: 'PUBLISHED' },
  { label: 'アーカイブ', value: 'ARCHIVED' },
]

export default function RecipeListPage() {
  const { user } = useAuth()
  const [status, setStatus] = useState<RecipeStatus | ''>('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading } = useRecipes({
    status: status || undefined,
    category: category || undefined,
    page,
    size: 20,
  })

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-800">レシピ一覧</h2>
        {user && canCreateRecipe(user.role) && (
          <Link
            to="/recipes/new"
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            + 新規作成
          </Link>
        )}
      </div>

      {/* Filters */}
      <div className="mb-4 flex flex-wrap items-center gap-4">
        <div className="flex gap-1 rounded-lg bg-gray-100 p-1">
          {statusTabs.map((tab) => (
            <button
              key={tab.value}
              onClick={() => { setStatus(tab.value); setPage(0) }}
              className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                status === tab.value
                  ? 'bg-white text-gray-800 shadow-sm'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
        <input
          type="text"
          placeholder="カテゴリで絞り込み"
          value={category}
          onChange={(e) => { setCategory(e.target.value); setPage(0) }}
          className="rounded-lg border border-gray-300 px-3 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
        />
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="py-12 text-center text-gray-500">読み込み中...</div>
      ) : data && data.content.length > 0 ? (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((recipe) => (
              <RecipeCard key={recipe.id} recipe={recipe} />
            ))}
          </div>
          <Pagination currentPage={data.number} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="py-12 text-center text-gray-400">レシピがありません</div>
      )}
    </div>
  )
}
