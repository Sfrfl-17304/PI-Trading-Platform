import { useEffect } from "react";
import { useState } from "react";
import api_market from "../services/market_axios";

function DashboardPage() {
  const [prices, setPrices] = useState([]);

  useEffect(() => {
    const fetchPrice = async () => {
      try {
        const response = await api_market.get("prices/latest");
        setPrices(response.data);
      } catch (error) {
        console.error("Error fetching price:", error);
      }
    };

    fetchPrice();
    const intervalId = setInterval(fetchPrice, 5000);
    return () => clearInterval(intervalId);
  }, []);

  return (
    <div>
      <h1>Latest Price: {prices ? JSON.stringify(prices) : "Loading..."}</h1>
    </div>
  );
}

export default DashboardPage;
