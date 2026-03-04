import { useMutation } from '@tanstack/react-query'
import {
  generateRecipeFromTheme,
  generateRecipeFromExisting,
  improveField,
  modifyRecipe,
} from '../api/recipeAi'
import type {
  AiGenerateRecipeRequest,
  AiGenerateFromRecipeRequest,
  AiImproveFieldRequest,
  AiModifyRecipeRequest,
} from '../types'

export function useGenerateFromTheme() {
  return useMutation({
    mutationFn: (req: AiGenerateRecipeRequest) => generateRecipeFromTheme(req),
  })
}

export function useGenerateFromRecipe() {
  return useMutation({
    mutationFn: (req: AiGenerateFromRecipeRequest) => generateRecipeFromExisting(req),
  })
}

export function useImproveField() {
  return useMutation({
    mutationFn: (req: AiImproveFieldRequest) => improveField(req),
  })
}

export function useModifyRecipe() {
  return useMutation({
    mutationFn: (req: AiModifyRecipeRequest) => modifyRecipe(req),
  })
}
