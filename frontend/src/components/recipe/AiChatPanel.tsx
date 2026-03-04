import { useState } from 'react'
import { useModifyRecipe } from '../../hooks/useRecipeAi'
import type { AiRecipeDraftResponse, AiModifyRecipeRequest } from '../../types'

interface AiChatPanelProps {
  currentRecipe: AiModifyRecipeRequest['currentRecipe']
  onApply: (draft: AiRecipeDraftResponse) => void
}

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
}

export default function AiChatPanel({ currentRecipe, onApply }: AiChatPanelProps) {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [lastDraft, setLastDraft] = useState<AiRecipeDraftResponse | null>(null)
  const mutation = useModifyRecipe()

  const handleSend = () => {
    if (!input.trim() || mutation.isPending) return
    const userMsg = input.trim()
    setMessages((prev) => [...prev, { role: 'user', content: userMsg }])
    setInput('')

    mutation.mutate(
      { instruction: userMsg, currentRecipe },
      {
        onSuccess: (draft) => {
          setLastDraft(draft)
          setMessages((prev) => [
            ...prev,
            {
              role: 'ai',
              content: `レシピを修正しました。\nタイトル: ${draft.title}\n手順数: ${draft.cookingSteps?.length ?? 0}件\n食材数: ${draft.ingredients?.length ?? 0}件\n\n「適用」ボタンでフォームに反映できます。`,
            },
          ])
        },
        onError: () => {
          setMessages((prev) => [
            ...prev,
            { role: 'ai', content: '申し訳ございません。修正に失敗しました。もう一度お試しください。' },
          ])
        },
      },
    )
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="fixed bottom-6 right-6 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-purple-600 text-white shadow-lg hover:bg-purple-700"
        title="AIに修正を相談"
      >
        AI
      </button>
    )
  }

  return (
    <div className="fixed bottom-6 right-6 z-50 flex h-[500px] w-[380px] flex-col rounded-xl border border-gray-200 bg-white shadow-2xl">
      {/* Header */}
      <div className="flex items-center justify-between border-b px-4 py-3">
        <h4 className="text-sm font-semibold text-purple-800">AI修正アシスタント</h4>
        <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600">x</button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {messages.length === 0 && (
          <p className="text-center text-xs text-gray-400">修正したい内容を入力してください</p>
        )}
        {messages.map((msg, i) => (
          <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div
              className={`max-w-[85%] rounded-lg px-3 py-2 text-sm ${
                msg.role === 'user'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-100 text-gray-800'
              }`}
            >
              <p className="whitespace-pre-wrap">{msg.content}</p>
            </div>
          </div>
        ))}
        {mutation.isPending && (
          <div className="flex justify-start">
            <div className="rounded-lg bg-gray-100 px-3 py-2 text-sm text-gray-500">
              考え中...
            </div>
          </div>
        )}
      </div>

      {/* Apply button */}
      {lastDraft && (
        <div className="border-t px-4 py-2">
          <button
            type="button"
            onClick={() => { onApply(lastDraft); setLastDraft(null) }}
            className="w-full rounded bg-purple-600 py-1.5 text-sm font-medium text-white hover:bg-purple-700"
          >
            最新の提案をフォームに適用
          </button>
        </div>
      )}

      {/* Input */}
      <div className="border-t p-3">
        <div className="flex gap-2">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="修正指示を入力... (Enter: 送信)"
            rows={2}
            className="flex-1 resize-none rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-purple-500 focus:outline-none"
          />
          <button
            type="button"
            onClick={handleSend}
            disabled={mutation.isPending || !input.trim()}
            className="self-end rounded bg-purple-600 px-3 py-1.5 text-sm text-white hover:bg-purple-700 disabled:opacity-50"
          >
            送信
          </button>
        </div>
      </div>
    </div>
  )
}
