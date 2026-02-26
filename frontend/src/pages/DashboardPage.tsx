import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { ROLE_LABELS } from '../constants'
import { canCreateRecipe, canCreateFeedback, canManageUsers } from '../utils/permissions'

export default function DashboardPage() {
  const { user } = useAuth()

  if (!user) return null

  const quickActions = [
    { to: '/recipes', label: 'レシピ一覧', show: true },
    { to: '/recipes/new', label: 'レシピ作成', show: canCreateRecipe(user.role) },
    { to: '/knowledge', label: 'ナレッジベース', show: true },
    { to: '/knowledge/new', label: 'ナレッジ投稿', show: true },
    { to: '/ai', label: 'AI相談', show: true },
    { to: '/feedbacks/new', label: 'フィードバック登録', show: canCreateFeedback(user.role) },
    { to: '/admin/users', label: 'メンバー管理', show: canManageUsers(user.role) },
  ].filter((a) => a.show)

  return (
    <div>
      <h2 className="mb-1 text-2xl font-bold text-gray-800">
        ようこそ、{user.name}さん
      </h2>
      <p className="mb-8 text-sm text-gray-500">
        {ROLE_LABELS[user.role]}としてログイン中
      </p>

      <section>
        <h3 className="mb-4 text-lg font-semibold text-gray-700">クイックアクション</h3>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          {quickActions.map((action) => (
            <Link
              key={action.to}
              to={action.to}
              className="rounded-lg border border-gray-200 bg-white p-4 text-center text-sm font-medium text-gray-700 shadow-sm transition-colors hover:bg-blue-50 hover:text-blue-700"
            >
              {action.label}
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}
