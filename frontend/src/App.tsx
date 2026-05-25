import { BrowserRouter, Link, Route, Routes, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import ProtectedRoute from "./components/ProtectedRoute";

function Navbar() {
  const navigate = useNavigate();
  const userData = localStorage.getItem("user");
  const user = userData ? JSON.parse(userData) : null;

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="app-navbar">
      <Link to="/">Dashboard</Link>

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
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;