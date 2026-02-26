import type { RecipeStatus } from '../../types'
import { STATUS_LABELS } from '../../constants'

const statusColors: Record<RecipeStatus, string> = {
  DRAFT: 'bg-yellow-100 text-yellow-800',
  PUBLISHED: 'bg-green-100 text-green-800',
  ARCHIVED: 'bg-gray-100 text-gray-600',
  DELETED: 'bg-red-100 text-red-800',
}

export default function RecipeStatusBadge({ status }: { status: RecipeStatus }) {
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusColors[status]}`}>
      {STATUS_LABELS[status]}
    </span>
  )
}
