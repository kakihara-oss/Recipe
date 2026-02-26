import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()

  useEffect(() => {
    const token = searchParams.get('token')
    if (token) {
      login(token)
      navigate('/', { replace: true })
    } else {
      navigate('/login', { replace: true })
    }
  }, [searchParams, login, navigate])

  return (
    <div className="flex h-screen items-center justify-center">
      <div className="text-gray-500">認証処理中...</div>
    </div>
  )
}
