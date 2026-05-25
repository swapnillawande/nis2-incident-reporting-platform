import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const userData = localStorage.getItem("user");

  if (!userData) {
    return <Navigate to="/login" replace />;
  }

  const user = JSON.parse(userData);

  if (!user.token) {
    localStorage.removeItem("user");
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

export default ProtectedRoute;