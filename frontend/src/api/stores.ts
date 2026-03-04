import apiClient from './client'
import type {
  StoreResponse,
  CreateStoreRequest,
  UpdateStoreRequest,
} from '../types'

export async function getStores(): Promise<StoreResponse[]> {
  const { data } = await apiClient.get('/stores')
  return data
}

export async function getStore(id: number): Promise<StoreResponse> {
  const { data } = await apiClient.get(`/stores/${id}`)
  return data
}

export async function createStore(req: CreateStoreRequest): Promise<StoreResponse> {
  const { data } = await apiClient.post('/stores', req)
  return data
}

export async function updateStore(id: number, req: UpdateStoreRequest): Promise<StoreResponse> {
  const { data } = await apiClient.put(`/stores/${id}`, req)
  return data
}
