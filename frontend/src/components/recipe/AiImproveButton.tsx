import { useState } from 'react'
import { useImproveField } from '../../hooks/useRecipeAi'

interface AiImproveButtonProps {
  fieldName: string
  fieldLabel: string
  currentValue: string
  recipeContext?: string
  onApply: (improvedValue: string) => void
}

export default function AiImproveButton({ fieldName, fieldLabel, currentValue, recipeContext, onApply }: AiImproveButtonProps) {
  const [showResult, setShowResult] = useState(false)
  const mutation = useImproveField()

  const handleImprove = () => {
    mutation.mutate(
      { fieldName, currentValue, recipeContext },
      { onSuccess: () => setShowResult(true) },
    )
  }

  const handleApply = () => {
    if (mutation.data) {
      onApply(mutation.data.improvedValue)
      setShowResult(false)
      mutation.reset()
    }
  }

  const handleDismiss = () => {
    setShowResult(false)
    mutation.reset()
  }

  return (
    <div className="inline-flex flex-col">
      <button
        type="button"
        onClick={handleImprove}
        disabled={mutation.isPending}
        className="rounded bg-purple-100 px-2 py-0.5 text-xs text-purple-700 hover:bg-purple-200 disabled:opacity-50"
        title={`${fieldLabel}をAIで改善`}
      >
        {mutation.isPending ? '...' : 'AI改善'}
      </button>
      {showResult && mutation.data && (
        <div className="mt-2 rounded border border-purple-200 bg-purple-50 p-3">
          <p className="mb-1 text-xs font-semibold text-purple-800">AI提案:</p>
          <p className="mb-2 whitespace-pre-wrap text-xs text-gray-700">{mutation.data.improvedValue}</p>
          <p className="mb-2 text-xs italic text-gray-500">{mutation.data.explanation}</p>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={handleApply}
              className="rounded bg-purple-600 px-2 py-1 text-xs text-white hover:bg-purple-700"
            >
              適用
            </button>
            <button
              type="button"
              onClick={handleDismiss}
              className="rounded border border-gray-300 px-2 py-1 text-xs text-gray-600 hover:bg-gray-50"
            >
              却下
            </button>
          </div>
        </div>
      )}
      {mutation.isError && (
        <p className="mt-1 text-xs text-red-500">改善に失敗しました</p>
      )}
    </div>
  )
}
