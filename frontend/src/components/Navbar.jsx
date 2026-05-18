import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import NotificationBell from "./NotificationBell";

const navLinkClass = ({ isActive }) =>
  `text-sm font-medium transition-colors ${
    isActive ? "text-white" : "text-slate-400 hover:text-slate-200"
  }`;

function Navbar() {
  const auth = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    auth.logout();
    navigate("/login");
  };

  return (
    <nav className="bg-slate-900/80 backdrop-blur border-b border-slate-800 px-6 py-3 flex items-center justify-between sticky top-0 z-40">
      <div className="flex items-center gap-8">
        <span className="text-white font-semibold text-sm tracking-wider">
          PI <span className="text-green-400">Trading</span>
        </span>
        <div className="flex items-center gap-6">
          <NavLink to="/dashboard" className={navLinkClass}>
            Dashboard
          </NavLink>
          <NavLink to="/orders" className={navLinkClass}>
            Orders
          </NavLink>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {auth.username ? (
          <>
            <span className="text-slate-500 text-xs">{auth.username}</span>
            <NotificationBell />
            <button
              onClick={handleLogout}
              className="text-slate-400 hover:text-slate-200 text-sm transition-colors cursor-pointer"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login" className={navLinkClass}>
              Login
            </NavLink>
            <NavLink
              to="/register"
              className="bg-green-500 hover:bg-green-400 text-white text-sm font-medium px-4 py-1.5 rounded-lg transition-colors"
            >
              Register
            </NavLink>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
