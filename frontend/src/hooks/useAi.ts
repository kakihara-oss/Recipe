import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getThreads, getThread, createThread, getMessages, sendMessage } from '../api/ai'
import type { CreateAiThreadRequest, SendAiMessageRequest } from '../types'

export function useThreads(params?: { page?: number; size?: number }) {
  return useQuery({
    queryKey: ['ai', 'threads', params],
    queryFn: () => getThreads(params),
  })
}

export function useThread(threadId: number) {
  return useQuery({
    queryKey: ['ai', 'threads', threadId],
    queryFn: () => getThread(threadId),
    enabled: threadId > 0,
  })
}

export function useMessages(threadId: number) {
  return useQuery({
    queryKey: ['ai', 'messages', threadId],
    queryFn: () => getMessages(threadId),
    enabled: threadId > 0,
    refetchInterval: 3000,
  })
}

export function useCreateThread() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateAiThreadRequest) => createThread(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ai', 'threads'] })
    },
  })
}

export function useSendMessage(threadId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: SendAiMessageRequest) => sendMessage(threadId, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ai', 'messages', threadId] })
    },
  })
}
