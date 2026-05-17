import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

function getUserIdFromToken(token) {
  try {
    const payload = token.split(".")[1];
    return JSON.parse(atob(payload)).sub;
  } catch {
    return null;
  }
}

function formatMessage(data) {
  const price = data.executionPrice
    ? `$${Number(data.executionPrice).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    : "";
  const trigger = data.triggerType || "trigger";
  return `${data.symbol} ${data.type} order closed at ${price} (${trigger})`;
}

export function useNotifications(token) {
  const [notifications, setNotifications] = useState([]);
  const clientRef = useRef(null);
  const seenOrderIds = useRef(new Set());

  useEffect(() => {
    if (!token) return;
    const userId = getUserIdFromToken(token);
    if (!userId) return;

    const client = new Client({
      webSocketFactory: () =>
        new SockJS("http://localhost:8084/ws-notifications"),
      onConnect: () => {
        client.subscribe(`/topic/orders/${userId}`, (message) => {
          const data = JSON.parse(message.body);

          if (data.orderId) {
            if (seenOrderIds.current.has(data.orderId)) return;
            seenOrderIds.current.add(data.orderId);
          }

          setNotifications((prev) =>
            [
              {
                id: Date.now(),
                message: formatMessage(data),
                timestamp: Date.now(),
              },
              ...prev,
            ].slice(0, 20),
          );
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error", frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [token]);

  const clearNotifications = () => setNotifications([]);

  return { notifications, clearNotifications };
}
