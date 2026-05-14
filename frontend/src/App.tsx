import { Navigate, Route, Routes } from 'react-router-dom';
import { DashboardLayout } from './components/layout/DashboardLayout';
import { LoginPage } from './pages/auth/LoginPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { useAuthStore } from './stores/auth';
import { DashboardStats, UsersPage, AdminMediaPage, ApprovalsPage, ReportsPage } from './pages/admin/AdminPages';
import { TeacherDashboard, UploadMediaPage, EvaluationsPage, StudentsListPage, StudentDetailPage } from './pages/teacher/TeacherPages';
import { StudentHomePage, VideosPage, GalleryPage, StudentEvaluationsPage, ChatAIPage } from './pages/student/StudentPages';

function Private() { return useAuthStore(s => s.accessToken) ? <DashboardLayout /> : <Navigate to="/login" replace />; }
export function App() {
  return <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
    <Route element={<Private />}>
      <Route path="/admin" element={<DashboardStats />} /><Route path="/admin/users" element={<UsersPage />} /><Route path="/admin/media" element={<AdminMediaPage />} /><Route path="/admin/approvals" element={<ApprovalsPage />} /><Route path="/admin/reports" element={<ReportsPage />} />
      <Route path="/teacher" element={<TeacherDashboard />} /><Route path="/teacher/upload" element={<UploadMediaPage />} /><Route path="/teacher/evaluations" element={<EvaluationsPage />} /><Route path="/teacher/students" element={<StudentsListPage />} /><Route path="/teacher/students/:id" element={<StudentDetailPage />} />
      <Route path="/student" element={<StudentHomePage />} /><Route path="/student/videos" element={<VideosPage />} /><Route path="/student/gallery" element={<GalleryPage />} /><Route path="/student/evaluations" element={<StudentEvaluationsPage />} /><Route path="/student/chat" element={<ChatAIPage />} />
    </Route>
    <Route path="*" element={<Navigate to="/login" replace />} />
  </Routes>;
}
