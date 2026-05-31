import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../api/errorUtils";
import {
  addIncidentNote,
  createIncident,
  deleteIncident,
  getAllIncidents,
  getIncidentNotes,
  updateIncident,
} from "../api/incidentApi";
import type {
  CreateIncidentRequest,
  IncidentResponse,
  IncidentNote,
  IncidentSeverity,
  IncidentStatus,
  UpdateIncidentRequest,
} from "../types/incident";

const SEVERITY_OPTIONS: IncidentSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const STATUS_OPTIONS: IncidentStatus[] = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];

const emptyCreateForm: CreateIncidentRequest = {
  title: "",
  description: "",
  severity: "MEDIUM",
};

function IncidentsPage() {
  const [incidents, setIncidents] = useState<IncidentResponse[]>([]);
  const [createForm, setCreateForm] = useState<CreateIncidentRequest>(emptyCreateForm);
  const [selectedIncident, setSelectedIncident] = useState<IncidentResponse | null>(null);
  const [editForm, setEditForm] = useState<UpdateIncidentRequest | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [statusFilter, setStatusFilter] = useState<IncidentStatus | "">("");
  const [severityFilter, setSeverityFilter] = useState<IncidentSeverity | "">("");
  const [incidentNotes, setIncidentNotes] = useState<IncidentNote[]>([]);
  const [newNote, setNewNote] = useState("");
  const [isLoadingNotes, setIsLoadingNotes] = useState(false);
  const [isAddingNote, setIsAddingNote] = useState(false);

  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const canDelete = currentUser?.roles?.some((role: string) =>
    ["ADMIN", "SECURITY_ANALYST"].includes(role)
  );

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadIncidents = async () => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAllIncidents({
        status: statusFilter,
        severity: severityFilter,
      });
      setIncidents(response);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load incidents"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getAllIncidents({
      status: statusFilter,
      severity: severityFilter,
    })
      .then((response) => {
        setIncidents(response);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load incidents"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [statusFilter, severityFilter]);

  const handleCreate = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsCreating(true);

    try {
      const response = await createIncident(createForm);
      const matchesStatus = !statusFilter || response.status === statusFilter;
      const matchesSeverity = !severityFilter || response.severity === severityFilter;

      if (matchesStatus && matchesSeverity) {
        setIncidents((currentIncidents) => [response, ...currentIncidents]);
      }

      setCreateForm(emptyCreateForm);
      showMessage("Incident created successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to create incident"), "error");
    } finally {
      setIsCreating(false);
    }
  };

  const openEdit = async (incident: IncidentResponse) => {
    setSelectedIncident(incident);
    setEditForm({
      title: incident.title,
      description: incident.description,
      severity: incident.severity,
      status: incident.status,
    });
    setIncidentNotes([]);
    setNewNote("");
    setIsLoadingNotes(true);

    try {
      const response = await getIncidentNotes(incident.id);
      setIncidentNotes(response);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load incident notes"), "error");
    } finally {
      setIsLoadingNotes(false);
    }
  };

  const closeEdit = () => {
    setSelectedIncident(null);
    setEditForm(null);
    setIncidentNotes([]);
    setNewNote("");
  };

  const handleSave = async () => {
    if (!selectedIncident || !editForm) {
      return;
    }

    try {
      const response = await updateIncident(selectedIncident.id, editForm);
      setIncidents((currentIncidents) =>
        currentIncidents.map((incident) =>
          incident.id === response.id ? response : incident
        )
      );
      closeEdit();
      showMessage("Incident updated successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to update incident"), "error");
    }
  };

  const handleDelete = async (incident: IncidentResponse) => {
    const confirmed = window.confirm(`Delete incident "${incident.title}"?`);

    if (!confirmed) {
      return;
    }

    try {
      await deleteIncident(incident.id);
      setIncidents((currentIncidents) =>
        currentIncidents.filter((currentIncident) => currentIncident.id !== incident.id)
      );
      showMessage("Incident deleted successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to delete incident"), "error");
    }
  };

  const handleAddNote = async () => {
    if (!selectedIncident || !newNote.trim()) {
      return;
    }

    setIsAddingNote(true);

    try {
      const response = await addIncidentNote(selectedIncident.id, {
        note: newNote.trim(),
      });
      setIncidentNotes((currentNotes) => [response, ...currentNotes]);
      setNewNote("");
      showMessage("Incident note added", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to add incident note"), "error");
    } finally {
      setIsAddingNote(false);
    }
  };

  return (
    <div className="page-container">
      <section className="page-header">
        <div>
          <span className="badge">Incident Response</span>
          <h1>Incidents</h1>
          <p className="page-subtitle">
            Capture security incidents, track severity, and move each case through resolution.
          </p>
        </div>

        <button className="btn-secondary" onClick={loadIncidents} disabled={isLoading}>
          Refresh
        </button>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

      <section className="filter-bar">
        <div className="filter-group">
          <label>Status</label>
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as IncidentStatus | "")}
          >
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {status.replace("_", " ")}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Severity</label>
          <select
            value={severityFilter}
            onChange={(event) =>
              setSeverityFilter(event.target.value as IncidentSeverity | "")
            }
          >
            <option value="">All severities</option>
            {SEVERITY_OPTIONS.map((severity) => (
              <option key={severity} value={severity}>
                {severity}
              </option>
            ))}
          </select>
        </div>

        <button
          className="btn-secondary"
          onClick={() => {
            setStatusFilter("");
            setSeverityFilter("");
          }}
          disabled={!statusFilter && !severityFilter}
        >
          Clear Filters
        </button>
      </section>

      <section className="table-panel incident-create-panel">
        <h3>Report Incident</h3>
        <form className="incident-form" onSubmit={handleCreate}>
          <div className="form-group">
            <label>Title</label>
            <input
              value={createForm.title}
              onChange={(event) =>
                setCreateForm({ ...createForm, title: event.target.value })
              }
              placeholder="Suspicious login attempts"
            />
          </div>

          <div className="form-group">
            <label>Severity</label>
            <select
              value={createForm.severity}
              onChange={(event) =>
                setCreateForm({
                  ...createForm,
                  severity: event.target.value as IncidentSeverity,
                })
              }
            >
              {SEVERITY_OPTIONS.map((severity) => (
                <option key={severity} value={severity}>
                  {severity}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group span-2">
            <label>Description</label>
            <textarea
              value={createForm.description}
              onChange={(event) =>
                setCreateForm({ ...createForm, description: event.target.value })
              }
              placeholder="Describe the event, affected system, and immediate observations."
            />
          </div>

          <button className="btn-primary" type="submit" disabled={isCreating}>
            {isCreating ? "Creating..." : "Create Incident"}
          </button>
        </form>
      </section>

      <section className="table-panel">
        {isLoading ? (
          <p className="text-muted">Loading incidents...</p>
        ) : incidents.length === 0 ? (
          <p className="text-muted">No incidents reported yet.</p>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Reported By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {incidents.map((incident) => (
                  <tr key={incident.id}>
                    <td>
                      <strong>{incident.title}</strong>
                      <span className="table-description">{incident.description}</span>
                    </td>
                    <td>
                      <span className={`severity-pill severity-${incident.severity.toLowerCase()}`}>
                        {incident.severity}
                      </span>
                    </td>
                    <td>
                      <span className={`status-pill status-${incident.status.toLowerCase().replace("_", "-")}`}>
                        {incident.status.replace("_", " ")}
                      </span>
                    </td>
                    <td>{incident.reportedByEmail}</td>
                    <td>
                      <div className="table-actions">
                        <button className="btn-secondary compact" onClick={() => openEdit(incident)}>
                          Edit
                        </button>
                        {canDelete && (
                          <button
                            className="btn-danger compact"
                            onClick={() => handleDelete(incident)}
                          >
                            Delete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selectedIncident && editForm && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" role="dialog" aria-modal="true">
            <div className="modal-header">
              <div>
                <span className="badge">Edit Incident</span>
                <h2>{selectedIncident.title}</h2>
              </div>
              <button className="icon-close" onClick={closeEdit} aria-label="Close">
                x
              </button>
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label>Title</label>
                <input
                  value={editForm.title ?? ""}
                  onChange={(event) =>
                    setEditForm({ ...editForm, title: event.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Severity</label>
                <select
                  value={editForm.severity}
                  onChange={(event) =>
                    setEditForm({
                      ...editForm,
                      severity: event.target.value as IncidentSeverity,
                    })
                  }
                >
                  {SEVERITY_OPTIONS.map((severity) => (
                    <option key={severity} value={severity}>
                      {severity}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Status</label>
                <select
                  value={editForm.status}
                  onChange={(event) =>
                    setEditForm({
                      ...editForm,
                      status: event.target.value as IncidentStatus,
                    })
                  }
                >
                  {STATUS_OPTIONS.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group span-2">
                <label>Description</label>
                <textarea
                  value={editForm.description ?? ""}
                  onChange={(event) =>
                    setEditForm({ ...editForm, description: event.target.value })
                  }
                />
              </div>
            </div>

            <div className="modal-actions">
              <button className="btn-secondary" onClick={closeEdit}>
                Cancel
              </button>
              <button className="btn-primary" onClick={handleSave}>
                Save Changes
              </button>
            </div>

            <section className="notes-panel">
              <h3>Timeline Notes</h3>

              <div className="note-composer">
                <textarea
                  value={newNote}
                  onChange={(event) => setNewNote(event.target.value)}
                  placeholder="Add a triage update, containment step, or evidence note."
                />
                <button
                  className="btn-secondary"
                  onClick={handleAddNote}
                  disabled={isAddingNote || !newNote.trim()}
                >
                  {isAddingNote ? "Adding..." : "Add Note"}
                </button>
              </div>

              {isLoadingNotes ? (
                <p className="text-muted">Loading notes...</p>
              ) : incidentNotes.length === 0 ? (
                <p className="text-muted">No timeline notes yet.</p>
              ) : (
                <div className="notes-list">
                  {incidentNotes.map((note) => (
                    <article className="note-item" key={note.id}>
                      <p>{note.note}</p>
                      <span>
                        {note.createdByEmail} · {new Date(note.createdAt).toLocaleString()}
                      </span>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </section>
        </div>
      )}
    </div>
  );
}

export default IncidentsPage;
