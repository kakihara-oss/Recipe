import apiClient from './client'
import type {
  KnowledgeCategoryResponse,
  KnowledgeArticleResponse,
  CreateKnowledgeArticleRequest,
  UpdateKnowledgeArticleRequest,
  Page,
} from '../types'

export async function getCategories(): Promise<KnowledgeCategoryResponse[]> {
  const { data } = await apiClient.get('/knowledge/categories')
  return data
}

export async function getArticles(params?: {
  categoryId?: number
  page?: number
  size?: number
}): Promise<Page<KnowledgeArticleResponse>> {
  const { data } = await apiClient.get('/knowledge/articles', { params })
  return data
}

export async function getArticle(id: number): Promise<KnowledgeArticleResponse> {
  const { data } = await apiClient.get(`/knowledge/articles/${id}`)
  return data
}

export async function searchArticles(keyword: string): Promise<KnowledgeArticleResponse[]> {
  const { data } = await apiClient.get('/knowledge/articles/search', { params: { keyword } })
  return data
}

export async function createArticle(req: CreateKnowledgeArticleRequest): Promise<KnowledgeArticleResponse> {
  const { data } = await apiClient.post('/knowledge/articles', req)
  return data
}

export async function updateArticle(id: number, req: UpdateKnowledgeArticleRequest): Promise<KnowledgeArticleResponse> {
  const { data } = await apiClient.put(`/knowledge/articles/${id}`, req)
  return data
}

export async function deleteArticle(id: number): Promise<void> {
  await apiClient.delete(`/knowledge/articles/${id}`)
}
