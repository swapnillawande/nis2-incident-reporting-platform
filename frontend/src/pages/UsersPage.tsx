import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../api/errorUtils";
import { deleteUser, getAllUsers, updateUser } from "../api/userApi";
import type { RoleName, UpdateUserRequest, UserResponse, UserStatus } from "../types/user";

const ROLE_OPTIONS: RoleName[] = [
  "ADMIN",
  "SECURITY_ANALYST",
  "COMPLIANCE_OFFICER",
  "AUDITOR",
];

const STATUS_OPTIONS: UserStatus[] = ["ACTIVE", "INACTIVE", "SUSPENDED"];

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

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadUsers = async () => {
    setIsLoading(true);
    setMessage("");
    setMessageType("");

    try {
      const response = await getAllUsers();
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

    getAllUsers()
      .then((response) => {
        setUsers(response);
      })
      .catch((error: unknown) => {
        showMessage(getApiErrorMessage(error, "Failed to load users"), "error");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [isAdmin]);

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

        <button className="btn-secondary" onClick={loadUsers} disabled={isLoading}>
          Refresh
        </button>
      </section>

      {message && <div className={`message ${messageType}`}>{message}</div>}

      <section className="table-panel">
        {isLoading ? (
          <p className="text-muted">Loading users...</p>
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
