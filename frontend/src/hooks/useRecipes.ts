import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getRecipes,
  getRecipe,
  createRecipe,
  updateRecipe,
  updateServiceDesign,
  updateExperienceDesign,
  updateRecipeStatus,
  deleteRecipe,
  getRecipeHistory,
} from '../api/recipes'
import type {
  CreateRecipeRequest,
  UpdateRecipeRequest,
  UpdateServiceDesignRequest,
  UpdateExperienceDesignRequest,
  UpdateStatusRequest,
} from '../types'

export function useRecipes(params?: {
  category?: string
  status?: string
  page?: number
  size?: number
}) {
  return useQuery({
    queryKey: ['recipes', params],
    queryFn: () => getRecipes(params),
  })
}

export function useRecipe(id: number) {
  return useQuery({
    queryKey: ['recipes', id],
    queryFn: () => getRecipe(id),
    enabled: id > 0,
  })
}

export function useRecipeHistory(id: number) {
  return useQuery({
    queryKey: ['recipes', id, 'history'],
    queryFn: () => getRecipeHistory(id),
    enabled: id > 0,
  })
}

export function useCreateRecipe() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateRecipeRequest) => createRecipe(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
    },
  })
}

export function useUpdateRecipe(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateRecipeRequest) => updateRecipe(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
    },
  })
}

export function useUpdateServiceDesign(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateServiceDesignRequest) => updateServiceDesign(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes', id] })
    },
  })
}

export function useUpdateExperienceDesign(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateExperienceDesignRequest) => updateExperienceDesign(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes', id] })
    },
  })
}

export function useUpdateRecipeStatus(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateStatusRequest) => updateRecipeStatus(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
    },
  })
}

export function useDeleteRecipe() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => deleteRecipe(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
    },
  })
}
