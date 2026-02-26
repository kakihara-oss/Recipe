import apiClient from './client'
import type {
  RecipeResponse,
  RecipeListResponse,
  RecipeHistoryResponse,
  CreateRecipeRequest,
  UpdateRecipeRequest,
  UpdateServiceDesignRequest,
  UpdateExperienceDesignRequest,
  UpdateStatusRequest,
  Page,
} from '../types'

export async function getRecipes(params?: {
  category?: string
  status?: string
  page?: number
  size?: number
}): Promise<Page<RecipeListResponse>> {
  const { data } = await apiClient.get('/recipes', { params })
  return data
}

export async function getRecipe(id: number): Promise<RecipeResponse> {
  const { data } = await apiClient.get(`/recipes/${id}`)
  return data
}

export async function createRecipe(req: CreateRecipeRequest): Promise<RecipeResponse> {
  const { data } = await apiClient.post('/recipes', req)
  return data
}

export async function updateRecipe(id: number, req: UpdateRecipeRequest): Promise<RecipeResponse> {
  const { data } = await apiClient.put(`/recipes/${id}`, req)
  return data
}

export async function updateServiceDesign(id: number, req: UpdateServiceDesignRequest): Promise<RecipeResponse> {
  const { data } = await apiClient.put(`/recipes/${id}/service-design`, req)
  return data
}

export async function updateExperienceDesign(id: number, req: UpdateExperienceDesignRequest): Promise<RecipeResponse> {
  const { data } = await apiClient.put(`/recipes/${id}/experience-design`, req)
  return data
}

export async function updateRecipeStatus(id: number, req: UpdateStatusRequest): Promise<RecipeResponse> {
  const { data } = await apiClient.put(`/recipes/${id}/status`, req)
  return data
}

export async function deleteRecipe(id: number): Promise<void> {
  await apiClient.delete(`/recipes/${id}`)
}

export async function getRecipeHistory(id: number): Promise<RecipeHistoryResponse[]> {
  const { data } = await apiClient.get(`/recipes/${id}/history`)
  return data
}
