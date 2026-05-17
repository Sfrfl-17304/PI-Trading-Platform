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

  async function login(username, password) {
    const response = await api_user.post("/api/auth/login", { username, password });
    if (!response) return "No login found";

    const token = response.data.accessToken;

    localStorage.setItem("token", token);

    const set_username = await api_user.get("/api/users/me", {
      headers: { Authorization: "Bearer " + token },
    });

    localStorage.setItem("username", set_username.data.username);

    setToken(token);
    setUsername(set_username.data.username);
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
