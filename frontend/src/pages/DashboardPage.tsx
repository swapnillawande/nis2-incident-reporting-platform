import { useEffect, useState } from "react";
import { getDashboardSummary } from "../api/dashboardApi";
import { getApiErrorMessage } from "../api/errorUtils";
import { getCurrentUser } from "../api/userApi";
import type { DashboardSummary } from "../types/dashboard";
import type { UserResponse } from "../types/user";

function DashboardPage() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const [currentUser, dashboardSummary] = await Promise.all([
          getCurrentUser(),
          getDashboardSummary(),
        ]);

        setUser(currentUser);
        setSummary(dashboardSummary);
      } catch (error: unknown) {
        console.error("Failed to fetch dashboard:", error);

        const errorMessage = getApiErrorMessage(
          error,
          "Session expired or unauthorized. Please login again."
        );

        setError(errorMessage);
        localStorage.removeItem("user");
      }
    };

    fetchDashboard();
  }, []);

  const activeIncidents =
    (summary?.openIncidents ?? 0) + (summary?.inProgressIncidents ?? 0);

  return (
    <div className="page-container">
      <div className="dashboard-hero">
        <span className="badge">NIS2 Security Platform</span>

        <h1 style={{ marginTop: "18px" }}>Incident Reporting Dashboard</h1>

        <p className="page-subtitle">
          Monitor security events, manage incidents, track compliance workflows,
          and generate audit-ready reports.
        </p>

        {error && <div className="message error">{error}</div>}

        {user ? (
          <div>
            <h3>Welcome, {user.fullName}</h3>
            <p className="text-muted">Email: {user.email}</p>
            <p className="text-muted">Role: {user.roles?.join(", ")}</p>
            <p className="text-muted">Status: {user.status}</p>
          </div>
        ) : !error ? (
          <p className="text-muted">Loading user session...</p>
        ) : null}
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <span>Total Users</span>
          <strong>{summary ? summary.totalUsers : "--"}</strong>
        </div>

        <div className="dashboard-card">
          <span>Total Incidents</span>
          <strong>{summary ? summary.totalIncidents : "--"}</strong>
        </div>

        <div className="dashboard-card">
          <span>Open Incidents</span>
          <strong>{summary ? summary.openIncidents : "--"}</strong>
        </div>

        <div className="dashboard-card">
          <span>Active Work</span>
          <strong>{summary ? activeIncidents : "--"}</strong>
        </div>
      </div>
    </div>
  );
}

export default DashboardPage;
