import { useMutation, useQueryClient } from '@tanstack/react-query'
import { uploadFile, deleteFile } from '../api/files'

export function useUploadFile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ file, target, targetId }: { file: File; target: string; targetId: number }) =>
      uploadFile(file, target, targetId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
      if (variables.target === 'knowledgeArticle') {
        queryClient.invalidateQueries({ queryKey: ['articles'] })
      }
      if (variables.target === 'ingredient') {
        queryClient.invalidateQueries({ queryKey: ['ingredients'] })
      }
    },
  })
}

export function useDeleteFile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ target, targetId }: { target: string; targetId: number }) =>
      deleteFile(target, targetId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] })
      if (variables.target === 'knowledgeArticle') {
        queryClient.invalidateQueries({ queryKey: ['articles'] })
      }
      if (variables.target === 'ingredient') {
        queryClient.invalidateQueries({ queryKey: ['ingredients'] })
      }
    },
  })
}
