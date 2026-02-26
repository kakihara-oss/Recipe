import { Link } from 'react-router-dom'
import type { RecipeListResponse } from '../../types'
import RecipeStatusBadge from './RecipeStatusBadge'

export default function RecipeCard({ recipe }: { recipe: RecipeListResponse }) {
  return (
    <Link
      to={`/recipes/${recipe.id}`}
      className="block rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-shadow hover:shadow-md"
    >
      <h3 className="mb-1 truncate text-base font-semibold text-gray-800">{recipe.title}</h3>
      {recipe.category && (
        <p className="mb-2 text-xs text-gray-500">{recipe.category}</p>
      )}
      <div className="mb-2 flex items-center gap-2">
        <RecipeStatusBadge status={recipe.status} />
        {recipe.servings && (
          <span className="text-xs text-gray-500">{recipe.servings}人前</span>
        )}
      </div>
      <div className="flex items-center justify-between text-xs text-gray-400">
        <span>{recipe.createdByName}</span>
        <span>{new Date(recipe.updatedAt).toLocaleDateString('ja-JP')}</span>
      </div>
    </Link>
  )
}
