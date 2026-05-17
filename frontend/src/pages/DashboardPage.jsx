import { useEffect, useMemo, useRef, useState } from "react";
import { createChart, LineSeries } from "lightweight-charts";
import api_market from "../services/market_axios";
import { getHistoricalPrices } from "../services/historicalService";
import { getPrediction } from "../services/predictionService";

function DashboardPage() {
  const [prices, setPrices] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState("BTCUSDT");

  const [prediction, setPrediction] = useState(null);
  const [predictionLoading, setPredictionLoading] = useState(false);

  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);
  const lineSeriesRef = useRef(null);

  const symbols = useMemo(() => {
    const unique = new Set(prices.map((p) => p.symbol).filter(Boolean));
    return Array.from(unique).sort();
  }, [prices]);

  const activeSymbol = symbols.includes(selectedSymbol)
    ? selectedSymbol
    : symbols[0] || selectedSymbol;

  useEffect(() => {
    setPrediction(null);
    setPredictionLoading(true);
    getPrediction(activeSymbol)
      .then((signal) => setPrediction(signal))
      .catch((err) => console.error("Prediction error:", err))
      .finally(() => setPredictionLoading(false));
  }, [activeSymbol]);

  // Build chart once
  useEffect(() => {
    if (!chartContainerRef.current) return;

    const chart = createChart(chartContainerRef.current, {
      width: chartContainerRef.current.clientWidth,
      height: 380,
      layout: {
        background: { color: "#0f172a" },
        textColor: "#64748b",
      },
      grid: {
        vertLines: { color: "#1e293b" },
        horzLines: { color: "#1e293b" },
      },
      rightPriceScale: { borderColor: "#1e293b" },
      timeScale: {
        borderColor: "#1e293b",
        timeVisible: true,
        secondsVisible: false,
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
          .map((p) => ({
            time: Math.floor(p.timestamp / 1000),
            value: p.price,
          }))
          .sort((a, b) => a.time - b.time);
        lineSeriesRef.current.setData(data);
      })
      .catch((err) => console.error("Historical fetch error:", err));
  }, [activeSymbol]);

  // Poll live prices
  useEffect(() => {
    let isCurrent = true;

    const fetchPrice = async () => {
      try {
        const response = await api_market.get("prices/latest");
        const latest = response.data;
        if (!isCurrent) return;

        setPrices(latest);
        if (!lineSeriesRef.current || !latest?.length) return;

        const primaryAsset = latest.find((a) => a.symbol === activeSymbol);
        if (!primaryAsset) return;

        const value = Number(primaryAsset.price);
        if (Number.isNaN(value)) return;

        lineSeriesRef.current.update({
          time: Math.floor(Date.now() / 1000),
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

  const selectedPrice = prices.find((p) => p.symbol === activeSymbol);

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-semibold text-white">Market</h1>
        <span className="flex items-center gap-1.5 text-xs text-slate-500">
          <span className="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse" />
          Live · every 5s
        </span>
      </div>

      {/* Symbol cards */}
      <div className="flex gap-2 flex-wrap mb-5">
        {prices?.length ? (
          prices.map((asset) => (
            <button
              key={`${asset.source}-${asset.symbol}`}
              onClick={() => setSelectedSymbol(asset.symbol)}
              className={`text-left border rounded-xl px-4 py-3 transition-all cursor-pointer min-w-32.5 ${
                asset.symbol === activeSymbol
                  ? "bg-slate-800 border-green-500/40"
                  : "bg-slate-900 border-slate-800 hover:border-slate-700"
              }`}
            >
              <p className="text-slate-500 text-xs">{asset.symbol}</p>
              <p className="text-white font-semibold text-sm mt-0.5">
                $
                {Number(asset.price).toLocaleString(undefined, {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
              </p>
            </button>
          ))
        ) : (
          <p className="text-slate-500 text-sm">Loading market data...</p>
        )}
      </div>

      {/* Selected price hero */}
      {selectedPrice && (
        <div className="flex items-start gap-8 mb-3">
          <div>
            <p className="text-slate-500 text-xs mb-1">{activeSymbol}</p>
            <span className="text-3xl font-bold text-white">
              ${Number(selectedPrice.price).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </span>
          </div>

          <div className="border-l border-slate-800 pl-8">
            <p className="text-slate-500 text-xs mb-1">AI Signal</p>
            {predictionLoading ? (
              <p className="text-slate-600 text-sm">Analysing...</p>
            ) : prediction ? (
              <div className="flex items-baseline gap-2">
                <span className={`text-2xl font-bold ${
                  prediction.action === "BUY" ? "text-green-400" :
                  prediction.action === "SELL" ? "text-red-400" :
                  "text-amber-400"
                }`}>
                  {prediction.action}
                </span>
                <span className="text-slate-400 text-sm">
                  {Math.round(prediction.confidence * 100)}% confidence
                </span>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Chart */}
      <div
        ref={chartContainerRef}
        className="w-full rounded-xl overflow-hidden border border-slate-800"
      />
    </div>
  );
}

export default DashboardPage;
