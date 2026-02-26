import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getCategories,
  getArticles,
  getArticle,
  searchArticles,
  createArticle,
  updateArticle,
  deleteArticle,
} from '../api/knowledge'
import type { CreateKnowledgeArticleRequest, UpdateKnowledgeArticleRequest } from '../types'

export function useCategories() {
  return useQuery({
    queryKey: ['knowledge', 'categories'],
    queryFn: getCategories,
  })
}

export function useArticles(params?: { categoryId?: number; page?: number; size?: number }) {
  return useQuery({
    queryKey: ['knowledge', 'articles', params],
    queryFn: () => getArticles(params),
  })
}

export function useArticle(id: number) {
  return useQuery({
    queryKey: ['knowledge', 'articles', id],
    queryFn: () => getArticle(id),
    enabled: id > 0,
  })
}

export function useSearchArticles(keyword: string) {
  return useQuery({
    queryKey: ['knowledge', 'search', keyword],
    queryFn: () => searchArticles(keyword),
    enabled: keyword.length > 0,
  })
}

export function useCreateArticle() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateKnowledgeArticleRequest) => createArticle(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge'] })
    },
  })
}

export function useUpdateArticle(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateKnowledgeArticleRequest) => updateArticle(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge'] })
    },
  })
}

export function useDeleteArticle() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteArticle(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge'] })
    },
  })
}
