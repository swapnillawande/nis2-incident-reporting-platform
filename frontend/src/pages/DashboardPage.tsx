function DashboardPage() {
  const userData = localStorage.getItem("user");
  const user = userData ? JSON.parse(userData) : null;

  return (
    <div className="page-container">
      <div className="dashboard-hero">
        <span className="badge">NIS2 Security Platform</span>

        <h1 style={{ marginTop: "18px" }}>Incident Reporting Dashboard</h1>

        <p className="page-subtitle">
          Monitor security events, manage incidents, track compliance workflows,
          and generate audit-ready reports.
        </p>

        {user ? (
          <div>
            <h3>Welcome, {user.fullName}</h3>
            <p className="text-muted">Email: {user.email}</p>
            <p className="text-muted">Role: {user.roles?.join(", ")}</p>
          </div>
        ) : (
          <p className="text-muted">No user logged in yet.</p>
        )}
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