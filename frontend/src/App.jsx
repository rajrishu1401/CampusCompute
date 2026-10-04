import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';

// Pages
import Landing from './pages/Landing';
import Login from './pages/Login';
import RegisterOrg from './pages/RegisterOrg';
import FirstTimeSetup from './pages/FirstTimeSetup';

// Admin Pages
import AdminDashboard from './pages/admin/Dashboard';
import AdminDevices from './pages/admin/Devices';
import AdminStudents from './pages/admin/Students';

// Student Pages
import StudentDashboard from './pages/student/Dashboard';
import StudentContainers from './pages/student/Containers';
import StudentTerminal from './pages/student/Terminal';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

// Protected route component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

function App() {
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Landing />} />
          <Route 
            path="/login" 
            element={isAuthenticated ? <Navigate to={user?.role === 'ORG_ADMIN' ? '/admin' : '/student'} replace /> : <Login />} 
          />
          <Route path="/register" element={<RegisterOrg />} />
          <Route path="/first-time-setup" element={<FirstTimeSetup />} />

          {/* Admin Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ORG_ADMIN', 'ROOT']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/devices"
            element={
              <ProtectedRoute allowedRoles={['ORG_ADMIN', 'ROOT']}>
                <AdminDevices />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/students"
            element={
              <ProtectedRoute allowedRoles={['ORG_ADMIN', 'ROOT']}>
                <AdminStudents />
              </ProtectedRoute>
            }
          />

          {/* Student Routes */}
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/containers"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentContainers />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/terminal/:containerId"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentTerminal />
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;
