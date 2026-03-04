import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCreateRecipe } from '../../hooks/useRecipes'
import { useIngredients } from '../../hooks/useIngredients'
import AiGenerateButton from '../../components/recipe/AiGenerateButton'
import AiGenerateFromRecipeButton from '../../components/recipe/AiGenerateFromRecipeButton'
import type { CreateRecipeRequest, AiRecipeDraftResponse } from '../../types'

interface StepInput {
  stepNumber: number
  description: string
  durationMinutes: string
  temperature: string
  tips: string
}

interface IngredientInput {
  ingredientId: string
  quantity: string
  unit: string
  preparationNote: string
  substitutes: string
}

export default function RecipeCreatePage() {
  const navigate = useNavigate()
  const createMutation = useCreateRecipe()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [servings, setServings] = useState('')
  const [concept, setConcept] = useState('')
  const [story, setStory] = useState('')
  const [steps, setSteps] = useState<StepInput[]>([
    { stepNumber: 1, description: '', durationMinutes: '', temperature: '', tips: '' },
  ])
  const [ingredientInputs, setIngredientInputs] = useState<IngredientInput[]>([])
  const [ingredientSearch, setIngredientSearch] = useState('')

  const { data: ingredientsList } = useIngredients({ keyword: ingredientSearch || undefined, size: 100 })

  const applyAiDraft = (draft: AiRecipeDraftResponse) => {
    if (draft.title) setTitle(draft.title)
    if (draft.description) setDescription(draft.description)
    if (draft.category) setCategory(draft.category)
    if (draft.servings) setServings(String(draft.servings))
    if (draft.concept) setConcept(draft.concept)
    if (draft.story) setStory(draft.story)
    if (draft.cookingSteps && draft.cookingSteps.length > 0) {
      setSteps(draft.cookingSteps.map((s, i) => ({
        stepNumber: s.stepNumber ?? i + 1,
        description: s.description ?? '',
        durationMinutes: s.durationMinutes ? String(s.durationMinutes) : '',
        temperature: s.temperature ?? '',
        tips: s.tips ?? '',
      })))
    }
  }

  const addStep = () => {
    setSteps([...steps, {
      stepNumber: steps.length + 1,
      description: '',
      durationMinutes: '',
      temperature: '',
      tips: '',
    }])
  }

  const removeStep = (index: number) => {
    const updated: StepInput[] = steps.filter((_, i) => i !== index).map((s, i) => ({ ...s, stepNumber: i + 1 }))
    setSteps(updated)
  }

  const updateStep = (index: number, field: Exclude<keyof StepInput, 'stepNumber'>, value: string) => {
    setSteps(steps.map((s, i) => i === index ? { ...s, [field]: value } as StepInput : s))
  }

  const addIngredient = () => {
    setIngredientInputs([...ingredientInputs, { ingredientId: '', quantity: '', unit: '', preparationNote: '', substitutes: '' }])
  }

  const removeIngredient = (index: number) => {
    setIngredientInputs(ingredientInputs.filter((_, i) => i !== index))
  }

  const updateIngredient = (index: number, field: keyof IngredientInput, value: string) => {
    setIngredientInputs(ingredientInputs.map((ing, i) => i === index ? { ...ing, [field]: value } : ing))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: CreateRecipeRequest = {
      title,
      description: description || undefined,
      category: category || undefined,
      servings: servings ? Number(servings) : undefined,
      concept: concept || undefined,
      story: story || undefined,
      cookingSteps: steps
        .filter((s) => s.description.trim())
        .map((s) => ({
          stepNumber: s.stepNumber,
          description: s.description,
          durationMinutes: s.durationMinutes ? Number(s.durationMinutes) : undefined,
          temperature: s.temperature || undefined,
          tips: s.tips || undefined,
        })),
      ingredients: ingredientInputs
        .filter((ing) => ing.ingredientId)
        .map((ing) => ({
          ingredientId: Number(ing.ingredientId),
          quantity: ing.quantity ? Number(ing.quantity) : undefined,
          unit: ing.unit || undefined,
          preparationNote: ing.preparationNote || undefined,
          substitutes: ing.substitutes || undefined,
        })),
    }
    createMutation.mutate(req, {
      onSuccess: (recipe) => navigate(`/recipes/${recipe.id}`),
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="mb-4 text-2xl font-bold text-gray-800">新規レシピ作成</h2>

      {/* AI Generate Buttons */}
      <div className="mb-6 flex flex-wrap gap-3">
        <AiGenerateButton onApply={applyAiDraft} />
        <AiGenerateFromRecipeButton onApply={applyAiDraft} />
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
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
          <label className="mb-1 block text-sm font-medium text-gray-700">説明</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            maxLength={2000}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">カテゴリ</label>
            <input
              type="text"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              maxLength={100}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">人数</label>
            <input
              type="number"
              value={servings}
              onChange={(e) => setServings(e.target.value)}
              min={1}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">コンセプト</label>
          <textarea
            value={concept}
            onChange={(e) => setConcept(e.target.value)}
            rows={2}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">ストーリー</label>
          <textarea
            value={story}
            onChange={(e) => setStory(e.target.value)}
            rows={2}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>

        {/* Cooking Steps */}
        <div>
          <h3 className="mb-3 text-lg font-semibold text-gray-700">調理手順</h3>
          {steps.map((step, index) => (
            <div key={index} className="mb-3 rounded-lg border border-gray-200 p-4">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-sm font-medium text-gray-600">手順 {step.stepNumber}</span>
                {steps.length > 1 && (
                  <button type="button" onClick={() => removeStep(index)} className="text-xs text-red-500 hover:underline">
                    削除
                  </button>
                )}
              </div>
              <textarea
                placeholder="手順の説明 *"
                value={step.description}
                onChange={(e) => updateStep(index, 'description', e.target.value)}
                rows={2}
                className="mb-2 w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
              <div className="grid grid-cols-3 gap-2">
                <input
                  type="number"
                  placeholder="時間(分)"
                  value={step.durationMinutes}
                  onChange={(e) => updateStep(index, 'durationMinutes', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
                <input
                  type="text"
                  placeholder="温度"
                  value={step.temperature}
                  onChange={(e) => updateStep(index, 'temperature', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
                <input
                  type="text"
                  placeholder="コツ"
                  value={step.tips}
                  onChange={(e) => updateStep(index, 'tips', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
              </div>
            </div>
          ))}
          <button
            type="button"
            onClick={addStep}
            className="text-sm text-blue-600 hover:underline"
          >
            + 手順を追加
          </button>
        </div>

        {/* Ingredients */}
        <div>
          <h3 className="mb-3 text-lg font-semibold text-gray-700">食材</h3>
          {ingredientInputs.map((ing, index) => (
            <div key={index} className="mb-3 rounded-lg border border-gray-200 p-4">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-sm font-medium text-gray-600">食材 {index + 1}</span>
                <button type="button" onClick={() => removeIngredient(index)} className="text-xs text-red-500 hover:underline">
                  削除
                </button>
              </div>
              <div className="mb-2">
                <select
                  value={ing.ingredientId}
                  onChange={(e) => updateIngredient(index, 'ingredientId', e.target.value)}
                  className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                >
                  <option value="">食材を選択...</option>
                  {ingredientsList?.content.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}{item.category ? ` (${item.category})` : ''}
                    </option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="number"
                  placeholder="数量"
                  value={ing.quantity}
                  onChange={(e) => updateIngredient(index, 'quantity', e.target.value)}
                  step="0.1"
                  min="0"
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
                <input
                  type="text"
                  placeholder="単位 (例: g, 個, 本)"
                  value={ing.unit}
                  onChange={(e) => updateIngredient(index, 'unit', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
              </div>
              <div className="mt-2 grid grid-cols-2 gap-2">
                <input
                  type="text"
                  placeholder="下処理メモ"
                  value={ing.preparationNote}
                  onChange={(e) => updateIngredient(index, 'preparationNote', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
                <input
                  type="text"
                  placeholder="代替食材"
                  value={ing.substitutes}
                  onChange={(e) => updateIngredient(index, 'substitutes', e.target.value)}
                  className="rounded border border-gray-300 px-2 py-1.5 text-sm focus:border-blue-500 focus:outline-none"
                />
              </div>
            </div>
          ))}
          <button
            type="button"
            onClick={addIngredient}
            className="text-sm text-blue-600 hover:underline"
          >
            + 食材を追加
          </button>
          {ingredientInputs.length > 0 && (
            <div className="mt-2">
              <input
                type="text"
                placeholder="食材名で絞り込み..."
                value={ingredientSearch}
                onChange={(e) => setIngredientSearch(e.target.value)}
                className="rounded border border-gray-300 px-2 py-1.5 text-xs text-gray-500 focus:border-blue-500 focus:outline-none"
              />
            </div>
          )}
        </div>

        <div className="flex justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => navigate('/recipes')}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={createMutation.isPending || !title.trim()}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {createMutation.isPending ? '作成中...' : '作成'}
          </button>
        </div>
      </form>
    </div>
  )
}
