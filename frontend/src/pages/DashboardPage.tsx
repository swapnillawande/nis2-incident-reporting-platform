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
  const closedOrResolved =
    (summary?.resolvedIncidents ?? 0) + (summary?.closedIncidents ?? 0);
  const dashboardCards = [
    {
      label: "Total Users",
      value: summary?.totalUsers,
      tone: "default",
    },
    {
      label: "Total Incidents",
      value: summary?.totalIncidents,
      tone: "default",
    },
    {
      label: "Active Work",
      value: summary ? activeIncidents : undefined,
      tone: "info",
    },
    {
      label: "Overdue SLA",
      value: summary?.overdueIncidents,
      tone: "danger",
    },
    {
      label: "Due Within 24h",
      value: summary?.dueSoonIncidents,
      tone: "warning",
    },
    {
      label: "No SLA Set",
      value: summary?.unscheduledActiveIncidents,
      tone: "neutral",
    },
    {
      label: "Closed / Resolved",
      value: summary ? closedOrResolved : undefined,
      tone: "success",
    },
  ];

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
        {dashboardCards.map((card) => (
          <div className={`dashboard-card dashboard-card-${card.tone}`} key={card.label}>
            <span>{card.label}</span>
            <strong>{card.value ?? "--"}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}

export default DashboardPage;
