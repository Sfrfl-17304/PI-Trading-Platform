import { useEffect, useState } from "react";
import { cancelOrder, createOrder, getUserOrders } from "../services/orderService";

const SYMBOLS = ["BTCUSDT", "ETHUSDT", "SPY"];

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
    fetchOrders();
  }, [status_filter]);

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    set_error("");
    if (!quantity) { set_error("Quantity is required."); return; }

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

  const inputClass =
    "w-full bg-slate-800/60 border border-slate-700 text-slate-200 placeholder-slate-500 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-green-500/70 focus:bg-slate-800 transition-all";

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      <h1 className="text-xl font-semibold text-white mb-6">Orders</h1>

      <div className="grid grid-cols-1 lg:grid-cols-[360px_1fr] gap-8 items-start">
        {/* New order form */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider mb-5">
            New Order
          </h2>

          {error && (
            <div className="bg-red-950/60 border border-red-900 text-red-300 text-sm rounded-lg px-4 py-3 mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleCreateOrder} className="flex flex-col gap-3">
            {/* Symbol */}
            <select value={symbol} onChange={(e) => set_symbol(e.target.value)} className={inputClass}>
              {SYMBOLS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>

            {/* BUY / SELL toggle */}
            <div className="flex rounded-lg overflow-hidden border border-slate-700">
              <button
                type="button"
                onClick={() => set_type("BUY")}
                className={`flex-1 py-2.5 text-sm font-semibold transition-colors cursor-pointer ${
                  type === "BUY" ? "bg-green-500 text-white" : "bg-slate-800 text-slate-400 hover:text-slate-200"
                }`}
              >
                BUY
              </button>
              <button
                type="button"
                onClick={() => set_type("SELL")}
                className={`flex-1 py-2.5 text-sm font-semibold transition-colors cursor-pointer ${
                  type === "SELL" ? "bg-red-500 text-white" : "bg-slate-800 text-slate-400 hover:text-slate-200"
                }`}
              >
                SELL
              </button>
            </div>

            <input
              type="number"
              placeholder="Quantity"
              value={quantity}
              onChange={(e) => set_quantity(e.target.value)}
              min="0"
              step="any"
              required
              className={inputClass}
            />

            <div className="grid grid-cols-2 gap-3">
              <input
                type="number"
                placeholder="Stop Loss"
                value={stop_loss}
                onChange={(e) => set_stop_loss(e.target.value)}
                min="0"
                step="any"
                className={inputClass}
              />
              <input
                type="number"
                placeholder="Take Profit"
                value={take_profit}
                onChange={(e) => set_take_profit(e.target.value)}
                min="0"
                step="any"
                className={inputClass}
              />
            </div>

            <button
              type="submit"
              className={`w-full font-semibold rounded-lg py-2.5 text-sm transition-colors cursor-pointer mt-1 ${
                type === "BUY"
                  ? "bg-green-500 hover:bg-green-400 text-white"
                  : "bg-red-500 hover:bg-red-400 text-white"
              }`}
            >
              Place {type} Order
            </button>
          </form>
        </div>

        {/* Order list */}
        <div>
          {/* Filter tabs */}
          <div className="flex gap-1 mb-4 bg-slate-900 border border-slate-800 rounded-xl p-1 w-fit">
            {["OPEN", "CLOSED"].map((s) => (
              <button
                key={s}
                onClick={() => set_status_filter(s)}
                className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors cursor-pointer ${
                  status_filter === s
                    ? "bg-slate-700 text-white"
                    : "text-slate-500 hover:text-slate-300"
                }`}
              >
                {s}
              </button>
            ))}
          </div>

          {orders.length === 0 ? (
            <div className="bg-slate-900 border border-slate-800 rounded-xl px-6 py-12 text-center">
              <p className="text-slate-500 text-sm">No {status_filter.toLowerCase()} orders</p>
            </div>
          ) : (
            <div className="flex flex-col gap-2">
              {orders.map((order) => (
                <div
                  key={order.id}
                  className="bg-slate-900 border border-slate-800 rounded-xl px-5 py-4 flex justify-between items-center hover:border-slate-700 transition-colors"
                >
                  <div className="flex flex-col gap-1">
                    <div className="flex items-center gap-2">
                      <span className={`text-xs font-bold px-2 py-0.5 rounded ${
                        order.type === "BUY"
                          ? "bg-green-500/10 text-green-400"
                          : "bg-red-500/10 text-red-400"
                      }`}>
                        {order.type}
                      </span>
                      <span className="text-white font-medium text-sm">{order.symbol}</span>
                    </div>
                    <div className="text-slate-400 text-xs">
                      Qty: {order.quantity}
                      {order.price && <span className="ml-2">@ ${Number(order.price).toLocaleString()}</span>}
                    </div>
                    <div className="flex gap-3">
                      {order.stopLoss && (
                        <span className="text-amber-400/80 text-xs">SL {order.stopLoss}</span>
                      )}
                      {order.takeProfit && (
                        <span className="text-green-400/80 text-xs">TP {order.takeProfit}</span>
                      )}
                    </div>
                    <div className="text-slate-600 text-xs">
                      {new Date(order.createdAt).toLocaleString()}
                    </div>
                  </div>

                  <div className="flex flex-col items-end gap-2">
                    <span className={`text-xs px-2.5 py-0.5 rounded-full font-medium ${
                      order.status === "OPEN"
                        ? "bg-green-500/10 text-green-400"
                        : "bg-slate-800 text-slate-500"
                    }`}>
                      {order.status}
                    </span>
                    {order.status === "OPEN" && (
                      <button
                        onClick={() => handleCancelOrder(order.id)}
                        className="text-xs px-3 py-1 rounded-lg text-red-400 hover:text-red-300 border border-red-900/50 hover:border-red-800 transition-colors cursor-pointer"
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
    </div>
  );
}

export default OrdersPage;
