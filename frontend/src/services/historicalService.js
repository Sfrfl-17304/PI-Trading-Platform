import api_historical from "./historical_axios";

export async function getHistoricalPrices(symbol, start, end, interval = "1m") {
  const response = await api_historical.get("/api/historical/prices", {
    params: {
      symbol,
      start: start.toISOString(),
      end: end.toISOString(),
      interval,
    },
  });
  return response.data.data;
}
