import { useEffect, useState } from "react";
import { getCurrentUser } from "../api/userApi";
import type { UserResponse } from "../types/user";

function DashboardPage() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const response = await getCurrentUser();
        setUser(response);
      } catch (error: any) {
        console.error("Failed to fetch current user:", error);

        const errorMessage =
          error.response?.data?.message ||
          error.response?.data?.error ||
          "Session expired or unauthorized. Please login again.";

        setError(errorMessage);
        localStorage.removeItem("user");
      }
    };

    fetchCurrentUser();
  }, []);

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
          <span>Total Assets</span>
          <strong>0</strong>
        </div>

        <div className="dashboard-card">
          <span>Open Alerts</span>
          <strong>0</strong>
        </div>

        <div className="dashboard-card">
          <span>Open Incidents</span>
          <strong>0</strong>
        </div>

        <div className="dashboard-card">
          <span>Pending Reports</span>
          <strong>0</strong>
        </div>
      </div>
    </div>
  );
}

export default DashboardPage;