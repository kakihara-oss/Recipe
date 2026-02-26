import apiClient from './client'
import type {
  ProductFeedbackResponse,
  FeedbackSummaryResponse,
  CreateProductFeedbackRequest,
  GenerateFeedbackSummaryRequest,
  Page,
} from '../types'

export async function getFeedbacks(params?: {
  recipeId?: number
  storeId?: number
  page?: number
  size?: number
}): Promise<Page<ProductFeedbackResponse>> {
  const { data } = await apiClient.get('/feedbacks', { params })
  return data
}

export async function getFeedback(id: number): Promise<ProductFeedbackResponse> {
  const { data } = await apiClient.get(`/feedbacks/${id}`)
  return data
}

export async function createFeedback(req: CreateProductFeedbackRequest): Promise<ProductFeedbackResponse> {
  const { data } = await apiClient.post('/feedbacks', req)
  return data
}

export async function deleteFeedback(id: number): Promise<void> {
  await apiClient.delete(`/feedbacks/${id}`)
}

export async function generateSummary(req: GenerateFeedbackSummaryRequest): Promise<FeedbackSummaryResponse> {
  const { data } = await apiClient.post('/feedbacks/summaries/generate', req)
  return data
}

export async function getSummaries(params: {
  recipeId: number
  page?: number
  size?: number
}): Promise<Page<FeedbackSummaryResponse>> {
  const { data } = await apiClient.get('/feedbacks/summaries', { params })
  return data
}

export async function getSummary(id: number): Promise<FeedbackSummaryResponse> {
  const { data } = await apiClient.get(`/feedbacks/summaries/${id}`)
  return data
}

export async function getTrend(recipeId: number): Promise<FeedbackSummaryResponse[]> {
  const { data } = await apiClient.get('/feedbacks/summaries/trend', { params: { recipeId } })
  return data
}
