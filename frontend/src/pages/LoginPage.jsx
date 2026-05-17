import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

function LoginPage() {
  const [username, set_username] = useState("");
  const [password, set_password] = useState("");
  const [error, setError] = useState("");

  const navigate = useNavigate();
  const auth = useAuth();

  const handleSubmit = async () => {
    setError("");

    try {
      await auth.login(username, password);

      navigate("/dashboard");
    } catch (err) {
      if (err.response?.status === 401) {
        setError("Invalid username or password. Please try again.");
      } else {
        setError(
          "Server is unreachable. Please check your internet connection.",
        );
      }
    }
  };

  return (
    <div
      style={{
        padding: "2rem",
        display: "flex",
        flexDirection: "column",
        gap: "0.5rem",
        maxWidth: "300px",
      }}
    >
      <h2>Login</h2>

      {error && (
        <div
          style={{
            padding: "0.75rem",
            background: "#fee2e2",
            color: "#991b1b",
            border: "1px solid #fca5a5",
            borderRadius: "0.375rem",
            fontSize: "0.875rem",
            marginBottom: "0.5rem",
          }}
        >
          {error}
        </div>
      )}

      <input
        type="text"
        placeholder="Username"
        value={username}
        onChange={(e) => set_username(e.target.value)}
      />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => set_password(e.target.value)}
      />
      <button onClick={handleSubmit}>Login</button>
      <button onClick={() => navigate("/register")}>Register</button>
    </div>
  );
}

export default LoginPage;
