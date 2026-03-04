import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getIngredients,
  getIngredient,
  createIngredient,
  updateIngredient,
  updateSupplyStatus,
  addPrice,
  getPriceHistory,
  updateSeasons,
  getSeasons,
  getAffectedRecipes,
} from '../api/ingredients'
import type {
  CreateIngredientRequest,
  UpdateIngredientRequest,
  CreateIngredientPriceRequest,
  UpdateIngredientSeasonRequest,
  SupplyStatus,
} from '../types'

export function useIngredients(params?: {
  category?: string
  supplyStatus?: SupplyStatus
  keyword?: string
  page?: number
  size?: number
}) {
  return useQuery({
    queryKey: ['ingredients', params],
    queryFn: () => getIngredients(params),
  })
}

export function useIngredient(id: number) {
  return useQuery({
    queryKey: ['ingredients', id],
    queryFn: () => getIngredient(id),
    enabled: id > 0,
  })
}

export function useCreateIngredient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateIngredientRequest) => createIngredient(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] })
    },
  })
}

export function useUpdateIngredient(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateIngredientRequest) => updateIngredient(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] })
    },
  })
}

export function useUpdateSupplyStatus(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (status: SupplyStatus) => updateSupplyStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] })
    },
  })
}

export function useAddPrice(ingredientId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateIngredientPriceRequest) => addPrice(ingredientId, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] })
    },
  })
}

export function usePriceHistory(ingredientId: number) {
  return useQuery({
    queryKey: ['ingredients', ingredientId, 'prices'],
    queryFn: () => getPriceHistory(ingredientId),
    enabled: ingredientId > 0,
  })
}

export function useUpdateSeasons(ingredientId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (seasons: UpdateIngredientSeasonRequest[]) => updateSeasons(ingredientId, seasons),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ingredients'] })
    },
  })
}

export function useSeasons(ingredientId: number) {
  return useQuery({
    queryKey: ['ingredients', ingredientId, 'seasons'],
    queryFn: () => getSeasons(ingredientId),
    enabled: ingredientId > 0,
  })
}

export function useAffectedRecipes(ingredientId: number) {
  return useQuery({
    queryKey: ['ingredients', ingredientId, 'affected-recipes'],
    queryFn: () => getAffectedRecipes(ingredientId),
    enabled: ingredientId > 0,
  })
}
