import api_order from "./order_axios";

export async function createOrder(data) {
  const response = await api_order.post("/api/orders", data);

  return response.data;
}

export async function getUserOrders(status) {
  const response = await api_order.get("/api/orders", {
    params: { status },
  });

  return response.data;
}

export async function cancelOrder(orderId) {
  const response = await api_order.delete(`/api/orders/${orderId}`);

  return response.data;
}
