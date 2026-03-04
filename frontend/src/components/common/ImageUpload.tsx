import { useRef, useState } from 'react'
import { useUploadFile, useDeleteFile } from '../../hooks/useFileUpload'

interface ImageUploadProps {
  target: string
  targetId: number
  currentImageUrl: string | null | undefined
  onSuccess?: (url: string) => void
  onDelete?: () => void
  label?: string
}

export default function ImageUpload({ target, targetId, currentImageUrl, onSuccess, onDelete, label }: ImageUploadProps) {
  const fileInput = useRef<HTMLInputElement>(null)
  const uploadMutation = useUploadFile()
  const deleteMutation = useDeleteFile()
  const [preview, setPreview] = useState<string | null>(null)

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const objectUrl = URL.createObjectURL(file)
    setPreview(objectUrl)

    uploadMutation.mutate(
      { file, target, targetId },
      {
        onSuccess: (res) => {
          URL.revokeObjectURL(objectUrl)
          setPreview(null)
          onSuccess?.(res.url)
        },
        onError: () => {
          URL.revokeObjectURL(objectUrl)
          setPreview(null)
        },
      },
    )
  }

  const handleDelete = () => {
    deleteMutation.mutate(
      { target, targetId },
      { onSuccess: () => onDelete?.() },
    )
  }

  const displayUrl = preview || currentImageUrl

  return (
    <div className="space-y-2">
      {label && <label className="block text-sm font-medium text-gray-700">{label}</label>}
      {displayUrl ? (
        <div className="relative inline-block">
          <img
            src={displayUrl}
            alt="uploaded"
            className="h-40 w-40 rounded-lg border border-gray-200 object-cover"
          />
          {!preview && currentImageUrl && (
            <button
              type="button"
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-red-500 text-xs text-white hover:bg-red-600 disabled:opacity-50"
            >
              x
            </button>
          )}
          {(uploadMutation.isPending || preview) && (
            <div className="absolute inset-0 flex items-center justify-center rounded-lg bg-black/40">
              <span className="text-sm text-white">アップロード中...</span>
            </div>
          )}
        </div>
      ) : (
        <button
          type="button"
          onClick={() => fileInput.current?.click()}
          disabled={uploadMutation.isPending}
          className="flex h-40 w-40 items-center justify-center rounded-lg border-2 border-dashed border-gray-300 text-gray-400 hover:border-blue-400 hover:text-blue-500 disabled:opacity-50"
        >
          <span className="text-center text-sm">
            写真を
            <br />
            アップロード
          </span>
        </button>
      )}
      <input
        ref={fileInput}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        onChange={handleFileSelect}
        className="hidden"
      />
      {currentImageUrl && !preview && (
        <button
          type="button"
          onClick={() => fileInput.current?.click()}
          className="text-xs text-blue-600 hover:underline"
        >
          写真を変更
        </button>
      )}
      {uploadMutation.isError && (
        <p className="text-xs text-red-500">アップロードに失敗しました</p>
      )}
    </div>
  )
}
