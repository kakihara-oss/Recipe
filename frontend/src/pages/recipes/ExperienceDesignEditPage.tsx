import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useRecipe, useUpdateExperienceDesign } from '../../hooks/useRecipes'
import type { UpdateExperienceDesignRequest } from '../../types'

export default function ExperienceDesignEditPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const recipeId = Number(id)
  const { data: recipe, isLoading } = useRecipe(recipeId)
  const mutation = useUpdateExperienceDesign(recipeId)

  const [form, setForm] = useState({
    targetScene: '',
    emotionalKeyPoints: '',
    specialOccasionSupport: '',
    seasonalPresentation: '',
    sensoryAppeal: '',
  })

  useEffect(() => {
    if (recipe?.experienceDesign) {
      setForm({
        targetScene: recipe.experienceDesign.targetScene ?? '',
        emotionalKeyPoints: recipe.experienceDesign.emotionalKeyPoints ?? '',
        specialOccasionSupport: recipe.experienceDesign.specialOccasionSupport ?? '',
        seasonalPresentation: recipe.experienceDesign.seasonalPresentation ?? '',
        sensoryAppeal: recipe.experienceDesign.sensoryAppeal ?? '',
      })
    }
  }, [recipe])

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>
  if (!recipe) return <div className="py-12 text-center text-gray-400">レシピが見つかりません</div>

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const req: UpdateExperienceDesignRequest = {
      targetScene: form.targetScene || undefined,
      emotionalKeyPoints: form.emotionalKeyPoints || undefined,
      specialOccasionSupport: form.specialOccasionSupport || undefined,
      seasonalPresentation: form.seasonalPresentation || undefined,
      sensoryAppeal: form.sensoryAppeal || undefined,
    }
    mutation.mutate(req, {
      onSuccess: () => navigate(`/recipes/${recipeId}`),
    })
  }

  const fields: { key: keyof typeof form; label: string }[] = [
    { key: 'targetScene', label: 'ターゲットシーン' },
    { key: 'emotionalKeyPoints', label: '感動ポイント' },
    { key: 'specialOccasionSupport', label: '記念日対応' },
    { key: 'seasonalPresentation', label: '季節演出' },
    { key: 'sensoryAppeal', label: '五感への訴求' },
  ]

  return (
    <div className="mx-auto max-w-2xl">
      <Link to={`/recipes/${recipeId}`} className="mb-4 inline-block text-sm text-blue-600 hover:underline">
        &larr; 詳細に戻る
      </Link>
      <h2 className="mb-1 text-2xl font-bold text-gray-800">体験設計編集</h2>
      <p className="mb-6 text-sm text-gray-500">{recipe.title}</p>
      <form onSubmit={handleSubmit} className="space-y-5">
        {fields.map(({ key, label }) => (
          <div key={key}>
            <label className="mb-1 block text-sm font-medium text-gray-700">{label}</label>
            <textarea
              value={form[key]}
              onChange={(e) => setForm({ ...form, [key]: e.target.value })}
              rows={3}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
        ))}
        <div className="flex justify-end gap-3 pt-4">
          <button type="button" onClick={() => navigate(`/recipes/${recipeId}`)} className="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
            キャンセル
          </button>
          <button type="submit" disabled={mutation.isPending} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {mutation.isPending ? '保存中...' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}
