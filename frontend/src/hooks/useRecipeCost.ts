import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getRecipeCost,
  calculateRecipeCost,
  updateRecipeCost,
  getWarningRecipes,
} from '../api/recipeCost'
import type { UpdateRecipeCostRequest } from '../types'

export function useRecipeCost(recipeId: number) {
  return useQuery({
    queryKey: ['recipeCost', recipeId],
    queryFn: () => getRecipeCost(recipeId),
    enabled: recipeId > 0,
    retry: false,
  })
}

export function useCalculateRecipeCost(recipeId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => calculateRecipeCost(recipeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipeCost', recipeId] })
    },
  })
}

export function useUpdateRecipeCost(recipeId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateRecipeCostRequest) => updateRecipeCost(recipeId, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipeCost', recipeId] })
    },
  })
}

export function useWarningRecipes() {
  return useQuery({
    queryKey: ['recipeCost', 'warnings'],
    queryFn: getWarningRecipes,
  })
}
