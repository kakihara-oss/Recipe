import type { Role } from '../types'

export function canCreateRecipe(role: Role): boolean {
  return role === 'CHEF' || role === 'PRODUCER'
}

export function canEditRecipe(role: Role): boolean {
  return role === 'CHEF' || role === 'PRODUCER'
}

export function canEditServiceDesign(role: Role): boolean {
  return role === 'CHEF' || role === 'SERVICE' || role === 'PRODUCER'
}

export function canEditExperienceDesign(role: Role): boolean {
  return role === 'CHEF' || role === 'SERVICE' || role === 'PRODUCER'
}

export function canChangeRecipeStatus(role: Role): boolean {
  return role === 'CHEF' || role === 'PRODUCER'
}

export function canDeleteRecipe(role: Role): boolean {
  return role === 'CHEF' || role === 'PRODUCER'
}

export function canCreateFeedback(role: Role): boolean {
  return role === 'CHEF' || role === 'SERVICE' || role === 'PRODUCER'
}

export function canManageUsers(role: Role): boolean {
  return role === 'PRODUCER'
}
