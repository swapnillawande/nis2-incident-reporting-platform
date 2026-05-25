import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../api/userApi";

function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("admin@nis2.com");
  const [password, setPassword] = useState("Admin@123");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    setMessage("");
    setMessageType("");

    try {
      const response = await loginUser({
        email,
        password,
      });

      localStorage.setItem("user", JSON.stringify(response));

      setMessage(`Login successful. Welcome ${response.fullName}`);
      setMessageType("success");

      setTimeout(() => {
        navigate("/");
      }, 700);
    } catch (error: any) {
      console.error("Login error:", error);

      const backendData = error.response?.data;

      let errorMessage = "Login failed";

      if (backendData?.validationErrors) {
        errorMessage = Object.values(backendData.validationErrors).join("\n");
      } else if (backendData?.message) {
        errorMessage = backendData.message;
      } else if (backendData?.error) {
        errorMessage = backendData.error;
      } else if (error.message) {
        errorMessage = error.message;
      }

      setMessage(errorMessage);
      setMessageType("error");
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-info">
        <h1>NIS2 Incident Reporting Platform</h1>
        <p>
          Monitor security events, manage incidents, protect evidence trails,
          and generate compliance-ready reports from one full-stack platform.
        </p>

        <div className="auth-info-badges">
          <span className="badge">Security Events</span>
          <span className="badge">Incident Reports</span>
          <span className="badge">Audit Ready</span>
        </div>
      </div>

      <div className="auth-card">
        <h2>Welcome Back</h2>

        <p className="auth-subtitle">
          Sign in to continue monitoring alerts, incidents, assets, and reports.
        </p>

        <form onSubmit={handleLogin}>
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
              placeholder="Enter password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button type="submit">Login</button>
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

export default LoginPage;