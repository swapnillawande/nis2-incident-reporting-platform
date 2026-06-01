import { useEffect, useMemo, useState } from "react";
import PaginationControls from "../components/PaginationControls";
import SavedViewControls from "../components/SavedViewControls";
import SortControls from "../components/SortControls";
import { getApiErrorMessage } from "../api/errorUtils";
import {
  addIncidentNote,
  bulkUpdateIncidentStatus,
  createIncident,
  deleteIncident,
  exportIncidentsCsv,
  getAllIncidents,
  getIncidentTimeline,
  updateIncident,
} from "../api/incidentApi";
import {
  createSavedView,
  deleteSavedView,
  getSavedViews,
} from "../api/savedViewApi";
import type {
  CreateIncidentRequest,
  IncidentResponse,
  IncidentTimelineItem,
  IncidentSeverity,
  IncidentStatus,
  UpdateIncidentRequest,
} from "../types/incident";
import type { SortDirection } from "../types/pagination";
import type { SavedViewResponse } from "../types/savedView";

const SEVERITY_OPTIONS: IncidentSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
const STATUS_OPTIONS: IncidentStatus[] = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const INCIDENT_SORT_OPTIONS = [
  { label: "Created", value: "createdAt" },
  { label: "Title", value: "title" },
  { label: "Severity", value: "severity" },
  { label: "Status", value: "status" },
  { label: "SLA Due", value: "dueAt" },
  { label: "Assigned To", value: "assignedToEmail" },
];

const emptyCreateForm: CreateIncidentRequest = {
  title: "",
  description: "",
  severity: "MEDIUM",
  assignedToEmail: "",
  dueAt: "",
};

const toDateTimeLocalValue = (dateTime?: string | null) => {
  if (!dateTime) {
    return "";
  }

  return dateTime.slice(0, 16);
};

const formatDueAt = (dateTime?: string | null) => {
  if (!dateTime) {
    return "No SLA";
  }

  return new Date(dateTime).toLocaleString();
};

const formatReportedAt = (dateTime: string) => new Date(dateTime).toLocaleString();

const formatTimelineAction = (action?: string | null) => {
  if (!action) {
    return "Activity";
  }

  return action
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
};

const getDueStatus = (incident: IncidentResponse) => {
  if (!incident.dueAt) {
    return "No SLA";
  }

  if (["RESOLVED", "CLOSED"].includes(incident.status)) {
    return "Completed";
  }

  const dueAtTime = new Date(incident.dueAt).getTime();
  const now = Date.now();
  const dayInMilliseconds = 24 * 60 * 60 * 1000;

  if (dueAtTime < now) {
    return "Overdue";
  }

  if (dueAtTime - now <= dayInMilliseconds) {
    return "Due Soon";
  }

  return "On Track";
};

function IncidentsPage() {
  const [incidents, setIncidents] = useState<IncidentResponse[]>([]);
  const [createForm, setCreateForm] = useState<CreateIncidentRequest>(emptyCreateForm);
  const [selectedIncident, setSelectedIncident] = useState<IncidentResponse | null>(null);
  const [editForm, setEditForm] = useState<UpdateIncidentRequest | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [isBulkUpdating, setIsBulkUpdating] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [statusFilter, setStatusFilter] = useState<IncidentStatus | "">("");
  const [severityFilter, setSeverityFilter] = useState<IncidentSeverity | "">("");
  const [assignedToFilter, setAssignedToFilter] = useState("");
  const [queryFilter, setQueryFilter] = useState("");
  const [createdFromFilter, setCreatedFromFilter] = useState("");
  const [createdToFilter, setCreatedToFilter] = useState("");
  const [dueFromFilter, setDueFromFilter] = useState("");
  const [dueToFilter, setDueToFilter] = useState("");
  const [incidentTimeline, setIncidentTimeline] = useState<IncidentTimelineItem[]>([]);
  const [newNote, setNewNote] = useState("");
  const [isLoadingTimeline, setIsLoadingTimeline] = useState(false);
  const [isAddingNote, setIsAddingNote] = useState(false);
  const [selectedIncidentIds, setSelectedIncidentIds] = useState<Set<number>>(
    () => new Set()
  );
  const [bulkStatus, setBulkStatus] = useState<IncidentStatus>("IN_PROGRESS");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalIncidents, setTotalIncidents] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDir, setSortDir] = useState<SortDirection>("desc");
  const [savedViews, setSavedViews] = useState<SavedViewResponse[]>([]);
  const [savedViewName, setSavedViewName] = useState("");
  const [isSavingView, setIsSavingView] = useState(false);

  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const canDelete = currentUser?.roles?.some((role: string) =>
    ["ADMIN", "SECURITY_ANALYST"].includes(role)
  );
  const visibleIncidentIds = useMemo(
    () => incidents.map((incident) => incident.id),
    [incidents]
  );
  const selectedVisibleCount = visibleIncidentIds.filter((incidentId) =>
    selectedIncidentIds.has(incidentId)
  ).length;
  const allVisibleSelected =
    visibleIncidentIds.length > 0 && selectedVisibleCount === visibleIncidentIds.length;

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  useEffect(() => {
    getSavedViews("INCIDENTS")
      .then(setSavedViews)
      .catch((error: unknown) => {
        setMessage(getApiErrorMessage(error, "Failed to load saved views"));
        setMessageType("error");
      });
  }, []);

  const loadIncidents = async (targetPage = page, targetSize = pageSize) => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAllIncidents({
        status: statusFilter,
        severity: severityFilter,
        assignedToEmail: assignedToFilter,
        query: queryFilter,
        createdFrom: createdFromFilter,
        createdTo: createdToFilter,
        dueFrom: dueFromFilter,
        dueTo: dueToFilter,
        page: targetPage,
        size: targetSize,
        sortBy,
        sortDir,
      });
      setIncidents(response.content);
      setPage(response.page);
      setPageSize(response.size);
      setTotalIncidents(response.totalElements);
      setTotalPages(response.totalPages);
      setSelectedIncidentIds(new Set());
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
        assignedToEmail: assignedToFilter,
        query: queryFilter,
        createdFrom: createdFromFilter,
        createdTo: createdToFilter,
        dueFrom: dueFromFilter,
        dueTo: dueToFilter,
        page,
        size: pageSize,
        sortBy,
        sortDir,
      })
      .then((response) => {
        setIncidents(response.content);
        setPage(response.page);
        setPageSize(response.size);
        setTotalIncidents(response.totalElements);
        setTotalPages(response.totalPages);
        setSelectedIncidentIds(new Set());
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load incidents"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [
    statusFilter,
    severityFilter,
    assignedToFilter,
    queryFilter,
    createdFromFilter,
    createdToFilter,
    dueFromFilter,
    dueToFilter,
    page,
    pageSize,
    sortBy,
    sortDir,
  ]);

  const handleCreate = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsCreating(true);

    try {
      const response = await createIncident(createForm);
      const matchesStatus = !statusFilter || response.status === statusFilter;
      const matchesSeverity = !severityFilter || response.severity === severityFilter;
      const matchesAssignee =
        !assignedToFilter.trim() ||
        response.assignedToEmail?.toLowerCase() === assignedToFilter.trim().toLowerCase();
      const normalizedQuery = queryFilter.trim().toLowerCase();
      const matchesQuery =
        !normalizedQuery ||
        response.title.toLowerCase().includes(normalizedQuery) ||
        response.description.toLowerCase().includes(normalizedQuery) ||
        (response.assignedToEmail?.toLowerCase().includes(normalizedQuery) ?? false) ||
        (response.dueAt ? formatDueAt(response.dueAt).toLowerCase().includes(normalizedQuery) : false);

      setCreateForm(emptyCreateForm);
      if (matchesStatus && matchesSeverity && matchesAssignee && matchesQuery) {
        await loadIncidents(0, pageSize);
      }
      showMessage("Incident created successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to create incident"), "error");
    } finally {
      setIsCreating(false);
    }
  };

  const loadIncidentTimeline = async (incidentId: number) => {
    setIsLoadingTimeline(true);

    try {
      const response = await getIncidentTimeline(incidentId);
      setIncidentTimeline(response);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load incident timeline"), "error");
    } finally {
      setIsLoadingTimeline(false);
    }
  };

  const openEdit = async (incident: IncidentResponse) => {
    setSelectedIncident(incident);
    setEditForm({
      title: incident.title,
      description: incident.description,
      severity: incident.severity,
      status: incident.status,
      assignedToEmail: incident.assignedToEmail ?? "",
      dueAt: toDateTimeLocalValue(incident.dueAt),
    });
    setIncidentTimeline([]);
    setNewNote("");
    await loadIncidentTimeline(incident.id);
  };

  const closeEdit = () => {
    setSelectedIncident(null);
    setEditForm(null);
    setIncidentTimeline([]);
    setNewNote("");
  };

  const handleSave = async () => {
    if (!selectedIncident || !editForm) {
      return;
    }

    try {
      await updateIncident(selectedIncident.id, editForm);
      closeEdit();
      await loadIncidents(page, pageSize);
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
      const nextPage = incidents.length === 1 && page > 0 ? page - 1 : page;
      await loadIncidents(nextPage, pageSize);
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
      await addIncidentNote(selectedIncident.id, {
        note: newNote.trim(),
      });
      await loadIncidentTimeline(selectedIncident.id);
      setNewNote("");
      showMessage("Incident note added", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to add incident note"), "error");
    } finally {
      setIsAddingNote(false);
    }
  };

  const toggleIncidentSelection = (incidentId: number) => {
    setSelectedIncidentIds((currentIds) => {
      const nextIds = new Set(currentIds);

      if (nextIds.has(incidentId)) {
        nextIds.delete(incidentId);
      } else {
        nextIds.add(incidentId);
      }

      return nextIds;
    });
  };

  const toggleAllVisibleIncidents = () => {
    setSelectedIncidentIds((currentIds) => {
      if (allVisibleSelected) {
        return new Set(
          Array.from(currentIds).filter((incidentId) => !visibleIncidentIds.includes(incidentId))
        );
      }

      return new Set([...Array.from(currentIds), ...visibleIncidentIds]);
    });
  };

  const handleBulkStatusUpdate = async () => {
    const incidentIds = Array.from(selectedIncidentIds);

    if (incidentIds.length === 0) {
      return;
    }

    setIsBulkUpdating(true);

    try {
      const updatedIncidents = await bulkUpdateIncidentStatus({
        incidentIds,
        status: bulkStatus,
      });
      await loadIncidents(page, pageSize);
      setSelectedIncidentIds(new Set());
      showMessage(`${updatedIncidents.length} incidents updated to ${bulkStatus.replace("_", " ")}`, "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to update selected incidents"), "error");
    } finally {
      setIsBulkUpdating(false);
    }
  };

  const buildSavedViewFilterJson = () =>
    JSON.stringify({
      statusFilter,
      severityFilter,
      assignedToFilter,
      queryFilter,
      createdFromFilter,
      createdToFilter,
      dueFromFilter,
      dueToFilter,
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
        viewType: "INCIDENTS",
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

      setStatusFilter((filters.statusFilter as IncidentStatus | "") ?? "");
      setSeverityFilter((filters.severityFilter as IncidentSeverity | "") ?? "");
      setAssignedToFilter(String(filters.assignedToFilter ?? ""));
      setQueryFilter(String(filters.queryFilter ?? ""));
      setCreatedFromFilter(String(filters.createdFromFilter ?? ""));
      setCreatedToFilter(String(filters.createdToFilter ?? ""));
      setDueFromFilter(String(filters.dueFromFilter ?? ""));
      setDueToFilter(String(filters.dueToFilter ?? ""));
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

  const handleExportCsv = async () => {
    setIsExporting(true);

    try {
      const csvBlob = await exportIncidentsCsv({
        status: statusFilter,
        severity: severityFilter,
        assignedToEmail: assignedToFilter,
        query: queryFilter,
      });
      const downloadUrl = URL.createObjectURL(csvBlob);
      const downloadLink = document.createElement("a");
      const timestamp = new Date().toISOString().slice(0, 19).replaceAll(":", "-");

      downloadLink.href = downloadUrl;
      downloadLink.download = `incidents-export-${timestamp}.csv`;
      document.body.appendChild(downloadLink);
      downloadLink.click();
      downloadLink.remove();
      URL.revokeObjectURL(downloadUrl);
      showMessage("Incident CSV exported", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to export incidents"), "error");
    } finally {
      setIsExporting(false);
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

        <div className="page-header-actions">
          <button className="btn-secondary" onClick={handleExportCsv} disabled={isExporting}>
            {isExporting ? "Exporting..." : "Export CSV"}
          </button>
          <button className="btn-secondary" onClick={() => loadIncidents()} disabled={isLoading}>
            Refresh
          </button>
        </div>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

      <SavedViewControls
        savedViews={savedViews}
        saveName={savedViewName}
        isSaving={isSavingView}
        placeholder="Open incidents this week"
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
            placeholder="Search title or description"
          />
        </div>

        <div className="filter-group">
          <label>Status</label>
          <select
            value={statusFilter}
            onChange={(event) => {
              setStatusFilter(event.target.value as IncidentStatus | "");
              setPage(0);
            }}
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
            onChange={(event) => {
              setSeverityFilter(event.target.value as IncidentSeverity | "");
              setPage(0);
            }}
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
            setQueryFilter("");
            setStatusFilter("");
            setSeverityFilter("");
            setAssignedToFilter("");
            setCreatedFromFilter("");
            setCreatedToFilter("");
            setDueFromFilter("");
            setDueToFilter("");
            setPage(0);
          }}
          disabled={
            !queryFilter &&
            !statusFilter &&
            !severityFilter &&
            !assignedToFilter &&
            !createdFromFilter &&
            !createdToFilter &&
            !dueFromFilter &&
            !dueToFilter
          }
        >
          Clear Filters
        </button>
      </section>

      <section className="filter-bar incident-assignee-bar">
        <div className="filter-group">
          <label>Assigned To</label>
          <input
            type="email"
            value={assignedToFilter}
            onChange={(event) => {
              setAssignedToFilter(event.target.value);
              setPage(0);
            }}
            placeholder="analyst@nis2.com"
          />
        </div>

        <button
          className="btn-secondary"
          onClick={() => {
            setAssignedToFilter(currentUser?.email ?? "");
            setPage(0);
          }}
          disabled={!currentUser?.email}
        >
          Mine
        </button>
      </section>

      <section className="filter-bar date-filter-bar">
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
        <div className="filter-group">
          <label>SLA From</label>
          <input
            type="datetime-local"
            value={dueFromFilter}
            onChange={(event) => {
              setDueFromFilter(event.target.value);
              setPage(0);
            }}
          />
        </div>
        <div className="filter-group">
          <label>SLA To</label>
          <input
            type="datetime-local"
            value={dueToFilter}
            onChange={(event) => {
              setDueToFilter(event.target.value);
              setPage(0);
            }}
          />
        </div>
      </section>

      <section className="sort-bar">
        <SortControls
          options={INCIDENT_SORT_OPTIONS}
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

      <section className="bulk-action-bar">
        <div>
          <span className="bulk-action-count">{selectedIncidentIds.size}</span>
          <span className="text-muted"> selected for bulk triage</span>
        </div>

        <div className="bulk-action-controls">
          <select
            value={bulkStatus}
            onChange={(event) => setBulkStatus(event.target.value as IncidentStatus)}
            disabled={selectedIncidentIds.size === 0 || isBulkUpdating}
          >
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {status.replace("_", " ")}
              </option>
            ))}
          </select>

          <button
            className="btn-secondary"
            onClick={handleBulkStatusUpdate}
            disabled={selectedIncidentIds.size === 0 || isBulkUpdating}
          >
            {isBulkUpdating ? "Updating..." : "Update Status"}
          </button>

          <button
            className="btn-secondary"
            onClick={() => setSelectedIncidentIds(new Set())}
            disabled={selectedIncidentIds.size === 0 || isBulkUpdating}
          >
            Clear
          </button>
        </div>
      </section>

      <section className="table-panel incident-create-panel !border-blue-100 !bg-gradient-to-br !from-white !via-sky-50 !to-emerald-50 dark:!from-slate-900 dark:!via-slate-900 dark:!to-slate-800 dark:!border-slate-700">
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <span className="inline-flex rounded-full bg-emerald-100 px-3 py-1 text-xs font-extrabold uppercase tracking-wide text-emerald-700">
              New case
            </span>
            <h3 className="mt-3 !mb-0">Report Incident</h3>
          </div>
          <span className="rounded-full bg-blue-100 px-3 py-1 text-xs font-bold text-blue-700">
            Triage intake
          </span>
        </div>
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

          <div className="form-group">
            <label>Assigned To</label>
            <input
              type="email"
              value={createForm.assignedToEmail ?? ""}
              onChange={(event) =>
                setCreateForm({ ...createForm, assignedToEmail: event.target.value })
              }
              placeholder="analyst@nis2.com"
            />
          </div>

          <div className="form-group">
            <label>SLA Due</label>
            <input
              type="datetime-local"
              value={createForm.dueAt ?? ""}
              onChange={(event) =>
                setCreateForm({ ...createForm, dueAt: event.target.value })
              }
            />
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

          <button
            className="btn-primary !w-auto !min-h-0 justify-self-start !rounded-xl !px-6 !py-3 !text-sm"
            type="submit"
            disabled={isCreating}
          >
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
                  <th className="select-column">
                    <input
                      aria-label="Select all visible incidents"
                      checked={allVisibleSelected}
                      className="table-checkbox"
                      onChange={toggleAllVisibleIncidents}
                      type="checkbox"
                    />
                  </th>
                  <th>Title</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Reported By</th>
                  <th>Reported</th>
                  <th>Assigned To</th>
                  <th>SLA Due</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {incidents.map((incident) => (
                  <tr key={incident.id}>
                    <td className="select-column">
                      <input
                        aria-label={`Select ${incident.title}`}
                        checked={selectedIncidentIds.has(incident.id)}
                        className="table-checkbox"
                        onChange={() => toggleIncidentSelection(incident.id)}
                        type="checkbox"
                      />
                    </td>
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
                    <td>{formatReportedAt(incident.createdAt)}</td>
                    <td>{incident.assignedToEmail || "Unassigned"}</td>
                    <td>
                      <span className={`sla-pill sla-${getDueStatus(incident).toLowerCase().replaceAll(" ", "-")}`}>
                        {getDueStatus(incident)}
                      </span>
                      <span className="table-description">{formatDueAt(incident.dueAt)}</span>
                    </td>
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
        <PaginationControls
          page={page}
          size={pageSize}
          totalElements={totalIncidents}
          totalPages={totalPages}
          isLoading={isLoading}
          onPageChange={(nextPage) => setPage(nextPage)}
          onSizeChange={(nextSize) => {
            setPageSize(nextSize);
            setPage(0);
          }}
        />
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

              <div className="form-group">
                <label>Assigned To</label>
                <input
                  type="email"
                  value={editForm.assignedToEmail ?? ""}
                  onChange={(event) =>
                    setEditForm({ ...editForm, assignedToEmail: event.target.value })
                  }
                  placeholder="analyst@nis2.com"
                />
              </div>

              <div className="form-group">
                <label>SLA Due</label>
                <input
                  type="datetime-local"
                  value={editForm.dueAt ?? ""}
                  onChange={(event) =>
                    setEditForm({ ...editForm, dueAt: event.target.value })
                  }
                />
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
              <h3>Activity Timeline</h3>

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

              {isLoadingTimeline ? (
                <p className="text-muted">Loading activity...</p>
              ) : incidentTimeline.length === 0 ? (
                <p className="text-muted">No activity yet.</p>
              ) : (
                <div className="notes-list">
                  {incidentTimeline.map((item) => (
                    <article
                      className={`timeline-item ${item.type === "NOTE" ? "timeline-note" : "timeline-audit"}`}
                      key={`${item.type}-${item.id}`}
                    >
                      <div className="timeline-item-header">
                        <span className="timeline-type">
                          {item.type === "NOTE" ? "Note" : formatTimelineAction(item.action)}
                        </span>
                        <span>{new Date(item.createdAt).toLocaleString()}</span>
                      </div>
                      <p>{item.type === "NOTE" ? item.note : item.details}</p>
                      <span>
                        {item.actorEmail || "System"}
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
