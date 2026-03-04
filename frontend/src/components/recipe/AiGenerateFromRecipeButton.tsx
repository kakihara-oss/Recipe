import { useState } from 'react'
import { useGenerateFromRecipe } from '../../hooks/useRecipeAi'
import { useRecipes } from '../../hooks/useRecipes'
import type { AiRecipeDraftResponse } from '../../types'

interface AiGenerateFromRecipeButtonProps {
  onApply: (draft: AiRecipeDraftResponse) => void
}

export default function AiGenerateFromRecipeButton({ onApply }: AiGenerateFromRecipeButtonProps) {
  const [open, setOpen] = useState(false)
  const [selectedRecipeId, setSelectedRecipeId] = useState<number | null>(null)
  const [instruction, setInstruction] = useState('')
  const mutation = useGenerateFromRecipe()
  const { data: recipesPage } = useRecipes({ status: 'PUBLISHED', size: 100 })

  const handleGenerate = () => {
    if (!selectedRecipeId) return
    mutation.mutate(
      { recipeId: selectedRecipeId, arrangementInstruction: instruction || undefined },
      {
        onSuccess: (draft) => {
          onApply(draft)
          setOpen(false)
          setSelectedRecipeId(null)
          setInstruction('')
        },
      },
    )
  }

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="rounded-lg border border-purple-300 px-3 py-1.5 text-sm font-medium text-purple-700 hover:bg-purple-50"
      >
        既存レシピからアレンジ
      </button>
    )
  }

  return (
    <div className="rounded-lg border border-purple-200 bg-purple-50 p-4">
      <h4 className="mb-2 text-sm font-semibold text-purple-800">既存レシピからアレンジ生成</h4>
      <div className="mb-3">
        <label className="mb-1 block text-xs text-purple-600">ベースレシピ *</label>
        <select
          value={selectedRecipeId ?? ''}
          onChange={(e) => setSelectedRecipeId(e.target.value ? Number(e.target.value) : null)}
          className="w-full rounded border border-purple-300 px-3 py-2 text-sm focus:border-purple-500 focus:outline-none"
        >
          <option value="">レシピを選択...</option>
          {recipesPage?.content.map((r) => (
            <option key={r.id} value={r.id}>{r.title}</option>
          ))}
        </select>
      </div>
      <div className="mb-3">
        <label className="mb-1 block text-xs text-purple-600">アレンジの方向性（任意）</label>
        <input
          type="text"
          value={instruction}
          onChange={(e) => setInstruction(e.target.value)}
          placeholder="例: 春バージョンにアレンジ、辛味を効かせて"
          maxLength={500}
          className="w-full rounded border border-purple-300 px-3 py-2 text-sm focus:border-purple-500 focus:outline-none"
        />
      </div>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={handleGenerate}
          disabled={mutation.isPending || !selectedRecipeId}
          className="rounded bg-purple-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-purple-700 disabled:opacity-50"
        >
          {mutation.isPending ? '生成中...' : 'アレンジ生成'}
        </button>
        <button
          type="button"
          onClick={() => { setOpen(false); setSelectedRecipeId(null); setInstruction('') }}
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
