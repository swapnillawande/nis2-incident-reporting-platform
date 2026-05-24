import { useState } from "react";
import { loginUser } from "../api/userApi";

function LoginPage() {
  const [email, setEmail] = useState("admin@nis2.com");
  const [password, setPassword] = useState("Admin@123");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");
    setMessageType("");

    try {
      const response = await loginUser({ email, password });

      localStorage.setItem("user", JSON.stringify(response));

      setMessage(`Login successful. Welcome ${response.fullName}`);
      setMessageType("success");
    } catch (error: any) {
      console.error("Login error:", error);

      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        "Login failed";

      setMessage(errorMessage);
      setMessageType("error");
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2>Welcome Back</h2>
        <p className="auth-subtitle">
          Sign in to manage incidents, alerts, audit logs, and compliance reports.
        </p>

        <form onSubmit={handleLogin}>
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
              placeholder="Enter password"
            />
          </div>

          <button type="submit">Login</button>
        </form>

        {message && <div className={`message ${messageType}`}>{message}</div>}
      </div>
    </div>
  );
}

export default LoginPage;