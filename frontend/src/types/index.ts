// ===== Enums =====

export type Role = 'CHEF' | 'SERVICE' | 'PURCHASER' | 'PRODUCER'

export type RecipeStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'DELETED'

export type SenderType = 'USER' | 'AI'

export type CollectionMethod = 'SURVEY' | 'INTERVIEW' | 'SNS' | 'DIRECT' | 'OTHER'

export type SupplyStatus = 'AVAILABLE' | 'LIMITED' | 'UNAVAILABLE' | 'SEASONAL'

// ===== User =====

export interface UserResponse {
  id: number
  email: string
  name: string
  pictureUrl: string | null
  role: Role
  enabled: boolean
  createdAt: string
  updatedAt: string
}

// ===== Recipe =====

export interface CookingStepInfo {
  id: number
  stepNumber: number
  description: string
  durationMinutes: number | null
  temperature: string | null
  tips: string | null
}

export interface IngredientInfo {
  id: number
  ingredientId: number
  ingredientName: string
  quantity: number | null
  unit: string | null
  preparationNote: string | null
  substitutes: string | null
}

export interface ServiceDesignInfo {
  id: number
  platingInstructions: string | null
  serviceMethod: string | null
  customerScript: string | null
  stagingMethod: string | null
  timing: string | null
  storytelling: string | null
}

export interface ExperienceDesignInfo {
  id: number
  targetScene: string | null
  emotionalKeyPoints: string | null
  specialOccasionSupport: string | null
  seasonalPresentation: string | null
  sensoryAppeal: string | null
}

export interface CreatedByInfo {
  id: number
  name: string
  role: Role
}

export interface RecipeResponse {
  id: number
  title: string
  description: string | null
  category: string | null
  servings: number | null
  status: RecipeStatus
  concept: string | null
  story: string | null
  createdBy: CreatedByInfo
  cookingSteps: CookingStepInfo[]
  ingredients: IngredientInfo[]
  serviceDesign: ServiceDesignInfo | null
  experienceDesign: ExperienceDesignInfo | null
  createdAt: string
  updatedAt: string
}

export interface RecipeListResponse {
  id: number
  title: string
  description: string | null
  category: string | null
  servings: number | null
  status: RecipeStatus
  createdByName: string
  createdAt: string
  updatedAt: string
}

export interface RecipeHistoryResponse {
  id: number
  changeType: string
  changedFields: string | null
  changedByName: string
  changedAt: string
}

// ===== Knowledge =====

export interface KnowledgeCategoryResponse {
  id: number
  name: string
  description: string | null
  sortOrder: number
}

export interface RelatedRecipeInfo {
  id: number
  title: string
}

export interface KnowledgeArticleResponse {
  id: number
  title: string
  content: string
  categoryName: string
  categoryId: number
  tags: string | null
  authorName: string
  authorId: number
  relatedRecipes: RelatedRecipeInfo[]
  createdAt: string
  updatedAt: string
}

// ===== AI Consultation =====

export interface AiThreadResponse {
  id: number
  theme: string
  recipeId: number | null
  recipeName: string | null
  userName: string
  createdAt: string
  updatedAt: string
}

export interface ReferencedArticleInfo {
  id: number
  title: string
}

export interface AiMessageResponse {
  id: number
  senderType: SenderType
  content: string
  referencedArticles: ReferencedArticleInfo[] | null
  createdAt: string
}

// ===== Feedback =====

export interface ProductFeedbackResponse {
  id: number
  recipeId: number
  recipeTitle: string
  storeId: number | null
  storeName: string | null
  periodStart: string
  periodEnd: string
  satisfactionScore: number
  emotionScore: number | null
  comment: string | null
  collectionMethod: CollectionMethod
  registeredByName: string
  createdAt: string
}

export interface FeedbackSummaryResponse {
  id: number
  recipeId: number
  recipeTitle: string
  periodStart: string
  periodEnd: string
  avgSatisfaction: string
  avgEmotion: string | null
  feedbackCount: number
  mainCommentTrend: string | null
  createdAt: string
  updatedAt: string
}

// ===== Pagination =====

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

// ===== Request Types =====

export interface CreateRecipeRequest {
  title: string
  description?: string
  category?: string
  servings?: number
  concept?: string
  story?: string
  cookingSteps?: {
    stepNumber: number
    description: string
    durationMinutes?: number
    temperature?: string
    tips?: string
  }[]
  ingredients?: {
    ingredientId: number
    quantity?: number
    unit?: string
    preparationNote?: string
    substitutes?: string
  }[]
}

export interface UpdateRecipeRequest {
  title?: string
  description?: string
  category?: string
  servings?: number
  concept?: string
  story?: string
}

export interface UpdateServiceDesignRequest {
  platingInstructions?: string
  serviceMethod?: string
  customerScript?: string
  stagingMethod?: string
  timing?: string
  storytelling?: string
}

export interface UpdateExperienceDesignRequest {
  targetScene?: string
  emotionalKeyPoints?: string
  specialOccasionSupport?: string
  seasonalPresentation?: string
  sensoryAppeal?: string
}

export interface UpdateStatusRequest {
  status: RecipeStatus
}

export interface CreateKnowledgeArticleRequest {
  title: string
  content: string
  categoryId: number
  tags?: string
  relatedRecipeIds?: number[]
}

export interface UpdateKnowledgeArticleRequest {
  title?: string
  content?: string
  categoryId?: number
  tags?: string
  relatedRecipeIds?: number[]
}

export interface CreateAiThreadRequest {
  theme: string
  recipeId?: number
  initialMessage: string
}

export interface SendAiMessageRequest {
  message: string
}

export interface CreateProductFeedbackRequest {
  recipeId: number
  storeId?: number
  periodStart: string
  periodEnd: string
  satisfactionScore: number
  emotionScore?: number
  comment?: string
  collectionMethod: CollectionMethod
}

export interface GenerateFeedbackSummaryRequest {
  recipeId: number
  periodStart: string
  periodEnd: string
}

export interface UpdateRoleRequest {
  role: Role
}
