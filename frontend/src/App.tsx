import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/auth/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage'
import DashboardPage from './pages/DashboardPage'

// Recipe pages
import RecipeListPage from './pages/recipes/RecipeListPage'
import RecipeDetailPage from './pages/recipes/RecipeDetailPage'
import RecipeCreatePage from './pages/recipes/RecipeCreatePage'
import RecipeEditPage from './pages/recipes/RecipeEditPage'
import ServiceDesignEditPage from './pages/recipes/ServiceDesignEditPage'
import ExperienceDesignEditPage from './pages/recipes/ExperienceDesignEditPage'
import RecipeHistoryPage from './pages/recipes/RecipeHistoryPage'

// Knowledge pages
import KnowledgeListPage from './pages/knowledge/KnowledgeListPage'
import KnowledgeDetailPage from './pages/knowledge/KnowledgeDetailPage'
import KnowledgeSearchPage from './pages/knowledge/KnowledgeSearchPage'
import KnowledgeCreatePage from './pages/knowledge/KnowledgeCreatePage'
import KnowledgeEditPage from './pages/knowledge/KnowledgeEditPage'

// AI pages
import AiConsultationPage from './pages/ai/AiConsultationPage'
import AiNewThreadPage from './pages/ai/AiNewThreadPage'

// Feedback pages
import FeedbackListPage from './pages/feedback/FeedbackListPage'
import FeedbackCreatePage from './pages/feedback/FeedbackCreatePage'
import FeedbackSummaryPage from './pages/feedback/FeedbackSummaryPage'
import FeedbackTrendPage from './pages/feedback/FeedbackTrendPage'

// Admin pages
import UserManagementPage from './pages/admin/UserManagementPage'

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

        {/* Recipe routes */}
        <Route path="/recipes" element={<RecipeListPage />} />
        <Route path="/recipes/new" element={<RecipeCreatePage />} />
        <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        <Route path="/recipes/:id/edit" element={<RecipeEditPage />} />
        <Route path="/recipes/:id/service-design/edit" element={<ServiceDesignEditPage />} />
        <Route path="/recipes/:id/experience-design/edit" element={<ExperienceDesignEditPage />} />
        <Route path="/recipes/:id/history" element={<RecipeHistoryPage />} />

        {/* Knowledge routes */}
        <Route path="/knowledge" element={<KnowledgeListPage />} />
        <Route path="/knowledge/search" element={<KnowledgeSearchPage />} />
        <Route path="/knowledge/new" element={<KnowledgeCreatePage />} />
        <Route path="/knowledge/:id" element={<KnowledgeDetailPage />} />
        <Route path="/knowledge/:id/edit" element={<KnowledgeEditPage />} />

        {/* AI routes */}
        <Route path="/ai" element={<AiConsultationPage />} />
        <Route path="/ai/new" element={<AiNewThreadPage />} />
        <Route path="/ai/threads/:threadId" element={<AiConsultationPage />} />

        {/* Feedback routes */}
        <Route path="/feedbacks" element={<FeedbackListPage />} />
        <Route path="/feedbacks/new" element={<FeedbackCreatePage />} />
        <Route path="/feedbacks/summaries" element={<FeedbackSummaryPage />} />
        <Route path="/feedbacks/trend" element={<FeedbackTrendPage />} />

        {/* Admin routes */}
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute allowedRoles={['PRODUCER']}>
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
      </Route>
    </Routes>
  )
}
