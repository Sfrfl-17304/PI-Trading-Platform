import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function ProtectedRoute({ children }) {
  const auth = useAuth();

  if (auth.isLoading) {
    return (
      <div style={{ padding: "2rem", color: "#e2e8f0" }}>
        Loading session...
      </div>
    );
  }

  if (auth.token) return <>{children}</>;
  else return <Navigate to="/login" />;
}

export default ProtectedRoute;
