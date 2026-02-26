import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface DevTokenResponse {
  token: string;
  email: string;
  role: string;
}

export interface RecipeListItem {
  id: number;
  title: string;
  description: string;
  category: string;
  servings: number;
  status: string;
  createdByName: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CookingStepInfo {
  id: number;
  stepNumber: number;
  description: string;
  durationMinutes: number;
  temperature: string;
  tips: string;
}

export interface IngredientInfo {
  id: number;
  ingredientId: number;
  ingredientName: string;
  quantity: number;
  unit: string;
  preparationNote: string;
  substitutes: string;
}

export interface ServiceDesignInfo {
  id: number;
  platingInstructions: string;
  serviceMethod: string;
  customerScript: string;
  stagingMethod: string;
  timing: string;
  storytelling: string;
}

export interface ExperienceDesignInfo {
  id: number;
  targetScene: string;
  emotionalKeyPoints: string;
  specialOccasionSupport: string;
  seasonalPresentation: string;
  sensoryAppeal: string;
}

export interface RecipeDetail {
  id: number;
  title: string;
  description: string;
  category: string;
  servings: number;
  status: string;
  concept: string;
  story: string;
  createdBy: { id: number; name: string; role: string };
  cookingSteps: CookingStepInfo[];
  ingredients: IngredientInfo[];
  serviceDesign: ServiceDesignInfo | null;
  experienceDesign: ExperienceDesignInfo | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRecipePayload {
  title: string;
  description: string;
  category: string;
  servings: number;
  concept: string;
  story: string;
  cookingSteps: {
    stepNumber: number;
    description: string;
    durationMinutes?: number;
    temperature?: string;
    tips?: string;
  }[];
  ingredients: {
    ingredientId: number;
    quantity: number;
    unit: string;
    preparationNote?: string;
  }[];
}

export interface IngredientMaster {
  id: number;
  name: string;
  category: string;
  standardUnit: string;
  supplyStatus: string;
}

export default api;
