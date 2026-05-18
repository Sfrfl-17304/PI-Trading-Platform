import api_user from "./user_axios";

export async function register(username, email, password) {
  const response = await api_user.post("/api/auth/register", {
    username,
    email,
    password,
  });

  return response.data;
}
