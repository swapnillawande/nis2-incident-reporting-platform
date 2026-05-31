import { useEffect, useState } from "react";
import { BrowserRouter, Link, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import IncidentsPage from "./pages/IncidentsPage";
import UsersPage from "./pages/UsersPage";
import AuditLogsPage from "./pages/AuditLogsPage";
import ProtectedRoute from "./components/ProtectedRoute";
import nis2Icon from "./assets/nis2-icon.png";

type ThemeMode = "light" | "dark";

interface NavbarProps {
  theme: ThemeMode;
  onToggleTheme: () => void;
}

function Navbar({ theme, onToggleTheme }: NavbarProps) {
  const navigate = useNavigate();
  const userData = localStorage.getItem("user");
  const user = userData ? JSON.parse(userData) : null;
  const isAdmin = user?.roles?.includes("ADMIN");
  const isDark = theme === "dark";
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    [
      "rounded-full px-4 py-2 text-sm font-extrabold transition-all duration-200",
      isActive
        ? "bg-cyan-500 text-slate-950 shadow-lg shadow-cyan-500/25"
        : "text-slate-600 hover:bg-cyan-50 hover:text-cyan-700 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-cyan-200",
    ].join(" ");

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <nav className="sticky top-0 z-30 border-b border-blue-100/70 bg-white/85 px-5 py-3 shadow-sm shadow-blue-950/5 backdrop-blur-xl dark:border-slate-700/70 dark:bg-slate-950/86 md:px-10">
      <div className="mx-auto flex w-full max-w-7xl flex-wrap items-center gap-3">
        <Link
          to="/"
          className="mr-2 flex items-center gap-2 rounded-full bg-slate-950 py-1.5 pl-1.5 pr-4 text-sm font-extrabold text-white shadow-lg shadow-slate-950/15 dark:bg-slate-900 dark:ring-1 dark:ring-cyan-400/30"
        >
          <img
            src={nis2Icon}
            alt="NIS2 logo"
            className="h-9 w-9 rounded-full bg-cyan-400/10 object-contain"
          />
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

        <button
          type="button"
          className="!m-0 !ml-auto !min-h-0 !w-auto rounded-full border border-slate-200 !bg-white px-2 py-1 !text-slate-700 !shadow-none transition hover:!bg-slate-50 dark:border-slate-700 dark:!bg-slate-900 dark:!text-slate-100 dark:hover:!bg-slate-800 md:!ml-2"
          onClick={onToggleTheme}
          aria-label={`Switch to ${isDark ? "light" : "dark"} mode`}
          title={`Switch to ${isDark ? "light" : "dark"} mode`}
        >
          <span className="flex items-center gap-2">
            <span
              className={`grid h-8 w-8 place-items-center rounded-full text-sm transition ${
                isDark ? "bg-cyan-400 text-slate-950" : "bg-amber-300 text-slate-950"
              }`}
            >
              {isDark ? "D" : "L"}
            </span>
            <span className="hidden pr-2 text-xs font-extrabold uppercase tracking-wide sm:inline">
              {isDark ? "Dark" : "Light"}
            </span>
          </span>
        </button>

        {user && (
          <div className="flex flex-wrap items-center gap-3">
            <span className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-bold text-emerald-800 dark:border-emerald-400/30 dark:bg-emerald-400/10 dark:text-emerald-200">
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
  const [theme, setTheme] = useState<ThemeMode>(() => {
    const savedTheme = localStorage.getItem("theme");
    return savedTheme === "dark" ? "dark" : "light";
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.classList.toggle("dark", theme === "dark");
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <BrowserRouter>
      <Navbar
        theme={theme}
        onToggleTheme={() => setTheme((currentTheme) => currentTheme === "dark" ? "light" : "dark")}
      />

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
