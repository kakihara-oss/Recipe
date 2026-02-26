import { useParams, Link, useNavigate } from 'react-router-dom'
import { useArticle, useDeleteArticle } from '../../hooks/useKnowledge'
import { useAuth } from '../../contexts/AuthContext'

export default function KnowledgeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const articleId = Number(id)
  const { data: article, isLoading } = useArticle(articleId)
  const deleteMutation = useDeleteArticle()

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!article) return <div className="py-12 text-center text-gray-400">記事が見つかりません</div>

  const canEdit = user && (user.role === 'PRODUCER' || user.id === article.authorId)

  const handleDelete = () => {
    if (confirm('この記事を削除しますか？')) {
      deleteMutation.mutate(articleId, {
        onSuccess: () => navigate('/knowledge'),
      })
    }
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Link to="/knowledge" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 一覧に戻る
      </Link>

      <div className="mb-4 flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 className="mb-2 text-2xl font-bold text-gray-800">{article.title}</h2>
          <div className="flex flex-wrap items-center gap-3 text-sm text-gray-500">
            <span className="rounded bg-blue-50 px-2 py-0.5 text-xs text-blue-700">{article.categoryName}</span>
            {article.tags && <span>タグ: {article.tags}</span>}
            <span>投稿者: {article.authorName}</span>
            <span>更新: {new Date(article.updatedAt).toLocaleDateString('ja-JP')}</span>
          </div>
        </div>
        {canEdit && (
          <div className="flex gap-2">
            <Link to={`/knowledge/${articleId}/edit`} className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50">
              編集
            </Link>
            <button onClick={handleDelete} className="rounded bg-red-600 px-3 py-1.5 text-sm text-white hover:bg-red-700">
              削除
            </button>
          </div>
        )}
      </div>

      {/* Article content */}
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <div className="prose max-w-none whitespace-pre-wrap text-gray-800">
          {article.content}
        </div>
      </div>

      {/* Related recipes */}
      {article.relatedRecipes.length > 0 && (
        <div className="mt-6">
          <h3 className="mb-2 text-sm font-semibold text-gray-600">関連レシピ</h3>
          <div className="flex flex-wrap gap-2">
            {article.relatedRecipes.map((recipe) => (
              <Link
                key={recipe.id}
                to={`/recipes/${recipe.id}`}
                className="rounded-full bg-gray-100 px-3 py-1 text-sm text-blue-700 hover:bg-blue-50"
              >
                {recipe.title}
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
