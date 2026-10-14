import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";

const HomeIcon = () => (
  <svg viewBox="0 0 24 24">
    <path d="M3 10.5L12 3L21 10.5" />
    <path d="M5.5 9.5V20H18.5V9.5" />
    <path d="M9.5 20V14H14.5V20" />
  </svg>
);

const ServiceIcon = () => (
  <svg viewBox="0 0 24 24">
    <path d="M4 6H20" />
    <path d="M4 12H20" />
    <path d="M4 18H20" />
    <circle cx="8" cy="6" r="1.5" />
    <circle cx="15" cy="12" r="1.5" />
    <circle cx="10" cy="18" r="1.5" />
  </svg>
);

const AboutIcon = () => (
  <svg viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="9" />
    <path d="M12 10V16" />
    <circle cx="12" cy="7" r="1" fill="currentColor" stroke="none" />
  </svg>
);

const ContactIcon = () => (
  <svg viewBox="0 0 24 24">
    <rect x="4" y="5" width="16" height="14" rx="2" />
    <path d="M4 7L12 13L20 7" />
  </svg>
);

function Logo() {
  return (
    <Link to="/" className="navbar-logo">
      <div className="logo-symbol">🥗</div>

      <div className="logo-text">
        <h2>NutriCare</h2>
        <span>SMART NUTRITION PLANNING</span>
      </div>
    </Link>
  );
}

function Navbar() {
  const navigate = useNavigate();
  let user = null;
  try {
    user = JSON.parse(localStorage.getItem("currentUser") || "null");
  } catch {
    localStorage.removeItem("currentUser");
  }
  if (!user?.token) user = null;

  const goToDashboard = () => {
    if (!user) return;
    if (user.role === "admin") navigate("/admin/dashboard");
    else if (user.role === "nutritionist") navigate("/nutritionist/dashboard");
    else navigate("/patient/dashboard");
  };

  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    navigate("/login");
  };

  return (
    <nav className="navbar">

      <Logo />

      <div className="navbar-links">

        <Link to="/" className="nav-link">
          <HomeIcon />
          <span>Home</span>
        </Link>

        <a href="/#services" className="nav-link">
          <ServiceIcon />
          <span>Our Services</span>
        </a>

        <a href="/#about" className="nav-link">
          <AboutIcon />
          <span>About</span>
        </a>

        <a href="/#contact" className="nav-link">
          <ContactIcon />
          <span>Contact</span>
        </a>

      </div>

      <div className="navbar-actions">
        {user ? (
          <>
            <button type="button" className="navbar-login" onClick={goToDashboard}>
              Dashboard
            </button>
            <button type="button" className="navbar-signup" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="navbar-login">
              Login
            </Link>

            <Link to="/signup" className="navbar-signup">
              Signup
            </Link>
          </>
        )}
      </div>

    </nav>
  );
}

export default Navbar;