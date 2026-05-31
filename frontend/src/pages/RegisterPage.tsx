import { useState } from "react";
import { getApiErrorMessage } from "../api/errorUtils";
import { registerUser } from "../api/userApi";
import type { RoleName } from "../types/user";

function RegisterPage() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<RoleName>("ADMIN");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    setMessage("");
    setMessageType("");

    try {
      const response = await registerUser({
        fullName,
        email,
        password,
        role,
      });

      setMessage(`User registered successfully: ${response.email}`);
      setMessageType("success");

      setFullName("");
      setEmail("");
      setPassword("");
      setRole("ADMIN");
    } catch (error: unknown) {
      console.error("Registration error:", error);

      const errorMessage = getApiErrorMessage(error, "Registration failed");

      setMessage(errorMessage);
      setMessageType("error");
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-info">
        <h1>NIS2 Incident Reporting Platform</h1>
        <p>
          Create user access for security monitoring, incident response, and
          compliance reporting workflows.
        </p>

        <div className="auth-info-badges">
          <span className="badge">Role Based Access</span>
          <span className="badge">JWT Security</span>
          <span className="badge">Compliance Workflow</span>
        </div>
      </div>

      <div className="auth-card">
        <h2>Create Account</h2>

        <p className="auth-subtitle">
          Register a platform user to access incident tracking, audit logs, and
          compliance reporting.
        </p>

        <form onSubmit={handleRegister}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              placeholder="Enter full name"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="admin@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="Minimum 8 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Role</label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value as RoleName)}
            >
              <option value="ADMIN">ADMIN</option>
              <option value="SECURITY_ANALYST">SECURITY_ANALYST</option>
              <option value="COMPLIANCE_OFFICER">COMPLIANCE_OFFICER</option>
              <option value="AUDITOR">AUDITOR</option>
            </select>
          </div>

          <button type="submit">Register</button>
        </form>

        {message && (
          <div className={`message ${messageType}`}>
            {message.split("\n").map((line, index) => (
              <p key={index}>{line}</p>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default RegisterPage;
