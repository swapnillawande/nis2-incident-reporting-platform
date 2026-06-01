import { useEffect, useMemo, useState } from "react";
import PaginationControls from "../components/PaginationControls";
import SortControls from "../components/SortControls";
import { exportAuditLogsCsv, getAuditLogs } from "../api/auditApi";
import { getApiErrorMessage } from "../api/errorUtils";
import type { AuditLogResponse } from "../types/audit";
import type { SortDirection } from "../types/pagination";

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

function AuditLogsPage() {
  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const isAdmin = currentUser?.roles?.includes("ADMIN");
  const [auditLogs, setAuditLogs] = useState<AuditLogResponse[]>([]);
  const [isLoading, setIsLoading] = useState(isAdmin);
  const [isExporting, setIsExporting] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [actionFilter, setActionFilter] = useState("");
  const [resourceTypeFilter, setResourceTypeFilter] = useState("");
  const [queryFilter, setQueryFilter] = useState("");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalAuditLogs, setTotalAuditLogs] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState<SortDirection>("desc");

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadAuditLogs = async (targetPage = page, targetSize = pageSize) => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAuditLogs({
        action: actionFilter,
        resourceType: resourceTypeFilter,
        query: queryFilter,
        page: targetPage,
        size: targetSize,
        sortBy,
        sortDir,
      });
      setAuditLogs(response.content);
      setPage(response.page);
      setPageSize(response.size);
      setTotalAuditLogs(response.totalElements);
      setTotalPages(response.totalPages);
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

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    getAuditLogs({
      action: actionFilter,
      resourceType: resourceTypeFilter,
      query: queryFilter,
      page,
      size: pageSize,
      sortBy,
      sortDir,
    })
      .then((response) => {
        setAuditLogs(response.content);
        setPage(response.page);
        setPageSize(response.size);
        setTotalAuditLogs(response.totalElements);
        setTotalPages(response.totalPages);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isAdmin, actionFilter, resourceTypeFilter, queryFilter, page, pageSize, sortBy, sortDir]);

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
            setPage(0);
          }}
          disabled={!queryFilter && !actionFilter && !resourceTypeFilter}
        >
          Clear Filters
        </button>
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
