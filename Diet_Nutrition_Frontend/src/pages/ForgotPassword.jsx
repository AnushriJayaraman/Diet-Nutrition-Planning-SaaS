import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import axios from "../api";
import "./Login.css";

function ForgotPassword() {
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState(location.state?.email || "");
  const [code, setCode] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [codeRequested, setCodeRequested] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const requestCode = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await axios.post("/api/auth/password-reset/request", { email });
      setCodeRequested(true);
      setMessage(response.data.message);
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to request a reset code. Please try again.");
    } finally {
      setBusy(false);
    }
  };

  const resetPassword = async (event) => {
    event.preventDefault();
    const strongPassword =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

    if (!strongPassword.test(password)) {
      setError("Use at least 8 characters with uppercase, lowercase, number, and special character.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setBusy(true);
    setError("");
    try {
      const response = await axios.post("/api/auth/password-reset/confirm", {
        email,
        code,
        newPassword: password,
      });
      navigate("/login", { state: { notice: response.data.message } });
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to reset your password. Check the code and try again.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-icon">🥗</div>
        <h1>{codeRequested ? "Reset Password" : "Forgot Password?"}</h1>
        <p className="login-subtitle">
          {codeRequested
            ? "Enter the email code and choose a new password."
            : "Enter your account email and we will send a password reset code."}
        </p>

        {message && <p className="login-notice" role="status">{message}</p>}
        {error && <p className="login-error" role="alert">{error}</p>}

        {!codeRequested ? (
          <form onSubmit={requestCode}>
            <div className="input-group">
              <label htmlFor="reset-email">Email Address</label>
              <input
                id="reset-email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="Enter your account email"
                required
              />
            </div>
            <button type="submit" className="login-button" disabled={busy}>
              {busy ? "Sending code..." : "Send reset code"}
            </button>
          </form>
        ) : (
          <form onSubmit={resetPassword}>
            <div className="input-group">
              <label htmlFor="reset-code">Email verification code</label>
              <input
                id="reset-code"
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                pattern="[0-9]{6}"
                maxLength="6"
                value={code}
                onChange={(event) => setCode(event.target.value.replace(/\D/g, ""))}
                placeholder="000000"
                required
              />
            </div>
            <div className="input-group">
              <label htmlFor="new-password">New Password</label>
              <input
                id="new-password"
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Create a new password"
                required
              />
            </div>
            <div className="input-group">
              <label htmlFor="confirm-new-password">Confirm New Password</label>
              <input
                id="confirm-new-password"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                placeholder="Confirm your new password"
                required
              />
            </div>
            <button type="submit" className="login-button" disabled={busy}>
              {busy ? "Updating password..." : "Reset password"}
            </button>
          </form>
        )}

        <p className="signup-text">
          <Link to="/login">Back to login</Link>
        </p>
      </div>
    </div>
  );
}

export default ForgotPassword;
