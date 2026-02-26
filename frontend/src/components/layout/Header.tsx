import { useAuth } from '../../contexts/AuthContext'
import { ROLE_LABELS } from '../../constants'

interface Props {
  onToggleSidebar: () => void
}

export default function Header({ onToggleSidebar }: Props) {
  const { user, logout } = useAuth()

  return (
    <header className="flex h-14 items-center justify-between border-b border-gray-200 bg-white px-4">
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="rounded p-1 text-gray-500 hover:bg-gray-100 lg:hidden"
          aria-label="メニューを開く"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <h1 className="text-lg font-semibold text-gray-800">感動創出レシピツール</h1>
      </div>

      {user && (
        <div className="flex items-center gap-4">
          <div className="hidden text-sm text-gray-600 sm:block">
            <span className="font-medium">{user.name}</span>
            <span className="ml-2 rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-500">
              {ROLE_LABELS[user.role]}
            </span>
          </div>
          <button
            onClick={logout}
            className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
          >
            ログアウト
          </button>
        </div>
      )}
    </header>
  )
}
