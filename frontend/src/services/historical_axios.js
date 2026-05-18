import axios from "axios";

const api_historical = axios.create({
  baseURL: "http://localhost:8087",
});

export default api_historical;
