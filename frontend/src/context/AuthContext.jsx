import { useContext, useState } from "react";
import api_user from "../services/user_axios";
import { createContext } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => {
    return localStorage.getItem("token") || null;
  });

  const [username, setUsername] = useState(() => {
    return localStorage.getItem("username") || null;
  });

  const [isLoading, setIsLoading] = useState(false);

  async function login(username, password) {
    // Turn true immediately to freeze the UI and show a loading spinner
    setIsLoading(true);
    try {
      const response = await api_user.post("/api/auth/login", {
        username,
        password,
      });
      if (!response || !response.data) {
        throw new Error("No data received from login server");
      }

      const token = response.data.accessToken;

      localStorage.setItem("token", token);

      const set_username = await api_user.get("/api/users/me", {
        headers: { Authorization: "Bearer " + token },
      });

      localStorage.setItem("username", set_username.data.username);

      setToken(token);
      setUsername(set_username.data.username);

      return true;
    } catch (error) {
      console.error("Login sequence failed:", error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("username");

    setToken(null);
    setUsername(null);
  }

  const value = {
    token,
    username,
    isLoading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
