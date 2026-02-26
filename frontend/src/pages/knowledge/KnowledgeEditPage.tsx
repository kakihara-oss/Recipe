import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useArticle, useCategories, useUpdateArticle } from '../../hooks/useKnowledge'
import type { UpdateKnowledgeArticleRequest } from '../../types'

export default function KnowledgeEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const articleId = Number(id)
  const { data: article, isLoading } = useArticle(articleId)
  const { data: categories } = useCategories()
  const updateMutation = useUpdateArticle(articleId)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [tags, setTags] = useState('')

  useEffect(() => {
    if (article) {
      setTitle(article.title)
      setContent(article.content)
      setCategoryId(String(article.categoryId))
      setTags(article.tags ?? '')
    }
  }, [article])

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!article) return <div className="py-12 text-center text-gray-400">記事が見つかりません</div>

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: UpdateKnowledgeArticleRequest = {
      title,
      content,
      categoryId: Number(categoryId),
      tags: tags || undefined,
    }
    updateMutation.mutate(req, {
      onSuccess: () => navigate(`/knowledge/${articleId}`),
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to={`/knowledge/${articleId}`} className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 記事に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">ナレッジ記事編集</h2>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">タイトル *</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={200}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">カテゴリ *</label>
          <select
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="">選択してください</option>
            {categories?.map((cat) => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">本文 * (Markdown形式)</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={12}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 font-mono text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">タグ (カンマ区切り)</label>
          <input
            type="text"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="flex justify-end gap-3 pt-4">
          <button type="button" onClick={() => navigate(`/knowledge/${articleId}`)} className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            キャンセル
          </button>
          <button
            type="submit"
            disabled={updateMutation.isPending || !title.trim() || !content.trim() || !categoryId}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {updateMutation.isPending ? '保存中...' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}
