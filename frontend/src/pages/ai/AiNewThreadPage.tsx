import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCreateThread } from '../../hooks/useAi'

export default function AiNewThreadPage() {
  const navigate = useNavigate()
  const createMutation = useCreateThread()

  const [theme, setTheme] = useState('')
  const [initialMessage, setInitialMessage] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    createMutation.mutate(
      { theme, initialMessage },
      {
        onSuccess: (thread) => navigate(`/ai/threads/${thread.id}`),
      },
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to="/ai" className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; AI相談に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">新規AI相談</h2>
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">相談テーマ *</label>
          <input
            type="text"
            value={theme}
            onChange={(e) => setTheme(e.target.value)}
            maxLength={200}
            required
            placeholder="例: 春の新作デザートのサービス演出"
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">最初のメッセージ *</label>
          <textarea
            value={initialMessage}
            onChange={(e) => setInitialMessage(e.target.value)}
            required
            rows={6}
            placeholder="AIに相談したい内容を入力..."
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="flex justify-end gap-3 pt-4">
          <button type="button" onClick={() => navigate('/ai')} className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            キャンセル
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || !theme.trim() || !initialMessage.trim()}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {createMutation.isPending ? '作成中...' : '相談を始める'}
          </button>
        </div>
      </form>
    </div>
  )
}
