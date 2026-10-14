import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import axios from "../api";
import "./Login.css";

function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();

    const email = e.target.email.value;
    const password = e.target.password.value;
    if (!role) {
      alert("Please select whether you are signing in as a patient or nutritionist.");
      return;
    }

    try {
      const response = await axios.post("/api/auth/login", {
        email,
        password,
        role,
      });

      const user = { ...response.data.user, token: response.data.token };
      localStorage.setItem("currentUser", JSON.stringify(user));
      alert(response.data.message || "Login successful");

      if (user.role === "admin") {
        navigate("/admin/dashboard");
        return;
      }

      if (user.role === "nutritionist") {
        navigate("/nutritionist/dashboard");
        return;
      }

      navigate("/patient/dashboard");
    } catch (error) {
      alert(error.response?.data?.message || "Login failed. Please try again.");
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        <div className="login-icon">🥗</div>

        <h1>Welcome Back</h1>

        <p className="login-subtitle">
          Login to your NutriCare account
        </p>

        {location.state?.notice && <p className="login-notice">{location.state.notice}</p>}

        <form onSubmit={handleLogin}>

          <div className="input-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              name="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="input-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              placeholder="Enter your password"
              required
            />
          </div>

          <fieldset className="role-selection">
            <legend>Sign in as</legend>
            <div className="role-options">
              <label className={role === "patient" ? "role-option selected" : "role-option"}>
                <input
                  type="radio"
                  name="role"
                  value="patient"
                  checked={role === "patient"}
                  onChange={(event) => setRole(event.target.value)}
                  required
                />
                <span>Patient</span>
              </label>
              <label className={role === "nutritionist" ? "role-option selected" : "role-option"}>
                <input
                  type="radio"
                  name="role"
                  value="nutritionist"
                  checked={role === "nutritionist"}
                  onChange={(event) => setRole(event.target.value)}
                  required
                />
                <span>Nutritionist</span>
              </label>
            </div>
          </fieldset>

          <div className="login-options">
            <label>
              <input type="checkbox" />
              Remember me
            </label>
            <Link to="/forgot-password" state={{ email }}>Forgot password?</Link>
          </div>

          <button type="submit" className="login-button">
            Login
          </button>

        </form>

        <div className="divider">
          <span>OR</span>
        </div>

        <p className="signup-text">
          Don't have an account?
          <Link to="/signup"> Create Account</Link>
        </p>

        <p className="admin-login-link">
          <Link to="/admin/login">Administrator sign in</Link>
        </p>

      </div>
    </div>
  );
}

export default Login;