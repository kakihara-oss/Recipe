import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getFeedbacks,
  createFeedback,
  deleteFeedback,
  generateSummary,
  getSummaries,
  getTrend,
} from '../api/feedbacks'
import type { CreateProductFeedbackRequest, GenerateFeedbackSummaryRequest } from '../types'

export function useFeedbacks(params?: { recipeId?: number; storeId?: number; page?: number; size?: number }) {
  return useQuery({
    queryKey: ['feedbacks', params],
    queryFn: () => getFeedbacks(params),
  })
}

export function useCreateFeedback() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateProductFeedbackRequest) => createFeedback(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feedbacks'] })
    },
  })
}

export function useDeleteFeedback() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteFeedback(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feedbacks'] })
    },
  })
}

export function useGenerateSummary() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: GenerateFeedbackSummaryRequest) => generateSummary(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feedbacks', 'summaries'] })
    },
  })
}

export function useSummaries(recipeId: number, params?: { page?: number; size?: number }) {
  return useQuery({
    queryKey: ['feedbacks', 'summaries', recipeId, params],
    queryFn: () => getSummaries({ recipeId, ...params }),
    enabled: recipeId > 0,
  })
}

export function useTrend(recipeId: number) {
  return useQuery({
    queryKey: ['feedbacks', 'trend', recipeId],
    queryFn: () => getTrend(recipeId),
    enabled: recipeId > 0,
  })
}
