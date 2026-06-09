import { useEffect, useMemo, useState } from "react";
import type Highcharts from "highcharts";
import DashboardChart from "../components/DashboardChart";
import PaginationControls from "../components/PaginationControls";
import SavedViewControls from "../components/SavedViewControls";
import SortControls from "../components/SortControls";
import { exportAuditLogsCsv, getAuditLogSummary, getAuditLogs } from "../api/auditApi";
import { getApiErrorMessage } from "../api/errorUtils";
import {
  createSavedView,
  deleteSavedView,
  getSavedViews,
} from "../api/savedViewApi";
import type { AuditLogResponse, AuditLogSummary } from "../types/audit";
import type { SortDirection } from "../types/pagination";
import type { SavedViewResponse } from "../types/savedView";

const ACTION_OPTIONS = [
  "USER_REGISTERED",
  "USER_LOGIN",
  "USER_UPDATED",
  "USER_DELETED",
  "INCIDENT_CREATED",
  "INCIDENT_UPDATED",
  "INCIDENT_DELETED",
  "INCIDENT_NOTE_ADDED",
];

const RESOURCE_TYPE_OPTIONS = ["USER", "INCIDENT"];
const AUDIT_SORT_OPTIONS = [
  { label: "Created", value: "createdAt" },
  { label: "Action", value: "action" },
  { label: "Resource", value: "resourceType" },
  { label: "Actor", value: "actorEmail" },
];

const formatAction = (action: string) => action.replaceAll("_", " ");

const emptyAuditSummary: AuditLogSummary = {
  totalLogs: 0,
  uniqueActors: 0,
  latestActivityAt: null,
  actionCounts: {},
  resourceTypeCounts: {},
};

const toChartEntries = (counts: Record<string, number>, limit = 6) =>
  Object.entries(counts)
    .sort(([, leftCount], [, rightCount]) => rightCount - leftCount)
    .slice(0, limit);

function AuditLogsPage() {
  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const isAdmin = currentUser?.roles?.includes("ADMIN");
  const [auditLogs, setAuditLogs] = useState<AuditLogResponse[]>([]);
  const [auditSummary, setAuditSummary] = useState<AuditLogSummary>(emptyAuditSummary);
  const [isLoading, setIsLoading] = useState(isAdmin);
  const [isExporting, setIsExporting] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [actionFilter, setActionFilter] = useState("");
  const [resourceTypeFilter, setResourceTypeFilter] = useState("");
  const [queryFilter, setQueryFilter] = useState("");
  const [createdFromFilter, setCreatedFromFilter] = useState("");
  const [createdToFilter, setCreatedToFilter] = useState("");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalAuditLogs, setTotalAuditLogs] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState<SortDirection>("desc");
  const [savedViews, setSavedViews] = useState<SavedViewResponse[]>([]);
  const [savedViewName, setSavedViewName] = useState("");
  const [isSavingView, setIsSavingView] = useState(false);

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    getSavedViews("AUDIT_LOGS")
      .then(setSavedViews)
      .catch((error: unknown) => {
        setMessage(getApiErrorMessage(error, "Failed to load saved views"));
        setMessageType("error");
      });
  }, [isAdmin]);

  const loadAuditLogs = async (targetPage = page, targetSize = pageSize) => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const filters = {
        action: actionFilter,
        resourceType: resourceTypeFilter,
        query: queryFilter,
        createdFrom: createdFromFilter,
        createdTo: createdToFilter,
        page: targetPage,
        size: targetSize,
        sortBy,
        sortDir,
      };
      const [response, summary] = await Promise.all([
        getAuditLogs(filters),
        getAuditLogSummary(filters),
      ]);
      setAuditLogs(response.content);
      setPage(response.page);
      setPageSize(response.size);
      setTotalAuditLogs(response.totalElements);
      setTotalPages(response.totalPages);
      setAuditSummary(summary);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  const handleExportCsv = async () => {
    setIsExporting(true);

    try {
      const csvBlob = await exportAuditLogsCsv({
        action: actionFilter,
        resourceType: resourceTypeFilter,
        query: queryFilter,
        createdFrom: createdFromFilter,
        createdTo: createdToFilter,
      });
      const downloadUrl = URL.createObjectURL(csvBlob);
      const downloadLink = document.createElement("a");
      const timestamp = new Date().toISOString().slice(0, 19).replaceAll(":", "-");

      downloadLink.href = downloadUrl;
      downloadLink.download = `audit-logs-export-${timestamp}.csv`;
      document.body.appendChild(downloadLink);
      downloadLink.click();
      downloadLink.remove();
      URL.revokeObjectURL(downloadUrl);
      showMessage("Audit CSV exported", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to export audit logs"), "error");
    } finally {
      setIsExporting(false);
    }
  };

  const buildSavedViewFilterJson = () =>
    JSON.stringify({
      actionFilter,
      resourceTypeFilter,
      queryFilter,
      createdFromFilter,
      createdToFilter,
      sortBy,
      sortDir,
      pageSize,
    });

  const handleSaveView = async () => {
    if (!savedViewName.trim()) {
      return;
    }

    setIsSavingView(true);

    try {
      const savedView = await createSavedView({
        viewType: "AUDIT_LOGS",
        name: savedViewName,
        filterJson: buildSavedViewFilterJson(),
      });
      setSavedViews((currentSavedViews) => [savedView, ...currentSavedViews]);
      setSavedViewName("");
      showMessage("Saved view created", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to save view"), "error");
    } finally {
      setIsSavingView(false);
    }
  };

  const handleApplySavedView = (savedView: SavedViewResponse) => {
    try {
      const filters = JSON.parse(savedView.filterJson) as Record<string, string | number>;

      setActionFilter(String(filters.actionFilter ?? ""));
      setResourceTypeFilter(String(filters.resourceTypeFilter ?? ""));
      setQueryFilter(String(filters.queryFilter ?? ""));
      setCreatedFromFilter(String(filters.createdFromFilter ?? ""));
      setCreatedToFilter(String(filters.createdToFilter ?? ""));
      setSortBy(String(filters.sortBy ?? "createdAt"));
      setSortDir((filters.sortDir as SortDirection) ?? "desc");
      setPageSize(Number(filters.pageSize ?? 10));
      setPage(0);
      showMessage(`Applied saved view: ${savedView.name}`, "success");
    } catch {
      showMessage("Saved view could not be applied", "error");
    }
  };

  const handleDeleteSavedView = async (savedView: SavedViewResponse) => {
    try {
      await deleteSavedView(savedView.id);
      setSavedViews((currentSavedViews) =>
        currentSavedViews.filter((item) => item.id !== savedView.id)
      );
      showMessage("Saved view deleted", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to delete saved view"), "error");
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    const filters = {
      action: actionFilter,
      resourceType: resourceTypeFilter,
      query: queryFilter,
      createdFrom: createdFromFilter,
      createdTo: createdToFilter,
      page,
      size: pageSize,
      sortBy,
      sortDir,
    };

    Promise.all([getAuditLogs(filters), getAuditLogSummary(filters)])
      .then(([response, summary]) => {
        setAuditLogs(response.content);
        setPage(response.page);
        setPageSize(response.size);
        setTotalAuditLogs(response.totalElements);
        setTotalPages(response.totalPages);
        setAuditSummary(summary);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [
    isAdmin,
    actionFilter,
    resourceTypeFilter,
    queryFilter,
    createdFromFilter,
    createdToFilter,
    page,
    pageSize,
    sortBy,
    sortDir,
  ]);

  const actionChartOptions = useMemo<Highcharts.Options>(() => {
    const entries = toChartEntries(auditSummary.actionCounts);

    return {
      chart: { type: "bar", height: 260 },
      xAxis: {
        categories: entries.map(([action]) => formatAction(action)),
        title: { text: undefined },
      },
      yAxis: {
        allowDecimals: false,
        min: 0,
        title: { text: undefined },
      },
      legend: { enabled: false },
      tooltip: { pointFormat: "<b>{point.y}</b> events" },
      plotOptions: {
        bar: {
          colorByPoint: true,
          colors: ["#2563eb", "#14b8a6", "#f59e0b", "#e11d48", "#7c3aed", "#0f766e"],
          borderRadius: 6,
        },
      },
      series: [
        {
          type: "bar",
          name: "Events",
          data: entries.map(([, count]) => count),
        },
      ],
    };
  }, [auditSummary.actionCounts]);

  const resourceChartOptions = useMemo<Highcharts.Options>(() => {
    const entries = toChartEntries(auditSummary.resourceTypeCounts);

    return {
      chart: { type: "pie", height: 260 },
      tooltip: { pointFormat: "<b>{point.y}</b> events" },
      plotOptions: {
        pie: {
          innerSize: "62%",
          dataLabels: {
            enabled: true,
            format: "{point.name}: {point.y}",
            style: { fontSize: "12px", textOutline: "none" },
          },
        },
      },
      series: [
        {
          type: "pie",
          name: "Resources",
          data: entries.map(([resourceType, count]) => ({
            name: resourceType,
            y: count,
          })),
        },
      ],
    };
  }, [auditSummary.resourceTypeCounts]);

  if (!isAdmin) {
    return (
      <div className="page-container">
        <section className="dashboard-hero">
          <span className="badge">Audit Logs</span>
          <h1 style={{ marginTop: "18px" }}>Admin Access Required</h1>
          <p className="page-subtitle">
            Audit activity is available only for ADMIN accounts.
          </p>
        </section>
      </div>
    );
  }

  return (
    <div className="page-container">
      <section className="page-header">
        <div>
          <span className="badge">Compliance Trail</span>
          <h1>Audit Logs</h1>
          <p className="page-subtitle">
            Review recent platform activity across users, incidents, and case notes.
          </p>
        </div>

        <div className="page-header-actions">
          <button className="btn-secondary" onClick={handleExportCsv} disabled={isExporting}>
            {isExporting ? "Exporting..." : "Export CSV"}
          </button>
          <button className="btn-secondary" onClick={() => loadAuditLogs()} disabled={isLoading}>
            Refresh
          </button>
        </div>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

      <SavedViewControls
        savedViews={savedViews}
        saveName={savedViewName}
        isSaving={isSavingView}
        placeholder="Recent incident changes"
        onApply={handleApplySavedView}
        onDelete={handleDeleteSavedView}
        onNameChange={setSavedViewName}
        onSave={handleSaveView}
      />

      <section className="filter-bar">
        <div className="filter-group">
          <label>Search</label>
          <input
            value={queryFilter}
            onChange={(event) => {
              setQueryFilter(event.target.value);
              setPage(0);
            }}
            placeholder="Actor, details, or resource ID"
          />
        </div>

        <div className="filter-group">
          <label>Action</label>
          <select
            value={actionFilter}
            onChange={(event) => {
              setActionFilter(event.target.value);
              setPage(0);
            }}
          >
            <option value="">All actions</option>
            {ACTION_OPTIONS.map((action) => (
              <option key={action} value={action}>
                {formatAction(action)}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Resource</label>
          <select
            value={resourceTypeFilter}
            onChange={(event) => {
              setResourceTypeFilter(event.target.value);
              setPage(0);
            }}
          >
            <option value="">All resources</option>
            {RESOURCE_TYPE_OPTIONS.map((resourceType) => (
              <option key={resourceType} value={resourceType}>
                {resourceType}
              </option>
            ))}
          </select>
        </div>

        <button
          className="btn-secondary"
          onClick={() => {
            setQueryFilter("");
            setActionFilter("");
            setResourceTypeFilter("");
            setCreatedFromFilter("");
            setCreatedToFilter("");
            setPage(0);
          }}
          disabled={
            !queryFilter &&
            !actionFilter &&
            !resourceTypeFilter &&
            !createdFromFilter &&
            !createdToFilter
          }
        >
          Clear Filters
        </button>
      </section>

      <section className="filter-bar date-filter-bar compact-date-filter-bar">
        <div className="filter-group">
          <label>Created From</label>
          <input
            type="datetime-local"
            value={createdFromFilter}
            onChange={(event) => {
              setCreatedFromFilter(event.target.value);
              setPage(0);
            }}
          />
        </div>

        <div className="filter-group">
          <label>Created To</label>
          <input
            type="datetime-local"
            value={createdToFilter}
            onChange={(event) => {
              setCreatedToFilter(event.target.value);
              setPage(0);
            }}
          />
        </div>
      </section>

      <section className="sort-bar">
        <SortControls
          options={AUDIT_SORT_OPTIONS}
          sortBy={sortBy}
          sortDir={sortDir}
          onSortByChange={(nextSortBy) => {
            setSortBy(nextSortBy);
            setPage(0);
          }}
          onSortDirChange={(nextSortDir) => {
            setSortDir(nextSortDir);
            setPage(0);
          }}
        />
      </section>

      <section className="audit-insights-grid">
        <article className="audit-kpi-panel">
          <span className="metric-label">Filtered Events</span>
          <strong>{auditSummary.totalLogs.toLocaleString()}</strong>
          <small>{totalAuditLogs.toLocaleString()} rows available in the table</small>
        </article>

        <article className="audit-kpi-panel accent-teal">
          <span className="metric-label">Unique Actors</span>
          <strong>{auditSummary.uniqueActors.toLocaleString()}</strong>
          <small>Across the active audit filters</small>
        </article>

        <article className="audit-kpi-panel accent-amber">
          <span className="metric-label">Latest Activity</span>
          <strong className="kpi-date">
            {auditSummary.latestActivityAt
              ? new Date(auditSummary.latestActivityAt).toLocaleString()
              : "No activity"}
          </strong>
          <small>Most recent matching audit event</small>
        </article>
      </section>

      <section className="audit-chart-grid">
        <article className="chart-panel">
          <div className="chart-panel-header">
            <div>
              <span className="badge">Action Mix</span>
              <h3>Top Audit Actions</h3>
            </div>
          </div>
          {Object.keys(auditSummary.actionCounts).length === 0 ? (
            <p className="text-muted">No action data for these filters.</p>
          ) : (
            <DashboardChart options={actionChartOptions} />
          )}
        </article>

        <article className="chart-panel">
          <div className="chart-panel-header">
            <div>
              <span className="badge">Resource Mix</span>
              <h3>Activity by Resource</h3>
            </div>
          </div>
          {Object.keys(auditSummary.resourceTypeCounts).length === 0 ? (
            <p className="text-muted">No resource data for these filters.</p>
          ) : (
            <DashboardChart options={resourceChartOptions} />
          )}
        </article>
      </section>

      <section className="table-panel">
        {isLoading ? (
          <p className="text-muted">Loading audit logs...</p>
        ) : auditLogs.length === 0 ? (
          <p className="text-muted">No audit activity has been recorded yet.</p>
        ) : (
          <div className="table-wrap">
            <table className="data-table audit-table">
              <thead>
                <tr>
                  <th>Action</th>
                  <th>Resource</th>
                  <th>Actor</th>
                  <th>Details</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map((auditLog) => (
                  <tr key={auditLog.id}>
                    <td>
                      <span className="audit-action">
                        {formatAction(auditLog.action)}
                      </span>
                    </td>
                    <td>
                      <strong>{auditLog.resourceType}</strong>
                      {auditLog.resourceId && (
                        <span className="table-description">ID {auditLog.resourceId}</span>
                      )}
                    </td>
                    <td>{auditLog.actorEmail}</td>
                    <td>{auditLog.details ?? "No details provided"}</td>
                    <td>{new Date(auditLog.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <PaginationControls
          page={page}
          size={pageSize}
          totalElements={totalAuditLogs}
          totalPages={totalPages}
          isLoading={isLoading}
          onPageChange={(nextPage) => setPage(nextPage)}
          onSizeChange={(nextSize) => {
            setPageSize(nextSize);
            setPage(0);
          }}
        />
      </section>
    </div>
  );
}

export default AuditLogsPage;
