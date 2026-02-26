import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/auth/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage'
import DashboardPage from './pages/DashboardPage'

export default function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

      {/* Protected routes */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />

        {/* Placeholder routes - will be implemented in later phases */}
        <Route path="/recipes" element={<Placeholder title="レシピ一覧" />} />
        <Route path="/recipes/new" element={<Placeholder title="レシピ作成" />} />
        <Route path="/recipes/:id" element={<Placeholder title="レシピ詳細" />} />
        <Route path="/recipes/:id/edit" element={<Placeholder title="レシピ編集" />} />
        <Route path="/recipes/:id/service-design/edit" element={<Placeholder title="サービス設計編集" />} />
        <Route path="/recipes/:id/experience-design/edit" element={<Placeholder title="体験設計編集" />} />
        <Route path="/recipes/:id/history" element={<Placeholder title="変更履歴" />} />

        <Route path="/knowledge" element={<Placeholder title="ナレッジベース" />} />
        <Route path="/knowledge/search" element={<Placeholder title="ナレッジ検索" />} />
        <Route path="/knowledge/new" element={<Placeholder title="ナレッジ記事作成" />} />
        <Route path="/knowledge/:id" element={<Placeholder title="ナレッジ記事詳細" />} />
        <Route path="/knowledge/:id/edit" element={<Placeholder title="ナレッジ記事編集" />} />

        <Route path="/ai" element={<Placeholder title="AI相談" />} />
        <Route path="/ai/new" element={<Placeholder title="新規AI相談" />} />
        <Route path="/ai/threads/:threadId" element={<Placeholder title="AI相談スレッド" />} />

        <Route path="/feedbacks" element={<Placeholder title="フィードバック一覧" />} />
        <Route path="/feedbacks/new" element={<Placeholder title="フィードバック登録" />} />
        <Route path="/feedbacks/summaries" element={<Placeholder title="フィードバックサマリー" />} />
        <Route path="/feedbacks/trend" element={<Placeholder title="フィードバックトレンド" />} />

        <Route
          path="/admin/users"
          element={
            <ProtectedRoute allowedRoles={['PRODUCER']}>
              <Placeholder title="メンバー管理" />
            </ProtectedRoute>
          }
        />
      </Route>
    </Routes>
  )
}

function Placeholder({ title }: { title: string }) {
  return (
    <div className="flex items-center justify-center rounded-lg border-2 border-dashed border-gray-300 p-12">
      <p className="text-lg text-gray-400">{title}（準備中）</p>
    </div>
  )
}
