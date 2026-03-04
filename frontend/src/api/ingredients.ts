import apiClient from './client'
import type {
  IngredientResponse,
  IngredientListResponse,
  IngredientPriceResponse,
  IngredientSeasonResponse,
  AffectedRecipeResponse,
  CreateIngredientRequest,
  UpdateIngredientRequest,
  CreateIngredientPriceRequest,
  UpdateIngredientSeasonRequest,
  SupplyStatus,
  Page,
} from '../types'

export async function getIngredients(params?: {
  category?: string
  supplyStatus?: SupplyStatus
  keyword?: string
  page?: number
  size?: number
}): Promise<Page<IngredientListResponse>> {
  const { data } = await apiClient.get('/ingredients', { params })
  return data
}

export async function getIngredient(id: number): Promise<IngredientResponse> {
  const { data } = await apiClient.get(`/ingredients/${id}`)
  return data
}

export async function createIngredient(req: CreateIngredientRequest): Promise<IngredientResponse> {
  const { data } = await apiClient.post('/ingredients', req)
  return data
}

export async function updateIngredient(id: number, req: UpdateIngredientRequest): Promise<IngredientResponse> {
  const { data } = await apiClient.put(`/ingredients/${id}`, req)
  return data
}

export async function updateSupplyStatus(id: number, supplyStatus: SupplyStatus): Promise<IngredientResponse> {
  const { data } = await apiClient.put(`/ingredients/${id}/supply-status`, { supplyStatus })
  return data
}

export async function addPrice(id: number, req: CreateIngredientPriceRequest): Promise<IngredientPriceResponse> {
  const { data } = await apiClient.post(`/ingredients/${id}/prices`, req)
  return data
}

export async function getPriceHistory(id: number): Promise<IngredientPriceResponse[]> {
  const { data } = await apiClient.get(`/ingredients/${id}/prices`)
  return data
}

export async function updateSeasons(id: number, seasons: UpdateIngredientSeasonRequest[]): Promise<IngredientSeasonResponse[]> {
  const { data } = await apiClient.put(`/ingredients/${id}/seasons`, seasons)
  return data
}

export async function getSeasons(id: number): Promise<IngredientSeasonResponse[]> {
  const { data } = await apiClient.get(`/ingredients/${id}/seasons`)
  return data
}

export async function getAffectedRecipes(id: number): Promise<AffectedRecipeResponse[]> {
  const { data } = await apiClient.get(`/ingredients/${id}/affected-recipes`)
  return data
}
