import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSearchArticles } from '../../hooks/useKnowledge'

export default function KnowledgeSearchPage() {
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const { data: results, isLoading } = useSearchArticles(keyword)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setKeyword(input.trim())
  }

  return (
    <div>
      <Link to="/knowledge" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 一覧に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">ナレッジ検索</h2>

      <form onSubmit={handleSearch} className="mb-6 flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="キーワードを入力..."
          className="flex-1 rounded-lg border border-gray-300 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={!input.trim()}
          className="rounded-lg bg-blue-600 px-6 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          検索
        </button>
      </form>

      {isLoading ? (
        <div className="py-12 text-center text-gray-500">検索中...</div>
      ) : keyword && results ? (
        results.length > 0 ? (
          <div className="space-y-3">
            <p className="text-sm text-gray-500">{results.length}件の結果</p>
            {results.map((article) => (
              <Link
                key={article.id}
                to={`/knowledge/${article.id}`}
                className="block rounded-lg border border-gray-200 bg-white p-4 transition-shadow hover:shadow-md"
              >
                <h3 className="mb-1 text-base font-semibold text-gray-800">{article.title}</h3>
                <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500">
                  <span className="rounded bg-blue-50 px-2 py-0.5 text-blue-700">{article.categoryName}</span>
                  {article.tags && <span>{article.tags}</span>}
                  <span>{article.authorName}</span>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="py-12 text-center text-gray-400">
            「{keyword}」に一致する記事が見つかりませんでした
          </div>
        )
      ) : null}
    </div>
  )
}
