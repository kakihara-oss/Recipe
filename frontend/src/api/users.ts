import apiClient from './client'
import type { UserResponse, UpdateRoleRequest } from '../types'

export async function getMe(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>('/users/me')
  return data
}

export async function getUsers(): Promise<UserResponse[]> {
  const { data } = await apiClient.get<UserResponse[]>('/users')
  return data
}

export async function updateUserRole(userId: number, req: UpdateRoleRequest): Promise<UserResponse> {
  const { data } = await apiClient.put<UserResponse>(`/users/${userId}/role`, req)
  return data
}
