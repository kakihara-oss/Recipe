import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getStores, getStore, createStore, updateStore } from '../api/stores'
import type { CreateStoreRequest, UpdateStoreRequest } from '../types'

export function useStores() {
  return useQuery({
    queryKey: ['stores'],
    queryFn: getStores,
  })
}

export function useStore(id: number) {
  return useQuery({
    queryKey: ['stores', id],
    queryFn: () => getStore(id),
    enabled: id > 0,
  })
}

export function useCreateStore() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateStoreRequest) => createStore(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stores'] })
    },
  })
}

export function useUpdateStore(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: UpdateStoreRequest) => updateStore(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stores'] })
    },
  })
}
