import { useEffect, useMemo, useState } from "react";
import { getAuditLogs } from "../api/auditApi";
import { getApiErrorMessage } from "../api/errorUtils";
import type { AuditLogResponse } from "../types/audit";

const formatAction = (action: string) => action.replaceAll("_", " ");

function AuditLogsPage() {
  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const isAdmin = currentUser?.roles?.includes("ADMIN");
  const [auditLogs, setAuditLogs] = useState<AuditLogResponse[]>([]);
  const [isLoading, setIsLoading] = useState(isAdmin);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadAuditLogs = async () => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAuditLogs();
      setAuditLogs(response);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    getAuditLogs()
      .then((response) => {
        setAuditLogs(response);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load audit logs"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isAdmin]);

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

        <button className="btn-secondary" onClick={loadAuditLogs} disabled={isLoading}>
          Refresh
        </button>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

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
