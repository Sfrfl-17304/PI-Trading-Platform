import { useEffect } from "react";
import { useState } from "react";
import {
  cancelOrder,
  createOrder,
  getUserOrders,
} from "../services/orderService";

function OrdersPage() {
  const [orders, set_orders] = useState([]);
  const [symbol, set_symbol] = useState("BTCUSDT");
  const [type, set_type] = useState("BUY");
  const [quantity, set_quantity] = useState("");
  const [stop_loss, set_stop_loss] = useState("");
  const [take_profit, set_take_profit] = useState("");
  const [error, set_error] = useState("");
  const [status_filter, set_status_filter] = useState("OPEN");

  const fetchOrders = async () => {
    try {
      set_error("");
      const data = await getUserOrders(status_filter);
      set_orders(data || []);
    } catch (err) {
      set_error(err.message || "Failed to load orders.");
    }
  };

  useEffect(() => {
    fetchOrders(status_filter);
  }, [status_filter]);

  const handleCreateOrder = async (e) => {
    e.preventDefault();

    set_error("");

    if (!quantity) {
      set_error("Quantity is required.");
      return;
    }

    try {
      await createOrder({
        symbol,
        type,
        quantity: parseFloat(quantity),
        stopLoss: stop_loss ? parseFloat(stop_loss) : null,
        takeProfit: take_profit ? parseFloat(take_profit) : null,
      });

      set_quantity("");
      set_stop_loss("");
      set_take_profit("");
      await fetchOrders();
    } catch (err) {
      set_error(err.message || "Failed to create order.");
    }
  };

  const handleCancelOrder = async (orderId) => {
    try {
      set_error("");
      await cancelOrder(orderId);
      await fetchOrders();
    } catch (err) {
      set_error(err.message || "Failed to cancel order.");
    }
  };

  return (
    <div style={{ padding: "1.5rem", color: "#e2e8f0" }}>
      <h1 style={{ marginBottom: "1.5rem" }}>Orders</h1>

      <div
        style={{
          background: "#0f172a",
          border: "1px solid #1e293b",
          borderRadius: "0.75rem",
          padding: "1.25rem",
          maxWidth: "500px",
          marginBottom: "2rem",
        }}
      >
        <h2 style={{ marginBottom: "1rem", fontSize: "1.1rem" }}>New Order</h2>
        {error && (
          <div
            style={{
              padding: "0.75rem",
              background: "#fee2e2",
              color: "#991b1b",
              borderRadius: "0.375rem",
              marginBottom: "1rem",
              fontSize: "0.875rem",
            }}
          >
            {error}
          </div>
        )}
        <form
          onSubmit={handleCreateOrder}
          style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}
        >
          <select
            value={symbol}
            onChange={(e) => set_symbol(e.target.value)}
            style={{
              padding: "0.5rem",
              borderRadius: "0.375rem",
              background: "#1e293b",
              color: "#e2e8f0",
              border: "1px solid #334155",
            }}
          >
            <option value="BTCUSDT">BTCUSDT</option>
            <option value="ETHUSDT">ETHUSDT</option>
            <option value="SPY">SPY</option>
          </select>

          <select
            value={type}
            onChange={(e) => set_type(e.target.value)}
            style={{
              padding: "0.5rem",
              borderRadius: "0.375rem",
              background: "#1e293b",
              color: "#e2e8f0",
              border: "1px solid #334155",
            }}
          >
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>

          <input
            type="number"
            placeholder="Quantity"
            value={quantity}
            onChange={(e) => set_quantity(e.target.value)}
            min="0"
            step="any"
            required
            style={{
              padding: "0.5rem",
              borderRadius: "0.375rem",
              background: "#1e293b",
              color: "#e2e8f0",
              border: "1px solid #334155",
            }}
          />

          <input
            type="number"
            placeholder="Stop Loss (optional)"
            value={stop_loss}
            onChange={(e) => set_stop_loss(e.target.value)}
            min="0"
            step="any"
            style={{
              padding: "0.5rem",
              borderRadius: "0.375rem",
              background: "#1e293b",
              color: "#e2e8f0",
              border: "1px solid #334155",
            }}
          />

          <input
            type="number"
            placeholder="Take Profit (optional)"
            value={take_profit}
            onChange={(e) => set_take_profit(e.target.value)}
            min="0"
            step="any"
            style={{
              padding: "0.5rem",
              borderRadius: "0.375rem",
              background: "#1e293b",
              color: "#e2e8f0",
              border: "1px solid #334155",
            }}
          />

          <button
            type="submit"
            style={{
              padding: "0.6rem",
              borderRadius: "0.375rem",
              background: "#22c55e",
              color: "#fff",
              border: "none",
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            Place Order
          </button>
        </form>
      </div>

      <div>
        <div style={{ display: "flex", gap: "0.5rem", marginBottom: "1rem" }}>
          {["OPEN", "CLOSED"].map((s) => (
            <button
              key={s}
              onClick={() => set_status_filter(s)}
              style={{
                padding: "0.4rem 1rem",
                borderRadius: "0.375rem",
                border: "1px solid #334155",
                cursor: "pointer",
                background: status_filter === s ? "#3b82f6" : "#1e293b",
                color: "#e2e8f0",
                fontWeight: status_filter === s ? 700 : 400,
              }}
            >
              {s}
            </button>
          ))}
        </div>

        {orders.length === 0 ? (
          <p style={{ color: "#94a3b8" }}>
            No {status_filter.toLowerCase()} orders.
          </p>
        ) : (
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              gap: "0.75rem",
              maxWidth: "700px",
            }}
          >
            {orders.map((order) => (
              <div
                key={order.id}
                style={{
                  background: "#0f172a",
                  border: "1px solid #1e293b",
                  borderRadius: "0.75rem",
                  padding: "1rem",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "0.25rem",
                  }}
                >
                  <div style={{ fontWeight: 700 }}>
                    <span
                      style={{
                        color: order.type === "BUY" ? "#22c55e" : "#ef4444",
                      }}
                    >
                      {order.type}
                    </span>{" "}
                    {order.symbol}
                  </div>
                  <div style={{ fontSize: "0.875rem", color: "#94a3b8" }}>
                    Qty: {order.quantity} {order.price && `@ $${order.price}`}
                  </div>
                  {order.stopLoss && (
                    <div style={{ fontSize: "0.8rem", color: "#f59e0b" }}>
                      SL: ${order.stopLoss}
                    </div>
                  )}
                  {order.takeProfit && (
                    <div style={{ fontSize: "0.8rem", color: "#22c55e" }}>
                      TP: ${order.takeProfit}
                    </div>
                  )}
                  <div style={{ fontSize: "0.75rem", color: "#64748b" }}>
                    {new Date(order.createdAt).toLocaleString()}
                  </div>
                </div>
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "flex-end",
                    gap: "0.5rem",
                  }}
                >
                  <span
                    style={{
                      fontSize: "0.8rem",
                      padding: "0.2rem 0.6rem",
                      borderRadius: "9999px",
                      background:
                        order.status === "OPEN" ? "#064e3b" : "#1e293b",
                      color: order.status === "OPEN" ? "#34d399" : "#94a3b8",
                    }}
                  >
                    {order.status}
                  </span>
                  {order.status === "OPEN" && (
                    <button
                      onClick={() => handleCancelOrder(order.id)}
                      style={{
                        padding: "0.3rem 0.75rem",
                        borderRadius: "0.375rem",
                        background: "#7f1d1d",
                        color: "#fca5a5",
                        border: "none",
                        cursor: "pointer",
                        fontSize: "0.8rem",
                      }}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default OrdersPage;
