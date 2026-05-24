import { useState } from "react";
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
    } catch (error: any) {
      console.error("Registration error:", error);

      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        "Registration failed";

      setMessage(errorMessage);
      setMessageType("error");
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Create Account</h2>
        <p className="auth-subtitle">
          Register a platform user for NIS2 incident monitoring and reporting.
        </p>

        <form onSubmit={handleRegister}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Enter full name"
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@company.com"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimum 8 characters"
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

        {message && <div className={`message ${messageType}`}>{message}</div>}
      </div>
    </div>
  );
}

export default RegisterPage;