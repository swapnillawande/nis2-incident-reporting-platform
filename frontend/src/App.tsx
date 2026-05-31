import { BrowserRouter, Link, NavLink, Route, Routes, useNavigate } from "react-router-dom";
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
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    [
      "rounded-full px-4 py-2 text-sm font-extrabold transition-all duration-200",
      isActive
        ? "bg-blue-600 text-white shadow-lg shadow-blue-500/25"
        : "text-slate-600 hover:bg-blue-50 hover:text-blue-700",
    ].join(" ");

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="sticky top-0 z-30 border-b border-blue-100/70 bg-white/85 px-5 py-3 shadow-sm shadow-blue-950/5 backdrop-blur-xl md:px-10">
      <div className="mx-auto flex w-full max-w-7xl flex-wrap items-center gap-3">
        <Link
          to="/"
          className="mr-2 flex items-center gap-2 rounded-full bg-slate-950 px-4 py-2 text-sm font-extrabold text-white shadow-lg shadow-slate-950/15"
        >
          <span className="h-2.5 w-2.5 rounded-full bg-emerald-400 shadow-[0_0_14px_rgba(52,211,153,0.9)]" />
          NISync
        </Link>

        <div className="flex flex-wrap items-center gap-2">
          <NavLink to="/" className={navLinkClass}>
            Dashboard
          </NavLink>
          {user && (
            <NavLink to="/incidents" className={navLinkClass}>
              Incidents
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/users" className={navLinkClass}>
              Users
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/audit-logs" className={navLinkClass}>
              Audit Logs
            </NavLink>
          )}

          {!user && (
            <NavLink to="/login" className={navLinkClass}>
              Login
            </NavLink>
          )}
          {!user && (
            <NavLink to="/register" className={navLinkClass}>
              Register
            </NavLink>
          )}
        </div>

        {user && (
          <div className="ml-auto flex flex-wrap items-center gap-3">
            <span className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-bold text-emerald-800">
              {user.fullName} | {user.roles?.join(", ")}
            </span>

            <button
              className="!m-0 !min-h-0 !w-auto rounded-full border border-rose-200 !bg-rose-50 px-4 py-2 text-sm font-extrabold !text-rose-700 !shadow-none transition hover:!bg-rose-100"
              onClick={handleLogout}
            >
              Logout
            </button>
          </div>
        )}
      </div>
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
