import apiClient from './client'
import type { FileUploadResponse } from '../types'

export async function uploadFile(
  file: File,
  target: string,
  targetId: number,
): Promise<FileUploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('target', target)
  formData.append('targetId', targetId.toString())
  const { data } = await apiClient.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function deleteFile(target: string, targetId: number): Promise<void> {
  await apiClient.delete('/files', { params: { target, targetId } })
}
