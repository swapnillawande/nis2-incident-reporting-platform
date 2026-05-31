import { BrowserRouter, Link, Route, Routes, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import IncidentsPage from "./pages/IncidentsPage";
import UsersPage from "./pages/UsersPage";
import AuditLogsPage from "./pages/AuditLogsPage";
import ProtectedRoute from "./components/ProtectedRoute";

function Navbar() {
  const navigate = useNavigate();
  const userData = localStorage.getItem("user");
  const user = userData ? JSON.parse(userData) : null;
  const isAdmin = user?.roles?.includes("ADMIN");

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="app-navbar">
      <Link to="/">Dashboard</Link>
      {user && <Link to="/incidents">Incidents</Link>}
      {isAdmin && <Link to="/users">Users</Link>}
      {isAdmin && <Link to="/audit-logs">Audit Logs</Link>}

      {!user && <Link to="/login">Login</Link>}
      {!user && <Link to="/register">Register</Link>}

      {user && (
        <>
          <span className="nav-user">
            {user.fullName} | {user.roles?.join(", ")}
          </span>

          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </>
      )}
    </nav>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Navbar />

      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/users"
          element={
            <ProtectedRoute>
              <UsersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/incidents"
          element={
            <ProtectedRoute>
              <IncidentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/audit-logs"
          element={
            <ProtectedRoute>
              <AuditLogsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
