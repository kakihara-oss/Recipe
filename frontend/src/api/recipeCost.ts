import apiClient from './client'
import type { RecipeCostResponse, UpdateRecipeCostRequest } from '../types'

export async function getRecipeCost(recipeId: number): Promise<RecipeCostResponse> {
  const { data } = await apiClient.get(`/recipes/${recipeId}/cost`)
  return data
}

export async function calculateRecipeCost(recipeId: number): Promise<RecipeCostResponse> {
  const { data } = await apiClient.post(`/recipes/${recipeId}/cost/calculate`)
  return data
}

export async function updateRecipeCost(recipeId: number, req: UpdateRecipeCostRequest): Promise<RecipeCostResponse> {
  const { data } = await apiClient.put(`/recipes/${recipeId}/cost`, req)
  return data
}

export async function getWarningRecipes(): Promise<RecipeCostResponse[]> {
  const { data } = await apiClient.get('/recipes/costs/warnings')
  return data
}
