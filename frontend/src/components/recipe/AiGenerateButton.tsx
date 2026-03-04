import { useState } from 'react'
import { useGenerateFromTheme } from '../../hooks/useRecipeAi'
import type { AiRecipeDraftResponse } from '../../types'

interface AiGenerateButtonProps {
  onApply: (draft: AiRecipeDraftResponse) => void
}

export default function AiGenerateButton({ onApply }: AiGenerateButtonProps) {
  const [open, setOpen] = useState(false)
  const [theme, setTheme] = useState('')
  const mutation = useGenerateFromTheme()

  const handleGenerate = () => {
    if (!theme.trim()) return
    mutation.mutate(
      { theme },
      {
        onSuccess: (draft) => {
          onApply(draft)
          setOpen(false)
          setTheme('')
        },
      },
    )
  }

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="rounded-lg bg-purple-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-purple-700"
      >
        AIでレシピを生成
      </button>
    )
  }

  return (
    <div className="rounded-lg border border-purple-200 bg-purple-50 p-4">
      <h4 className="mb-2 text-sm font-semibold text-purple-800">AIレシピ生成</h4>
      <p className="mb-3 text-xs text-purple-600">テーマを入力するとAIがレシピの下書きを生成します</p>
      <input
        type="text"
        value={theme}
        onChange={(e) => setTheme(e.target.value)}
        placeholder="例: 春の前菜、記念日ディナー、和風デザート"
        maxLength={200}
        className="mb-3 w-full rounded border border-purple-300 px-3 py-2 text-sm focus:border-purple-500 focus:outline-none"
      />
      <div className="flex gap-2">
        <button
          type="button"
          onClick={handleGenerate}
          disabled={mutation.isPending || !theme.trim()}
          className="rounded bg-purple-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-purple-700 disabled:opacity-50"
        >
          {mutation.isPending ? '生成中...' : '生成する'}
        </button>
        <button
          type="button"
          onClick={() => { setOpen(false); setTheme('') }}
          className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
        >
          キャンセル
        </button>
      </div>
      {mutation.isError && (
        <p className="mt-2 text-xs text-red-500">生成に失敗しました。もう一度お試しください。</p>
      )}
    </div>
  )
}
