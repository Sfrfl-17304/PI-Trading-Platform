import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate, Link } from "react-router-dom";
import { register } from "../services/authService";

function RegisterPage() {
  const [username, set_username] = useState("");
  const [email, set_email] = useState("");
  const [password, set_password] = useState("");
  const [error, setError] = useState("");

  const auth = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async () => {
    setError("");
    try {
      await register(username, email, password);
      await auth.login(username, password);
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      setError("Username or email already taken.");
    }
  };

  const inputClass =
    "w-full bg-slate-800/60 border border-slate-700 text-slate-200 placeholder-slate-500 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-green-500/70 focus:bg-slate-800 transition-all";

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4 bg-[radial-gradient(ellipse_at_top,_#0f2040_0%,_#070d19_60%)]">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-bold text-white tracking-tight">
          PI <span className="text-green-400">Trading</span>
        </h1>
        <p className="text-slate-500 text-sm mt-1">Simulated crypto trading platform</p>
      </div>

      <div className="w-full max-w-sm bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl">
        <h2 className="text-lg font-semibold text-white mb-5">Create account</h2>

        {error && (
          <div className="bg-red-950/60 border border-red-900 text-red-300 text-sm rounded-lg px-4 py-3 mb-4">
            {error}
          </div>
        )}

        <div className="flex flex-col gap-3">
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => set_username(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
            className={inputClass}
          />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => set_email(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
            className={inputClass}
          />
          <input
            type="password"
            placeholder="Password (min. 8 characters)"
            value={password}
            onChange={(e) => set_password(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
            className={inputClass}
          />
          <button
            onClick={handleSubmit}
            className="w-full bg-green-500 hover:bg-green-400 text-white font-semibold rounded-lg py-2.5 text-sm transition-colors cursor-pointer mt-1"
          >
            Create account
          </button>
        </div>

        <p className="text-center text-slate-500 text-sm mt-5">
          Already have an account?{" "}
          <Link to="/login" className="text-green-400 hover:text-green-300 transition-colors">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;
