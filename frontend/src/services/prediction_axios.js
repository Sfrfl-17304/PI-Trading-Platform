import axios from "axios";

const api_prediction = axios.create({
  baseURL: "http://localhost:8085",
});

export default api_prediction;
