import { useNavigate } from "react-router-dom";
import "./RoleSelection.css";

function RoleSelection() {
  const navigate = useNavigate();

  const handleStoredUserRedirect = () => {
    const storedUser = localStorage.getItem("currentUser");

    if (!storedUser) return false;

    try {
      const user = JSON.parse(storedUser);
      if (user?.role === "admin") {
        navigate("/admin/dashboard");
        return true;
      }
      if (user?.role === "nutritionist") {
        navigate("/nutritionist/dashboard");
        return true;
      }
      if (user?.role === "patient") {
        navigate("/patient/dashboard");
        return true;
      }
    } catch (error) {
      localStorage.removeItem("currentUser");
    }

    return false;
  };

  const selectRole = (role) => {
    if (role === "admin") {
      navigate("/admin/dashboard");
    }

    if (role === "nutritionist") {
      navigate("/nutritionist/dashboard");
    }

    if (role === "patient") {
      navigate("/patient/dashboard");
    }
  };

  if (handleStoredUserRedirect()) {
    return null;
  }

  return (
    <div className="role-selection-page">
      <div className="role-selection-card">
        <div className="role-logo">🥗</div>

        <span>DIET & NUTRITION PLANNING</span>

        <h1>Select Your Role</h1>

        <p>
          Choose your role to continue to your personalized dashboard.
        </p>

        <div className="role-options">

          <button
            className="role-option"
            onClick={() => selectRole("admin")}
          >
            <div className="role-icon">👨‍💼</div>

            <div>
              <h2>Admin</h2>
              <p>Manage users, patients, nutritionists and reports.</p>
            </div>

            <strong>→</strong>
          </button>

          <button
            className="role-option"
            onClick={() => selectRole("nutritionist")}
          >
            <div className="role-icon">🥗</div>

            <div>
              <h2>Nutritionist</h2>
              <p>Create diet plans and track patient nutrition.</p>
            </div>

            <strong>→</strong>
          </button>

          <button
            className="role-option"
            onClick={() => selectRole("patient")}
          >
            <div className="role-icon">👤</div>

            <div>
              <h2>Patient</h2>
              <p>View your diet plan and nutrition progress.</p>
            </div>

            <strong>→</strong>
          </button>

        </div>
      </div>
    </div>
  );
}

export default RoleSelection;