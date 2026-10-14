import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../../api";
import "./AdminDashboard.css";

function AdminDashboard() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    axios.get("/api/admin/dashboard")
      .then((response) => setSummary(response.data))
      .catch((error) => setLoadError(error.response?.data?.message || "Unable to load clinic dashboard statistics."));
  }, []);

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <span>ADMIN DASHBOARD</span>
        <h1>Clinic Management</h1>
        <p>Coordinate people, records, care activity, and clinic performance.</p>
      </div>

      {loadError && <p className="admin-dashboard-error" role="alert">{loadError}</p>}
      <div className="admin-dashboard-stats">
        <div><strong>{summary ? summary.patients : "—"}</strong><span>Patient accounts</span></div>
        <div><strong>{summary ? summary.nutritionists : "—"}</strong><span>Nutritionists</span></div>
        <div><strong>{summary ? summary.users : "—"}</strong><span>Total accounts</span></div>
        <div><strong>{summary ? summary.dietPlans : "—"}</strong><span>Diet plans</span></div>
        <div><strong>{summary ? summary.appointments : "—"}</strong><span>Appointments</span></div>
      </div>

      <div className="admin-modules">

        <div
          className="admin-module-card overview-module-card"
          onClick={() => navigate("/admin/overview")}
        >
          <div className="admin-module-icon">◉</div>
          <h2>Clinic Overview</h2>
          <p>See live totals for users, care records, plans, and clinic activity.</p>
          <button>View Overview →</button>
        </div>

        <div
          className="admin-module-card"
          onClick={() => navigate("/admin/nutritionists")}
        >
          <div className="admin-module-icon">🥗</div>
          <h2>Nutritionist Management</h2>
          <p>Manage nutritionists registered with the clinic.</p>
          <button>Open Module →</button>
        </div>

        <div className="admin-module-card" onClick={() => navigate("/admin/appointments")}>
          <div className="admin-module-icon">📅</div>
          <h2>Appointments</h2>
          <p>View patient appointment requests and their current status.</p>
          <button type="button">View Appointments →</button>
        </div>

        <div
          className="admin-module-card"
          onClick={() => navigate("/admin/patients")}
        >
          <div className="admin-module-icon">👤</div>
          <h2>Patient Management</h2>
          <p>Manage patient information and records.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="admin-module-card"
          onClick={() => navigate("/admin/reports")}
        >
          <div className="admin-module-icon">📊</div>
          <h2>Reports</h2>
          <p>View clinic nutrition and patient reports.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="admin-module-card"
          onClick={() => navigate("/admin/users")}
        >
          <div className="admin-module-icon">👥</div>
          <h2>User Management</h2>
          <p>Manage users and their roles.</p>
          <button>Open Module →</button>
        </div>

      </div>
    </div>
  );
}

export default AdminDashboard;