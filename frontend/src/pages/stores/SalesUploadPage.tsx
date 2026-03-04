import { useState, useRef } from 'react'
import { useUploadPosCsv } from '../../hooks/useSales'

export default function SalesUploadPage() {
  const uploadMutation = useUploadPosCsv()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)

  const handleUpload = () => {
    if (!selectedFile) return
    uploadMutation.mutate(selectedFile)
  }

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-800">POSデータアップロード</h2>

      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <h3 className="mb-4 text-lg font-semibold text-gray-700">CSVファイルをアップロード</h3>

        <div className="mb-4 rounded-lg bg-gray-50 p-4 text-sm text-gray-600">
          <p className="mb-2 font-medium">CSVフォーマット:</p>
          <code className="block rounded bg-gray-100 px-3 py-2 text-xs">
            店舗コード, レシピID, 対象年月(YYYY-MM), 出数, 売上金額
          </code>
          <p className="mt-2">例: <code className="text-xs">STORE001,1,2026-01,10,50000</code></p>
          <p className="mt-1 text-xs text-gray-500">
            同一店舗・同一年月の再アップロードは既存データを上書きします。
          </p>
        </div>

        <div className="mb-4 flex items-center gap-3">
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv"
            onChange={(e) => setSelectedFile(e.target.files?.[0] ?? null)}
            className="text-sm text-gray-600"
          />
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploadMutation.isPending}
            className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {uploadMutation.isPending ? 'アップロード中...' : 'アップロード'}
          </button>
        </div>

        {uploadMutation.data && (
          <div className="rounded-lg border p-4">
            <h4 className="mb-2 text-sm font-semibold text-gray-700">アップロード結果</h4>
            <dl className="mb-3 grid grid-cols-2 gap-2 text-sm">
              <div>
                <dt className="text-gray-500">取込件数</dt>
                <dd className="text-lg font-medium text-green-600">{uploadMutation.data.importedCount}件</dd>
              </div>
              <div>
                <dt className="text-gray-500">スキップ件数</dt>
                <dd className="text-lg font-medium text-yellow-600">{uploadMutation.data.skippedCount}件</dd>
              </div>
            </dl>
            {uploadMutation.data.errors.length > 0 && (
              <div>
                <h5 className="mb-1 text-sm font-medium text-red-600">エラー一覧:</h5>
                <ul className="max-h-40 overflow-y-auto text-xs text-red-500">
                  {uploadMutation.data.errors.map((err, i) => (
                    <li key={i} className="py-0.5">{err}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
