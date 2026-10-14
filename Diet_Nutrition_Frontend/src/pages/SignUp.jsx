import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "../api";
import "./SignUp.css";

function getSignupError(error, fallback) {
  if (error.response?.status === 404) {
    return "The backend currently running is outdated. Restart the backend so account verification endpoints are available.";
  }
  if (!error.response) {
    return "Cannot reach the backend. Make sure the Spring Boot server is running.";
  }
  return error.response.data?.message || fallback;
}

function SignUp() {
  const navigate = useNavigate();
  const [role, setRole] = useState("patient");
  const [verificationPending, setVerificationPending] = useState(false);
  const [signupEmail, setSignupEmail] = useState("");
  const [destination, setDestination] = useState("");
  const [verificationCode, setVerificationCode] = useState("");
  const [resendSeconds, setResendSeconds] = useState(0);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (resendSeconds <= 0) return undefined;
    const timer = window.setTimeout(() => setResendSeconds((seconds) => seconds - 1), 1000);
    return () => window.clearTimeout(timer);
  }, [resendSeconds]);

  const handleSignup = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const name = form.elements.namedItem("name").value;
    const email = form.elements.namedItem("email").value;
    const password = form.elements.namedItem("password").value;
    const confirmPassword = form.elements.namedItem("confirmPassword").value;
    const selectedRole = form.elements.namedItem("role").value;

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
      const response = await axios.post("/api/auth/signup/otp", {
        name,
        email,
        password,
        confirmPassword,
        role: selectedRole,
      });
      setSignupEmail(email);
      setDestination(response.data.destination);
      setVerificationPending(true);
      setResendSeconds(30);
      setVerificationCode("");
      setMessage(response.data.message || "Verification code sent.");
    } catch (requestError) {
      setError(getSignupError(requestError, "Unable to send verification code. Please try again."));
    } finally {
      setBusy(false);
    }
  };

  const handleVerify = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await axios.post("/api/auth/signup/verify", {
        email: signupEmail,
        code: verificationCode,
      });
      navigate("/login", { state: { notice: "Account verified. You can now sign in." } });
    } catch (requestError) {
      setError(getSignupError(requestError, "Verification failed. Check the code and try again."));
    } finally {
      setBusy(false);
    }
  };

  const resendCode = async () => {
    setBusy(true);
    setError("");
    try {
      const response = await axios.post("/api/auth/signup/resend-otp", {
        email: signupEmail,
      });
      setDestination(response.data.destination);
      setResendSeconds(30);
      setVerificationCode("");
      setMessage(response.data.message || "A new verification code was sent.");
    } catch (requestError) {
      setError(getSignupError(requestError, "Unable to resend the verification code."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="signup-page">
      <div className="signup-card">
        <div className="signup-icon">🥗</div>
        <h1>{verificationPending ? "Verify your email" : "Create Account"}</h1>
        <p className="signup-subtitle">
          {verificationPending
            ? `Enter the 6-digit code for ${destination}.`
            : "Join NutriCare for smarter nutrition planning"}
        </p>

        {verificationPending ? (
          <form onSubmit={handleVerify}>
            <div className="input-group">
              <label htmlFor="verification-code">Verification code</label>
              <input
                id="verification-code"
                name="code"
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                pattern="[0-9]{6}"
                maxLength="6"
                placeholder="000000"
                value={verificationCode}
                onChange={(event) => setVerificationCode(event.target.value.replace(/\D/g, ""))}
                required
              />
            </div>
            {message && <p className="verification-message">{message}</p>}
            {error && <p className="verification-error" role="alert">{error}</p>}
            <button type="submit" className="signup-button" disabled={busy}>
              {busy ? "Verifying..." : "Verify and create account"}
            </button>
            <button
              type="button"
              className="resend-code-button"
              onClick={resendCode}
              disabled={busy || resendSeconds > 0}
            >
              {resendSeconds > 0 ? `Resend code in ${resendSeconds}s` : "Resend code"}
            </button>
          </form>
        ) : (
          <form onSubmit={handleSignup}>
            <div className="input-group">
              <label htmlFor="signup-name">Full Name</label>
              <input id="signup-name" type="text" name="name" placeholder="Enter your full name" required />
            </div>

            <div className="input-group">
              <label htmlFor="signup-email">Email Address</label>
              <input id="signup-email" type="email" name="email" placeholder="Enter your email address" required />
            </div>

            <div className="input-group">
              <label htmlFor="signup-role">Role</label>
              <select id="signup-role" name="role" value={role} onChange={(event) => setRole(event.target.value)}>
                <option value="patient">Patient</option>
                <option value="nutritionist">Nutritionist</option>
              </select>
            </div>

            <div className="input-group">
              <label htmlFor="signup-password">Password</label>
              <input id="signup-password" type="password" name="password" placeholder="Create your password" autoComplete="new-password" required />
            </div>

            <div className="input-group">
              <label htmlFor="signup-confirm-password">Confirm Password</label>
              <input id="signup-confirm-password" type="password" name="confirmPassword" placeholder="Confirm your password" autoComplete="new-password" required />
            </div>

            <label className="terms">
              <input type="checkbox" required />
              <span>I agree to the Terms & Conditions</span>
            </label>

            {error && <p className="verification-error" role="alert">{error}</p>}
            <button type="submit" className="signup-button" disabled={busy}>
              {busy ? "Sending code..." : "Send verification code"}
            </button>
          </form>
        )}

        <div className="divider"><span>OR</span></div>
        <p className="login-text">
          Already have an account?
          <Link to="/login"> Login</Link>
        </p>
      </div>
    </div>
  );
}

export default SignUp;
