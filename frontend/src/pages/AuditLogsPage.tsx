import { useEffect, useMemo, useState } from "react";
import { exportAuditLogsCsv, getAuditLogs } from "../api/auditApi";
import { getApiErrorMessage } from "../api/errorUtils";
import type { AuditLogResponse } from "../types/audit";

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

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadAuditLogs = async () => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAuditLogs({
        action: actionFilter,
        resourceType: resourceTypeFilter,
        query: queryFilter,
      });
      setAuditLogs(response);
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
    })
      .then((response) => {
        setAuditLogs(response);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isAdmin, actionFilter, resourceTypeFilter, queryFilter]);

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
          <button className="btn-secondary" onClick={loadAuditLogs} disabled={isLoading}>
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
            onChange={(event) => setQueryFilter(event.target.value)}
            placeholder="Actor, details, or resource ID"
          />
        </div>

        <div className="filter-group">
          <label>Action</label>
          <select
            value={actionFilter}
            onChange={(event) => setActionFilter(event.target.value)}
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
            onChange={(event) => setResourceTypeFilter(event.target.value)}
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
          }}
          disabled={!queryFilter && !actionFilter && !resourceTypeFilter}
        >
          Clear Filters
        </button>
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
      </section>
    </div>
  );
}

export default AuditLogsPage;
