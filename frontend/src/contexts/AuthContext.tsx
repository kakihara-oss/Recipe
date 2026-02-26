import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import type { UserResponse } from '../types'
import { getMe } from '../api/users'

interface AuthContextType {
  user: UserResponse | null
  loading: boolean
  login: (token: string) => void
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchUser = useCallback(async () => {
    const token = localStorage.getItem('jwt_token')
    if (!token) {
      setUser(null)
      setLoading(false)
      return
    }
    try {
      const me = await getMe()
      setUser(me)
    } catch {
      localStorage.removeItem('jwt_token')
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchUser()
  }, [fetchUser])

  const login = useCallback((token: string) => {
    localStorage.setItem('jwt_token', token)
    setLoading(true)
    getMe()
      .then(setUser)
      .catch(() => {
        localStorage.removeItem('jwt_token')
        setUser(null)
      })
      .finally(() => setLoading(false))
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('jwt_token')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser: fetchUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
