import { useEffect, useMemo, useState } from "react";
import PaginationControls from "../components/PaginationControls";
import SavedViewControls from "../components/SavedViewControls";
import SortControls from "../components/SortControls";
import {
  createSavedView,
  deleteSavedView,
  getSavedViews,
} from "../api/savedViewApi";
import { getApiErrorMessage } from "../api/errorUtils";
import {
  createUser,
  deleteUser,
  exportUsersCsv,
  getAllUsers,
  updateUser,
} from "../api/userApi";
import type {
  CreateUserRequest,
  RoleName,
  UpdateUserRequest,
  UserResponse,
  UserStatus,
} from "../types/user";
import type { SortDirection } from "../types/pagination";
import type { SavedViewResponse } from "../types/savedView";

const ROLE_OPTIONS: RoleName[] = [
  "ADMIN",
  "SECURITY_ANALYST",
  "COMPLIANCE_OFFICER",
  "AUDITOR",
];

const STATUS_OPTIONS: UserStatus[] = ["ACTIVE", "INACTIVE", "SUSPENDED"];
const USER_SORT_OPTIONS = [
  { label: "Created", value: "createdAt" },
  { label: "Name", value: "fullName" },
  { label: "Email", value: "email" },
  { label: "Status", value: "status" },
  { label: "Updated", value: "updatedAt" },
];

const emptyCreateForm: CreateUserRequest = {
  fullName: "",
  email: "",
  password: "",
  role: "SECURITY_ANALYST",
};

function UsersPage() {
  const currentUser = useMemo(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  }, []);

  const isAdmin = currentUser?.roles?.includes("ADMIN");
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null);
  const [formData, setFormData] = useState<UpdateUserRequest | null>(null);
  const [isLoading, setIsLoading] = useState(isAdmin);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const [queryFilter, setQueryFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState<UserStatus | "">("");
  const [roleFilter, setRoleFilter] = useState<RoleName | "">("");
  const [createdFromFilter, setCreatedFromFilter] = useState("");
  const [createdToFilter, setCreatedToFilter] = useState("");
  const [createForm, setCreateForm] = useState<CreateUserRequest>(emptyCreateForm);
  const [isCreating, setIsCreating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalUsers, setTotalUsers] = useState(0);
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

    getSavedViews("USERS")
      .then(setSavedViews)
      .catch((error: unknown) => {
        setMessage(getApiErrorMessage(error, "Failed to load saved views"));
        setMessageType("error");
      });
  }, [isAdmin]);

  const loadUsers = async (targetPage = page, targetSize = pageSize) => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAllUsers({
        status: statusFilter,
        role: roleFilter,
        query: queryFilter,
        createdFrom: createdFromFilter,
        createdTo: createdToFilter,
        page: targetPage,
        size: targetSize,
        sortBy,
        sortDir,
      });
      setUsers(response.content);
      setPage(response.page);
      setPageSize(response.size);
      setTotalUsers(response.totalElements);
      setTotalPages(response.totalPages);
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to load users"), "error");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    getAllUsers({
      status: statusFilter,
      role: roleFilter,
      query: queryFilter,
      createdFrom: createdFromFilter,
      createdTo: createdToFilter,
      page,
      size: pageSize,
      sortBy,
      sortDir,
    })
      .then((response) => {
        setUsers(response.content);
        setPage(response.page);
        setPageSize(response.size);
        setTotalUsers(response.totalElements);
        setTotalPages(response.totalPages);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load users"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [
    isAdmin,
    statusFilter,
    roleFilter,
    queryFilter,
    createdFromFilter,
    createdToFilter,
    page,
    pageSize,
    sortBy,
    sortDir,
  ]);

  const openEdit = (user: UserResponse) => {
    setSelectedUser(user);
    setFormData({
      fullName: user.fullName,
      email: user.email,
      status: user.status,
      roles: user.roles,
    });
  };

  const closeEdit = () => {
    setSelectedUser(null);
    setFormData(null);
  };

  const handleRoleChange = (role: RoleName, checked: boolean) => {
    if (!formData) {
      return;
    }

    const roles = checked
      ? Array.from(new Set([...formData.roles, role]))
      : formData.roles.filter((item) => item !== role);

    setFormData({
      ...formData,
      roles: roles.length > 0 ? roles : formData.roles,
    });
  };

  const handleSave = async () => {
    if (!selectedUser || !formData) {
      return;
    }

    try {
      await updateUser(selectedUser.id, formData);
      closeEdit();
      await loadUsers(page, pageSize);
      showMessage("User updated successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to update user"), "error");
    }
  };

  const handleDelete = async (user: UserResponse) => {
    const confirmed = window.confirm(`Delete ${user.fullName}?`);

    if (!confirmed) {
      return;
    }

    try {
      await deleteUser(user.id);
      const nextPage = users.length === 1 && page > 0 ? page - 1 : page;
      await loadUsers(nextPage, pageSize);
      showMessage("User deleted successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to delete user"), "error");
    }
  };

  const handleCreate = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsCreating(true);

    try {
      await createUser(createForm);
      setCreateForm(emptyCreateForm);
      await loadUsers(0, pageSize);
      showMessage("User created successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to create user"), "error");
    } finally {
      setIsCreating(false);
    }
  };

  const handleExportCsv = async () => {
    setIsExporting(true);
    setMessage("");
    setMessageType("");

    try {
      const blob = await exportUsersCsv({
        status: statusFilter,
        role: roleFilter,
        query: queryFilter,
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      const timestamp = new Date().toISOString().slice(0, 19).replaceAll(":", "-");

      link.href = url;
      link.download = `users-export-${timestamp}.csv`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      showMessage("Users exported successfully", "success");
    } catch (error: unknown) {
      showMessage(getApiErrorMessage(error, "Failed to export users"), "error");
    } finally {
      setIsExporting(false);
    }
  };

  const buildSavedViewFilterJson = () =>
    JSON.stringify({
      queryFilter,
      statusFilter,
      roleFilter,
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
        viewType: "USERS",
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

      setQueryFilter(String(filters.queryFilter ?? ""));
      setStatusFilter((filters.statusFilter as UserStatus | "") ?? "");
      setRoleFilter((filters.roleFilter as RoleName | "") ?? "");
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

  if (!isAdmin) {
    return (
      <div className="page-container">
        <section className="dashboard-hero">
          <span className="badge">Users</span>
          <h1 style={{ marginTop: "18px" }}>Admin Access Required</h1>
          <p className="page-subtitle">
            User management is available only for ADMIN accounts.
          </p>
        </section>
      </div>
    );
  }

  return (
    <div className="page-container">
      <section className="page-header">
        <div>
          <span className="badge">Access Control</span>
          <h1>User Management</h1>
          <p className="page-subtitle">
            Review platform users, adjust roles, update account status, and remove access.
          </p>
        </div>

        <div className="page-header-actions">
          <button
            className="btn-secondary"
            onClick={handleExportCsv}
            disabled={isExporting}
          >
            {isExporting ? "Exporting..." : "Export CSV"}
          </button>

          <button className="btn-secondary" onClick={() => loadUsers()} disabled={isLoading}>
            Refresh
          </button>
        </div>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

      <SavedViewControls
        savedViews={savedViews}
        saveName={savedViewName}
        isSaving={isSavingView}
        placeholder="Active auditors"
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
            placeholder="Search name or email"
          />
        </div>

        <div className="filter-group">
          <label>Status</label>
          <select
            value={statusFilter}
            onChange={(event) => {
              setStatusFilter(event.target.value as UserStatus | "");
              setPage(0);
            }}
          >
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Role</label>
          <select
            value={roleFilter}
            onChange={(event) => {
              setRoleFilter(event.target.value as RoleName | "");
              setPage(0);
            }}
          >
            <option value="">All roles</option>
            {ROLE_OPTIONS.map((role) => (
              <option key={role} value={role}>
                {role.replace("_", " ")}
              </option>
            ))}
          </select>
        </div>

        <button
          className="btn-secondary"
          onClick={() => {
            setQueryFilter("");
            setStatusFilter("");
            setRoleFilter("");
            setCreatedFromFilter("");
            setCreatedToFilter("");
            setPage(0);
          }}
          disabled={
            !queryFilter &&
            !statusFilter &&
            !roleFilter &&
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
          options={USER_SORT_OPTIONS}
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

      <section className="table-panel incident-create-panel !border-blue-100 !bg-gradient-to-br !from-white !via-sky-50 !to-emerald-50 dark:!from-slate-900 dark:!via-slate-900 dark:!to-slate-800 dark:!border-slate-700">
        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <span className="inline-flex rounded-full bg-emerald-100 px-3 py-1 text-xs font-extrabold uppercase tracking-wide text-emerald-700">
              New access
            </span>
            <h3 className="mt-3 !mb-0">Create User</h3>
          </div>
          <span className="rounded-full bg-blue-100 px-3 py-1 text-xs font-bold text-blue-700">
            Admin only
          </span>
        </div>

        <form className="incident-form" onSubmit={handleCreate}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              value={createForm.fullName}
              onChange={(event) =>
                setCreateForm({ ...createForm, fullName: event.target.value })
              }
              placeholder="Security Analyst"
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={createForm.email}
              onChange={(event) =>
                setCreateForm({ ...createForm, email: event.target.value })
              }
              placeholder="analyst@nis2.com"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={createForm.password}
              onChange={(event) =>
                setCreateForm({ ...createForm, password: event.target.value })
              }
              placeholder="Minimum 8 characters"
            />
          </div>

          <div className="form-group">
            <label>Role</label>
            <select
              value={createForm.role}
              onChange={(event) =>
                setCreateForm({ ...createForm, role: event.target.value as RoleName })
              }
            >
              {ROLE_OPTIONS.map((role) => (
                <option key={role} value={role}>
                  {role.replace("_", " ")}
                </option>
              ))}
            </select>
          </div>

          <button
            className="btn-primary !w-auto !min-h-0 justify-self-start !rounded-xl !px-6 !py-3 !text-sm"
            type="submit"
            disabled={isCreating}
          >
            {isCreating ? "Creating..." : "Create User"}
          </button>
        </form>
      </section>

      <section className="table-panel">
        {isLoading ? (
          <p className="text-muted">Loading users...</p>
        ) : users.length === 0 ? (
          <p className="text-muted">No users match the selected filters.</p>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Roles</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <strong>{user.fullName}</strong>
                    </td>
                    <td>{user.email}</td>
                    <td>{user.roles.join(", ")}</td>
                    <td>
                      <span className={`status-pill status-${user.status.toLowerCase()}`}>
                        {user.status}
                      </span>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="btn-secondary compact" onClick={() => openEdit(user)}>
                          Edit
                        </button>
                        <button className="btn-danger compact" onClick={() => handleDelete(user)}>
                          Delete
                        </button>
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
          totalElements={totalUsers}
          totalPages={totalPages}
          isLoading={isLoading}
          onPageChange={(nextPage) => setPage(nextPage)}
          onSizeChange={(nextSize) => {
            setPageSize(nextSize);
            setPage(0);
          }}
        />
      </section>

      {selectedUser && formData && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" role="dialog" aria-modal="true">
            <div className="modal-header">
              <div>
                <span className="badge">Edit User</span>
                <h2>{selectedUser.fullName}</h2>
              </div>
              <button className="icon-close" onClick={closeEdit} aria-label="Close">
                x
              </button>
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label>Full Name</label>
                <input
                  value={formData.fullName}
                  onChange={(event) =>
                    setFormData({ ...formData, fullName: event.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(event) =>
                    setFormData({ ...formData, email: event.target.value })
                  }
                />
              </div>

              <div className="form-group">
                <label>Status</label>
                <select
                  value={formData.status}
                  onChange={(event) =>
                    setFormData({
                      ...formData,
                      status: event.target.value as UserStatus,
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
            </div>

            <div className="role-list">
              {ROLE_OPTIONS.map((role) => (
                <label key={role} className="role-option">
                  <input
                    type="checkbox"
                    checked={formData.roles.includes(role)}
                    onChange={(event) => handleRoleChange(role, event.target.checked)}
                  />
                  <span>{role}</span>
                </label>
              ))}
            </div>

            <div className="modal-actions">
              <button className="btn-secondary" onClick={closeEdit}>
                Cancel
              </button>
              <button className="btn-primary" onClick={handleSave}>
                Save Changes
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  );
}

export default UsersPage;
