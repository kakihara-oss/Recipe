import apiClient from './client'
import type {
  MonthlySalesResponse,
  StoreMonthlyFoodCostResponse,
  CsvUploadResponse,
} from '../types'

export async function uploadPosCsv(file: File): Promise<CsvUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await apiClient.post('/sales/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function getMonthlySales(storeId: number, targetMonth: string): Promise<MonthlySalesResponse[]> {
  const { data } = await apiClient.get(`/sales/stores/${storeId}/monthly`, { params: { targetMonth } })
  return data
}

export async function getSalesByRange(storeId: number, startMonth: string, endMonth: string): Promise<MonthlySalesResponse[]> {
  const { data } = await apiClient.get(`/sales/stores/${storeId}/range`, { params: { startMonth, endMonth } })
  return data
}

export async function calculateFoodCost(storeId: number, targetMonth: string): Promise<StoreMonthlyFoodCostResponse> {
  const { data } = await apiClient.post(`/sales/stores/${storeId}/food-cost/calculate`, null, { params: { targetMonth } })
  return data
}

export async function getStoreComparison(targetMonth: string): Promise<StoreMonthlyFoodCostResponse[]> {
  const { data } = await apiClient.get('/sales/food-cost/comparison', { params: { targetMonth } })
  return data
}

export async function getStoreTrend(storeId: number, startMonth: string, endMonth: string): Promise<StoreMonthlyFoodCostResponse[]> {
  const { data } = await apiClient.get(`/sales/stores/${storeId}/food-cost/trend`, { params: { startMonth, endMonth } })
  return data
}
