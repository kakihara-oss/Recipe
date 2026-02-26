import { Navigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import type { Role } from '../../types'

interface Props {
  children: React.ReactNode
  allowedRoles?: Role[]
}

export default function ProtectedRoute({ children, allowedRoles }: Props) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}
