import apiClient from './client'
import type {
  AiThreadResponse,
  AiMessageResponse,
  CreateAiThreadRequest,
  SendAiMessageRequest,
  Page,
} from '../types'

export async function getThreads(params?: {
  page?: number
  size?: number
}): Promise<Page<AiThreadResponse>> {
  const { data } = await apiClient.get('/ai/threads', { params })
  return data
}

export async function getThread(threadId: number): Promise<AiThreadResponse> {
  const { data } = await apiClient.get(`/ai/threads/${threadId}`)
  return data
}

export async function createThread(req: CreateAiThreadRequest): Promise<AiThreadResponse> {
  const { data } = await apiClient.post('/ai/threads', req)
  return data
}

export async function getMessages(threadId: number): Promise<AiMessageResponse[]> {
  const { data } = await apiClient.get(`/ai/threads/${threadId}/messages`)
  return data
}

export async function sendMessage(threadId: number, req: SendAiMessageRequest): Promise<AiMessageResponse> {
  const { data } = await apiClient.post(`/ai/threads/${threadId}/messages`, req)
  return data
}
