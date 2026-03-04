import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  uploadPosCsv,
  getMonthlySales,
  getSalesByRange,
  calculateFoodCost,
  getStoreComparison,
  getStoreTrend,
} from '../api/sales'

export function useUploadPosCsv() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (file: File) => uploadPosCsv(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sales'] })
      queryClient.invalidateQueries({ queryKey: ['food-cost'] })
    },
  })
}

export function useMonthlySales(storeId: number, targetMonth: string) {
  return useQuery({
    queryKey: ['sales', storeId, targetMonth],
    queryFn: () => getMonthlySales(storeId, targetMonth),
    enabled: storeId > 0 && !!targetMonth,
  })
}

export function useSalesByRange(storeId: number, startMonth: string, endMonth: string) {
  return useQuery({
    queryKey: ['sales', storeId, startMonth, endMonth],
    queryFn: () => getSalesByRange(storeId, startMonth, endMonth),
    enabled: storeId > 0 && !!startMonth && !!endMonth,
  })
}

export function useCalculateFoodCost() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ storeId, targetMonth }: { storeId: number; targetMonth: string }) =>
      calculateFoodCost(storeId, targetMonth),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['food-cost'] })
    },
  })
}

export function useStoreComparison(targetMonth: string) {
  return useQuery({
    queryKey: ['food-cost', 'comparison', targetMonth],
    queryFn: () => getStoreComparison(targetMonth),
    enabled: !!targetMonth,
  })
}

export function useStoreTrend(storeId: number, startMonth: string, endMonth: string) {
  return useQuery({
    queryKey: ['food-cost', 'trend', storeId, startMonth, endMonth],
    queryFn: () => getStoreTrend(storeId, startMonth, endMonth),
    enabled: storeId > 0 && !!startMonth && !!endMonth,
  })
}
