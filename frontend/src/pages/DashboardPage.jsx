import { useEffect, useMemo, useRef, useState } from "react";
import { createChart, LineSeries } from "lightweight-charts";
import api_market from "../services/market_axios";
import { getHistoricalPrices } from "../services/historicalService";

function DashboardPage() {
  const [prices, setPrices] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState("BTCUSDT");

  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);
  const lineSeriesRef = useRef(null);

  // Build symbol options from current prices
  const symbols = useMemo(() => {
    const unique = new Set(prices.map((p) => p.symbol).filter(Boolean));
    return Array.from(unique).sort();
  }, [prices]);

  // Keep a valid selected symbol
  const activeSymbol = symbols.includes(selectedSymbol)
    ? selectedSymbol
    : symbols[0] || selectedSymbol;

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

    const lineSeries = chart.addSeries(LineSeries, {
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

  // Load historical data when symbol changes
  useEffect(() => {
    if (!lineSeriesRef.current) return;
    lineSeriesRef.current.setData([]);

    const end = new Date();
    const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);

    getHistoricalPrices(activeSymbol, start, end)
      .then((points) => {
        if (!lineSeriesRef.current || !points?.length) return;
        const data = points
          .map((p) => ({ time: Math.floor(p.timestamp / 1000), value: p.price }))
          .sort((a, b) => a.time - b.time);
        lineSeriesRef.current.setData(data);
      })
      .catch((err) => console.error("Historical fetch error:", err));
  }, [activeSymbol]);

  // Poll latest prices and update both list + chart
  useEffect(() => {
    let isCurrent = true;

    const fetchPrice = async () => {
      try {
        const response = await api_market.get("prices/latest");
        const latest = response.data;

        // 2. If the user changed symbols while this request was running, drop it
        if (!isCurrent) return;

        setPrices(latest);

        if (!lineSeriesRef.current || !latest?.length) return;

        // Pick one asset to chart
        const primaryAsset = latest.find((a) => a.symbol === activeSymbol);
        if (!primaryAsset) return;

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
    return () => {
      isCurrent = false;
      clearInterval(intervalId);
    };
  }, [activeSymbol]);

  return (
    <div style={{ padding: "1rem" }}>
      <h1 style={{ marginBottom: "1rem" }}>Live prices</h1>

      <div style={{ marginBottom: "0.75rem" }}>
        <label htmlFor="symbol-select" style={{ marginRight: "0.5rem" }}>
          Currency:
        </label>
        <select
          id="symbol-select"
          value={activeSymbol}
          onChange={(e) => setSelectedSymbol(e.target.value)}
          disabled={!symbols.length}
          style={{
            padding: "0.4rem 0.6rem",
            borderRadius: "0.5rem",
            border: "1px solid #334155",
            background: "#0f172a",
            color: "#e2e8f0",
          }}
        >
          {!symbols.length ? (
            <option value="">Loading...</option>
          ) : (
            symbols.map((sym) => (
              <option key={sym} value={sym}>
                {sym}
              </option>
            ))
          )}
        </select>
      </div>
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
