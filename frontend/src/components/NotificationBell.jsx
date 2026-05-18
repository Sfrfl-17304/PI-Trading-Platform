import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNotifications } from "../hooks/useNotifications";

function NotificationBell() {
  const { token } = useAuth();
  const { notifications, clearNotifications } = useNotifications(token);
  const [open, setOpen] = useState(false);

  if (!token) return null;

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="relative p-1 text-xl bg-transparent border-none cursor-pointer"
        title="Notifications"
      >
        🔔
        {notifications.length > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-4 h-4 flex items-center justify-center px-1">
            {notifications.length}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-10 w-80 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl z-50 overflow-hidden">
          <div className="flex justify-between items-center px-4 py-3 border-b border-slate-800">
            <span className="font-semibold text-slate-200 text-sm">
              Notifications
            </span>
            {notifications.length > 0 && (
              <button
                onClick={clearNotifications}
                className="text-slate-500 hover:text-slate-300 text-xs cursor-pointer bg-transparent border-none"
              >
                Clear all
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto">
            {notifications.length === 0 ? (
              <p className="text-slate-500 text-sm text-center py-4 px-4">
                No notifications
              </p>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className="px-4 py-3 border-b border-slate-800 last:border-0"
                >
                  <p className="text-slate-200 text-sm">{n.message}</p>
                  <p className="text-slate-500 text-xs mt-0.5">
                    {new Date(n.timestamp).toLocaleTimeString()}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
