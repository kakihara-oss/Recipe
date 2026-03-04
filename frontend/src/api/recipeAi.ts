import apiClient from './client'
import type {
  AiRecipeDraftResponse,
  AiImproveFieldResponse,
  AiGenerateRecipeRequest,
  AiGenerateFromRecipeRequest,
  AiImproveFieldRequest,
  AiModifyRecipeRequest,
} from '../types'

export async function generateRecipeFromTheme(req: AiGenerateRecipeRequest): Promise<AiRecipeDraftResponse> {
  const { data } = await apiClient.post('/recipes/ai/generate', req)
  return data
}

export async function generateRecipeFromExisting(req: AiGenerateFromRecipeRequest): Promise<AiRecipeDraftResponse> {
  const { data } = await apiClient.post('/recipes/ai/generate-from-recipe', req)
  return data
}

export async function improveField(req: AiImproveFieldRequest): Promise<AiImproveFieldResponse> {
  const { data } = await apiClient.post('/recipes/ai/improve-field', req)
  return data
}

export async function modifyRecipe(req: AiModifyRecipeRequest): Promise<AiRecipeDraftResponse> {
  const { data } = await apiClient.post('/recipes/ai/modify', req)
  return data
}
