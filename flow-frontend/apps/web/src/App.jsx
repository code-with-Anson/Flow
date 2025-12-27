import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

import Explore from './pages/Explore';
import AiChatPage from './pages/ai/AiChatPage';
import KnowledgeBasePage from './pages/ai/KnowledgeBasePage';
import AiProviderSettings from './pages/ai/AiProviderSettings';

const PrivateRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  if (!user) {
    return <Navigate to="/login" />;
  }
  
  return children;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={
            <PrivateRoute>
              <Home />
            </PrivateRoute>
          } />

          <Route path="/explore" element={
            <PrivateRoute>
              <Explore />
            </PrivateRoute>
          } />
          <Route path="/ai" element={
            <PrivateRoute>
              <AiChatPage />
            </PrivateRoute>
          } />
          <Route path="/ai/settings" element={
            <PrivateRoute>
              <AiProviderSettings />
            </PrivateRoute>
          } />
          <Route path="/ai/knowledge" element={
            <PrivateRoute>
              <KnowledgeBasePage />
            </PrivateRoute>
          } />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
