import { useEffect, useMemo, useState } from "react";
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

const ROLE_OPTIONS: RoleName[] = [
  "ADMIN",
  "SECURITY_ANALYST",
  "COMPLIANCE_OFFICER",
  "AUDITOR",
];

const STATUS_OPTIONS: UserStatus[] = ["ACTIVE", "INACTIVE", "SUSPENDED"];

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
  const [createForm, setCreateForm] = useState<CreateUserRequest>(emptyCreateForm);
  const [isCreating, setIsCreating] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadUsers = async () => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAllUsers({
        status: statusFilter,
        role: roleFilter,
        query: queryFilter,
      });
      setUsers(response);
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
    })
      .then((response) => {
        setUsers(response);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load users"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isAdmin, statusFilter, roleFilter, queryFilter]);

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
      const response = await updateUser(selectedUser.id, formData);
      setUsers((currentUsers) =>
        currentUsers.map((user) => (user.id === response.id ? response : user))
      );
      closeEdit();
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
      setUsers((currentUsers) =>
        currentUsers.filter((currentUser) => currentUser.id !== user.id)
      );
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
      await loadUsers();
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

          <button className="btn-secondary" onClick={loadUsers} disabled={isLoading}>
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
            placeholder="Search name or email"
          />
        </div>

        <div className="filter-group">
          <label>Status</label>
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as UserStatus | "")}
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
            onChange={(event) => setRoleFilter(event.target.value as RoleName | "")}
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
          }}
          disabled={!queryFilter && !statusFilter && !roleFilter}
        >
          Clear Filters
        </button>
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
