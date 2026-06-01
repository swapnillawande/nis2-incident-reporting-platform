import { useEffect, useMemo, useState } from "react";
import type Highcharts from "highcharts";
import DashboardChart from "../components/DashboardChart";
import { getDashboardSummary, getRecentActiveIncidents } from "../api/dashboardApi";
import { getApiErrorMessage } from "../api/errorUtils";
import { getCurrentUser } from "../api/userApi";
import type { DashboardSummary } from "../types/dashboard";
import type { IncidentResponse } from "../types/incident";
import type { UserResponse } from "../types/user";

const formatDateTime = (dateTime?: string | null) => {
  if (!dateTime) {
    return "No SLA";
  }

  return new Date(dateTime).toLocaleString();
};

function DashboardPage() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [recentIncidents, setRecentIncidents] = useState<IncidentResponse[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const [currentUser, dashboardSummary, activeIncidents] = await Promise.all([
          getCurrentUser(),
          getDashboardSummary(),
          getRecentActiveIncidents(),
        ]);

        setUser(currentUser);
        setSummary(dashboardSummary);
        setRecentIncidents(activeIncidents);
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
  const chartTextColor = "#334155";
  const chartMutedColor = "#64748b";
  const chartGridColor = "rgba(148, 163, 184, 0.24)";
  const emptyChartLabel = summary ? undefined : "Loading dashboard data";
  const incidentTrendChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "areaspline", height: 300 },
    colors: ["#2563eb"],
    xAxis: {
      categories: summary?.incidentTrend?.map((point) =>
        new Date(`${point.date}T00:00:00`).toLocaleDateString(undefined, {
          month: "short",
          day: "numeric",
        })
      ) ?? [],
      labels: { style: { color: chartMutedColor, fontWeight: "700" } },
      lineColor: chartGridColor,
    },
    yAxis: {
      min: 0,
      allowDecimals: false,
      title: { text: "New incidents", style: { color: chartMutedColor } },
      gridLineColor: chartGridColor,
      labels: { style: { color: chartMutedColor } },
    },
    legend: { enabled: false },
    tooltip: { pointFormat: "<b>{point.y}</b> incidents created" },
    plotOptions: {
      areaspline: {
        marker: {
          enabled: true,
          radius: 4,
        },
        fillOpacity: 0.16,
      },
    },
    series: [
      {
        type: "areaspline",
        name: "New Incidents",
        data: summary?.incidentTrend?.map((point) => point.count) ?? [],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const incidentStatusChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "column", height: 300 },
    colors: ["#2563eb", "#f59e0b", "#10b981", "#64748b"],
    xAxis: {
      categories: ["Open", "In Progress", "Resolved", "Closed"],
      labels: { style: { color: chartMutedColor, fontWeight: "700" } },
      lineColor: chartGridColor,
    },
    yAxis: {
      min: 0,
      allowDecimals: false,
      title: { text: "Incidents", style: { color: chartMutedColor } },
      gridLineColor: chartGridColor,
      labels: { style: { color: chartMutedColor } },
    },
    legend: { enabled: false },
    tooltip: { pointFormat: "<b>{point.y}</b> incidents" },
    series: [
      {
        type: "column",
        name: "Incidents",
        borderRadius: 5,
        colorByPoint: true,
        data: [
          summary?.openIncidents ?? 0,
          summary?.inProgressIncidents ?? 0,
          summary?.resolvedIncidents ?? 0,
          summary?.closedIncidents ?? 0,
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const slaExposureChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "bar", height: 300 },
    colors: ["#dc2626", "#f59e0b", "#64748b"],
    xAxis: {
      categories: ["Overdue", "Due Within 24h", "No SLA Set"],
      labels: { style: { color: chartMutedColor, fontWeight: "700" } },
      lineColor: chartGridColor,
    },
    yAxis: {
      min: 0,
      allowDecimals: false,
      title: { text: "Active incidents", style: { color: chartMutedColor } },
      gridLineColor: chartGridColor,
      labels: { style: { color: chartMutedColor } },
    },
    legend: { enabled: false },
    tooltip: { pointFormat: "<b>{point.y}</b> incidents" },
    series: [
      {
        type: "bar",
        name: "SLA Exposure",
        borderRadius: 5,
        colorByPoint: true,
        data: [
          summary?.overdueIncidents ?? 0,
          summary?.dueSoonIncidents ?? 0,
          summary?.unscheduledActiveIncidents ?? 0,
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const severityChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "pie", height: 300 },
    colors: ["#38bdf8", "#f59e0b", "#f97316", "#dc2626"],
    tooltip: { pointFormat: "<b>{point.y}</b> incidents ({point.percentage:.0f}%)" },
    plotOptions: {
      pie: {
        innerSize: "55%",
        dataLabels: {
          color: chartTextColor,
          distance: 16,
          format: "{point.name}: {point.y}",
          style: {
            textOutline: "none",
            fontWeight: "700",
          },
        },
      },
    },
    series: [
      {
        type: "pie",
        name: "Severity",
        data: [
          ["Low", summary?.lowSeverityIncidents ?? 0],
          ["Medium", summary?.mediumSeverityIncidents ?? 0],
          ["High", summary?.highSeverityIncidents ?? 0],
          ["Critical", summary?.criticalSeverityIncidents ?? 0],
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const assignmentCoverageChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "pie", height: 300 },
    colors: ["#10b981", "#dc2626"],
    tooltip: { pointFormat: "<b>{point.y}</b> active incidents ({point.percentage:.0f}%)" },
    plotOptions: {
      pie: {
        innerSize: "64%",
        dataLabels: {
          color: chartTextColor,
          distance: 18,
          format: "{point.name}: {point.y}",
          style: {
            textOutline: "none",
            fontWeight: "700",
          },
        },
      },
    },
    series: [
      {
        type: "pie",
        name: "Assignment",
        data: [
          ["Assigned", summary?.assignedActiveIncidents ?? 0],
          ["Unassigned", summary?.unassignedActiveIncidents ?? 0],
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const userStatusChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "pie", height: 300 },
    colors: ["#10b981", "#64748b", "#dc2626"],
    tooltip: { pointFormat: "<b>{point.y}</b> users ({point.percentage:.0f}%)" },
    plotOptions: {
      pie: {
        innerSize: "60%",
        dataLabels: {
          color: chartTextColor,
          distance: 18,
          format: "{point.name}: {point.y}",
          style: {
            textOutline: "none",
            fontWeight: "700",
          },
        },
      },
    },
    series: [
      {
        type: "pie",
        name: "Users",
        data: [
          ["Active", summary?.activeUsers ?? 0],
          ["Inactive", summary?.inactiveUsers ?? 0],
          ["Suspended", summary?.suspendedUsers ?? 0],
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);
  const roleCoverageChart = useMemo<Highcharts.Options>(() => ({
    chart: { type: "column", height: 300 },
    colors: ["#2563eb"],
    xAxis: {
      categories: ["Admins", "Security", "Compliance", "Auditors"],
      labels: { style: { color: chartMutedColor, fontWeight: "700" } },
      lineColor: chartGridColor,
    },
    yAxis: {
      min: 0,
      allowDecimals: false,
      title: { text: "Users", style: { color: chartMutedColor } },
      gridLineColor: chartGridColor,
      labels: { style: { color: chartMutedColor } },
    },
    legend: { enabled: false },
    tooltip: { pointFormat: "<b>{point.y}</b> users" },
    series: [
      {
        type: "column",
        name: "Role Coverage",
        borderRadius: 5,
        data: [
          summary?.adminUsers ?? 0,
          summary?.securityAnalystUsers ?? 0,
          summary?.complianceOfficerUsers ?? 0,
          summary?.auditorUsers ?? 0,
        ],
      },
    ],
    lang: { noData: emptyChartLabel },
  }), [summary, emptyChartLabel]);

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

      <section className="dashboard-section">
        <div className="dashboard-section-header">
          <div>
            <span className="badge">Analytics</span>
            <h2>Dashboard Charts</h2>
          </div>
        </div>

        <div className="dashboard-charts-grid">
          <div className="chart-panel chart-panel-wide">
            <div>
              <span className="chart-kicker">Incident Trend</span>
              <h3>New Incidents: Last 7 Days</h3>
            </div>
            <DashboardChart options={incidentTrendChart} />
          </div>

          <div className="chart-panel chart-panel-wide">
            <div>
              <span className="chart-kicker">Incident Flow</span>
              <h3>Incident Status</h3>
            </div>
            <DashboardChart options={incidentStatusChart} />
          </div>

          <div className="chart-panel">
            <div>
              <span className="chart-kicker">Access Control</span>
              <h3>Account Status</h3>
            </div>
            <DashboardChart options={userStatusChart} />
          </div>

          <div className="chart-panel">
            <div>
              <span className="chart-kicker">SLA Risk</span>
              <h3>SLA Exposure</h3>
            </div>
            <DashboardChart options={slaExposureChart} />
          </div>

          <div className="chart-panel">
            <div>
              <span className="chart-kicker">Incident Risk</span>
              <h3>Severity Distribution</h3>
            </div>
            <DashboardChart options={severityChart} />
          </div>

          <div className="chart-panel">
            <div>
              <span className="chart-kicker">Triage Ownership</span>
              <h3>Assignment Coverage</h3>
            </div>
            <DashboardChart options={assignmentCoverageChart} />
          </div>

          <div className="chart-panel chart-panel-wide">
            <div>
              <span className="chart-kicker">Access Control</span>
              <h3>Role Coverage</h3>
            </div>
            <DashboardChart options={roleCoverageChart} />
          </div>
        </div>
      </section>

      <section className="dashboard-section">
        <div className="dashboard-section-header">
          <div>
            <span className="badge">Triage Queue</span>
            <h2>Recent Active Incidents</h2>
          </div>
        </div>

        {recentIncidents.length === 0 ? (
          <p className="text-muted">No active incidents in the triage queue.</p>
        ) : (
          <div className="recent-incident-list">
            {recentIncidents.map((incident) => (
              <article className="recent-incident-item" key={incident.id}>
                <div>
                  <strong>{incident.title}</strong>
                  <span>{incident.description}</span>
                </div>
                <div className="recent-incident-meta">
                  <span className={`severity-pill severity-${incident.severity.toLowerCase()}`}>
                    {incident.severity}
                  </span>
                  <span className={`status-pill status-${incident.status.toLowerCase().replace("_", "-")}`}>
                    {incident.status.replace("_", " ")}
                  </span>
                  <small>SLA: {formatDateTime(incident.dueAt)}</small>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default DashboardPage;
