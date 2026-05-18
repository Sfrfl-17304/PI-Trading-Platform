import api_prediction from "./prediction_axios";

export async function getPrediction(symbol) {
  const response = await api_prediction.get(`/api/v1/predict/${symbol}`);
  return response.data;
}
