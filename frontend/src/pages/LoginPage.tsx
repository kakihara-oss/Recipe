import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import apiClient from '../api/client'

const DEV_ROLES = ['PRODUCER', 'CHEF', 'SERVICE', 'PURCHASER'] as const

export default function LoginPage() {
  const { user, loading, login } = useAuth()
  const [devLoading, setDevLoading] = useState(false)

  const handleDevLogin = async (role: string) => {
    setDevLoading(true)
    try {
      const { data } = await apiClient.post('/dev/login', { role })
      login(data.token)
    } catch {
      alert('開発用ログインに失敗しました。バックエンドが dev プロファイルで起動しているか確認してください。')
    } finally {
      setDevLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    )
  }

  if (user) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h1 className="mb-2 text-center text-2xl font-bold text-gray-800">
          感動創出レシピツール
        </h1>
        <p className="mb-8 text-center text-sm text-gray-500">
          Kando Recipe
        </p>
        <a
          href="/oauth2/authorization/google"
          className="flex w-full items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50"
        >
          <svg className="h-5 w-5" viewBox="0 0 24 24">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"
              fill="#4285F4"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              fill="#34A853"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              fill="#FBBC05"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              fill="#EA4335"
            />
          </svg>
          Googleアカウントでログイン
        </a>

        <div className="mt-6 border-t pt-6">
          <p className="mb-3 text-center text-xs text-gray-400">
            開発用ログイン（devプロファイル時のみ有効）
          </p>
          <div className="grid grid-cols-2 gap-2">
            {DEV_ROLES.map((role) => (
              <button
                key={role}
                onClick={() => handleDevLogin(role)}
                disabled={devLoading}
                className="rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 disabled:opacity-50"
              >
                {role}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
