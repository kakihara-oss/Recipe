import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCategories, useCreateArticle } from '../../hooks/useKnowledge'
import type { CreateKnowledgeArticleRequest } from '../../types'

export default function KnowledgeCreatePage() {
  const navigate = useNavigate()
  const { data: categories } = useCategories()
  const createMutation = useCreateArticle()

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [tags, setTags] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: CreateKnowledgeArticleRequest = {
      title,
      content,
      categoryId: Number(categoryId),
      tags: tags || undefined,
    }
    createMutation.mutate(req, {
      onSuccess: (article) => navigate(`/knowledge/${article.id}`),
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to="/knowledge" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 一覧に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">ナレッジ記事作成</h2>
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
            placeholder="例: 春,野菜,調理"
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="flex justify-end gap-3 pt-4">
          <button type="button" onClick={() => navigate('/knowledge')} className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            キャンセル
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || !title.trim() || !content.trim() || !categoryId}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {createMutation.isPending ? '作成中...' : '作成'}
          </button>
        </div>
      </form>
    </div>
  )
}
