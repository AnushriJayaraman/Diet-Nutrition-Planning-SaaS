import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../../api";
import "./PatientDashboard.css";

function PatientDashboard() {
  const navigate = useNavigate();
  const [snapshot, setSnapshot] = useState(null);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    axios.get("/api/patient/dashboard")
      .then((response) => setSnapshot(response.data))
      .catch((error) => setLoadError(error.response?.data?.message || "Unable to load your saved health data."));
  }, []);

  return (
    <div className="patient-dashboard">
      <div className="patient-header">
        <span>PATIENT DASHBOARD</span>

        <h1>🌿 My Nutrition Journey</h1>

        <p>
          View your diet plan, nutrition progress and personal information.
        </p>
      </div>
      {loadError && <p className="patient-dashboard-error" role="alert">{loadError}</p>}
      <section className="patient-dashboard-snapshot">
        <div><strong>{snapshot?.latestBmi?.bmi ?? "—"}</strong><span>Latest BMI</span></div>
        <div><strong>{snapshot?.dietPlanCount ?? "—"}</strong><span>Diet plans</span></div>
        <div><strong>{snapshot?.todayTracking?.totalCalories ?? "—"}</strong><span>Today's calories (kcal)</span></div>
        <div>
          <strong>{snapshot?.nextAppointment ? new Date(snapshot.nextAppointment.startsAt).toLocaleString() : "—"}</strong>
          <span>Next appointment</span>
        </div>
      </section>

      <div className="patient-modules">

        <div
          className="patient-module-card"
          onClick={() => navigate("/patient/diet-plan")}
        >
          <div className="patient-module-icon">🥗</div>

          <h2>Diet Plan</h2>

          <p>
            View your personalized diet plan and meal recommendations.
          </p>

          <button type="button">
            Open Module →
          </button>
        </div>

        <div className="patient-module-card" onClick={() => navigate("/patient/appointments")}>
          <div className="patient-module-icon">📅</div>
          <h2>Appointments</h2>
          <p>Request an appointment with your assigned nutritionist and view updates.</p>
          <button type="button">Open Appointments →</button>
        </div>

        <div
          className="patient-module-card"
          onClick={() => navigate("/patient/bmi")}
        >
          <div className="patient-module-icon">⚖️</div>

          <h2>BMI Assessment</h2>

          <p>
            View your recorded BMI assessment information.
          </p>

          <button type="button">
            Open Module →
          </button>
        </div>

        <div
          className="patient-module-card"
          onClick={() => navigate("/patient/tracking")}
        >
          <div className="patient-module-icon">🍎</div>

          <h2>Nutrition Tracking</h2>

          <p>
            Track your meals and daily nutrition information.
          </p>

          <button type="button">
            Open Module →
          </button>
        </div>

        <div
          className="patient-module-card"
          onClick={() => navigate("/patient/progress")}
        >
          <div className="patient-module-icon">📈</div>

          <h2>Progress</h2>

          <p>
            View your nutrition progress and updates.
          </p>

          <button type="button">
            Open Module →
          </button>
        </div>

        <div
          className="patient-module-card"
          onClick={() => navigate("/patient/profile")}
        >
          <div className="patient-module-icon">👤</div>

          <h2>My Profile</h2>

          <p>
            View and manage your personal information.
          </p>

          <button type="button">
            Open Module →
          </button>
        </div>

      </div>
    </div>
  );
}

export default PatientDashboard;