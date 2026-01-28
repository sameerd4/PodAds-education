import { Routes, Route, Navigate } from 'react-router-dom';
import LivePage from './pages/LivePage';
import ScenariosPage from './pages/ScenariosPage';
import TracePage from './pages/TracePage';
import DashboardPage from './pages/DashboardPage';
import Layout from './components/Layout';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/live" replace />} />
        <Route path="/live" element={<LivePage />} />
        <Route path="/scenarios" element={<ScenariosPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/trace/:decisionId" element={<TracePage />} />
      </Routes>
    </Layout>
  );
}

export default App;


