import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useRecipe, useUpdateRecipe } from '../../hooks/useRecipes'
import type { UpdateRecipeRequest } from '../../types'

export default function RecipeEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const recipeId = Number(id)
  const { data: recipe, isLoading } = useRecipe(recipeId)
  const updateMutation = useUpdateRecipe(recipeId)

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [servings, setServings] = useState('')
  const [concept, setConcept] = useState('')
  const [story, setStory] = useState('')

  useEffect(() => {
    if (recipe) {
      setTitle(recipe.title)
      setDescription(recipe.description ?? '')
      setCategory(recipe.category ?? '')
      setServings(recipe.servings?.toString() ?? '')
      setConcept(recipe.concept ?? '')
      setStory(recipe.story ?? '')
    }
  }, [recipe])

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!recipe) return <div className="py-12 text-center text-gray-400">レシピが見つかりません</div>

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: UpdateRecipeRequest = {
      title,
      description: description || undefined,
      category: category || undefined,
      servings: servings ? Number(servings) : undefined,
      concept: concept || undefined,
      story: story || undefined,
    }
    updateMutation.mutate(req, {
      onSuccess: () => navigate(`/recipes/${recipeId}`),
    })
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to={`/recipes/${recipeId}`} className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 詳細に戻る
      </Link>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">レシピ編集</h2>
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
        <div className="flex justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => navigate(`/recipes/${recipeId}`)}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={updateMutation.isPending || !title.trim()}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {updateMutation.isPending ? '保存中...' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}
