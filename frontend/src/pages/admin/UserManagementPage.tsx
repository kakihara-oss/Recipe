import { useUsers, useUpdateRole } from '../../hooks/useUsers'
import { useAuth } from '../../contexts/AuthContext'
import { ROLE_LABELS } from '../../constants'
import type { Role } from '../../types'

const roles: Role[] = ['CHEF', 'SERVICE', 'PURCHASER', 'PRODUCER']

export default function UserManagementPage() {
  const { user: currentUser } = useAuth()
  const { data: users, isLoading } = useUsers()
  const updateRoleMutation = useUpdateRole()

  const handleRoleChange = (userId: number, role: Role) => {
    updateRoleMutation.mutate({ userId, role })
  }

  if (isLoading) return <div className="py-12 text-center text-gray-500">読み込み中...</div>

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">メンバー管理</h2>

      <div className="overflow-x-auto rounded-lg border border-gray-200">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-gray-600">名前</th>
              <th className="px-4 py-3 text-left text-gray-600">メール</th>
              <th className="px-4 py-3 text-left text-gray-600">役割</th>
              <th className="px-4 py-3 text-left text-gray-600">登録日</th>
            </tr>
          </thead>
          <tbody className="bg-white">
            {users?.map((u) => {
              const isSelf = currentUser?.id === u.id
              return (
                <tr key={u.id} className="border-t border-gray-100">
                  <td className="px-4 py-3 font-medium text-gray-800">
                    {u.name}
                    {isSelf && <span className="ml-2 text-xs text-gray-400">(自分)</span>}
                  </td>
                  <td className="px-4 py-3 text-gray-600">{u.email}</td>
                  <td className="px-4 py-3">
                    {isSelf ? (
                      <span className="text-gray-600">{ROLE_LABELS[u.role]}</span>
                    ) : (
                      <select
                        value={u.role}
                        onChange={(e) => handleRoleChange(u.id, e.target.value as Role)}
                        disabled={updateRoleMutation.isPending}
                        className="rounded border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
                      >
                        {roles.map((r) => (
                          <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                        ))}
                      </select>
                    )}
                  </td>
                  <td className="px-4 py-3 text-gray-600">
                    {new Date(u.createdAt).toLocaleDateString('ja-JP')}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
