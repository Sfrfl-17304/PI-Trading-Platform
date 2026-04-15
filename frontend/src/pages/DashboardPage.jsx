import { useEffect, useRef, useState } from "react";
import { createChart, LineSeries } from "lightweight-charts";
import api_market from "../services/market_axios";

function DashboardPage() {
  const [prices, setPrices] = useState([]);
  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);
  const lineSeriesRef = useRef(null);

  // Build chart once
  useEffect(() => {
    if (!chartContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 400,
      layout: {
        background: { color: "#0f172a" },
        textColor: "#e2e8f0",
      },
      grid: {
        vertLines: { color: "#1e293b" },
        horzLines: { color: "#1e293b" },
      },
      rightPriceScale: {
        borderColor: "#334155",
      },
      timeScale: {
        borderColor: "#334155",
        timeVisible: true,
        secondsVisible: true,
      },
    });

    const lineSeries = chart.addSeries(LineSeries,{
      color: "#22c55e",
      lineWidth: 2,
    });

    chartRef.current = chart;
    lineSeriesRef.current = lineSeries;

    const handleResize = () => {
      if (!chartContainerRef.current || !chartRef.current) return;
      chartRef.current.applyOptions({
        width: chartContainerRef.current.clientWidth,
      });
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.remove();
      chartRef.current = null;
      lineSeriesRef.current = null;
    };
  }, []);

  // Poll latest prices and update both list + chart
  useEffect(() => {
    const fetchPrice = async () => {
      try {
        const response = await api_market.get("prices/latest");
        const latest = response.data;
        setPrices(latest);

        if (!lineSeriesRef.current || !latest?.length) return;

        // Pick one asset to chart
        const primaryAsset = latest[0];
        const value = Number(primaryAsset.price);
        if (Number.isNaN(value)) return;

        lineSeriesRef.current.update({
          time: Math.floor(Date.now() / 1000), // unix seconds
          value,
        });
      } catch (error) {
        console.error("Error fetching price:", error);
      }
    };

    fetchPrice();
    const intervalId = setInterval(fetchPrice, 5000);
    return () => clearInterval(intervalId);
  }, []);

  return (
    <div style={{ padding: "1rem" }}>
      <h1 style={{ marginBottom: "1rem" }}>Live prices</h1>

      <div
        ref={chartContainerRef}
        style={{
          width: "100%",
          maxWidth: "1000px",
          marginBottom: "1.5rem",
          borderRadius: "0.75rem",
          overflow: "hidden",
          border: "1px solid #1e293b",
        }}
      />

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(180px, 1fr))",
          gap: "0.75rem",
        }}
      >
        {prices?.length ? (
          prices.map((asset) => (
            <div
              key={`${asset.source}-${asset.symbol}`}
              style={{
                background: "#0f172a",
                color: "#e2e8f0",
                border: "1px solid #1e293b",
                borderRadius: "0.75rem",
                padding: "0.75rem",
              }}
            >
              <small style={{ color: "#94a3b8" }}>{asset.source}</small>
              <h3 style={{ margin: "0.3rem 0" }}>{asset.symbol}</h3>
              <p style={{ margin: 0, fontSize: "1.05rem", fontWeight: 600 }}>
                $
                {Number(asset.price).toLocaleString(undefined, {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
              </p>
            </div>
          ))
        ) : (
          <p>Loading market data...</p>
        )}
      </div>
    </div>
  );
}

export default DashboardPage;
