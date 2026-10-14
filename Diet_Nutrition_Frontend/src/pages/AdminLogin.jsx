import { Link, useNavigate } from "react-router-dom";
import axios from "../api";
import "./Login.css";

function AdminLogin() {
  const navigate = useNavigate();

  const handleLogin = async (event) => {
    event.preventDefault();
    const email = event.currentTarget.elements.namedItem("email").value;
    const password = event.currentTarget.elements.namedItem("password").value;

    try {
      const response = await axios.post("/api/auth/admin-login", {
        email,
        password,
      });
      localStorage.setItem("currentUser", JSON.stringify({
        ...response.data.user,
        token: response.data.token,
      }));
      navigate("/admin/dashboard");
    } catch (error) {
      alert(error.response?.data?.message || "Admin sign-in failed. Please try again.");
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-icon">A</div>
        <h1>Admin Sign In</h1>
        <p className="login-subtitle">Use your existing administrator ID and password.</p>
        <form onSubmit={handleLogin}>
          <div className="input-group">
            <label htmlFor="admin-email">Admin ID</label>
            <input id="admin-email" name="email" type="email" placeholder="Enter your admin email" autoComplete="username" required />
          </div>
          <div className="input-group">
            <label htmlFor="admin-password">Password</label>
            <input id="admin-password" name="password" type="password" placeholder="Enter your password" autoComplete="current-password" required />
          </div>
          <button type="submit" className="login-button">Sign in as admin</button>
        </form>
        <div className="divider"><span>OR</span></div>
        <p className="signup-text"><Link to="/login">Back to login</Link></p>
      </div>
    </div>
  );
}

export default AdminLogin;
