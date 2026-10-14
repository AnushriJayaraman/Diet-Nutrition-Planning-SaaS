import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../../api";
import "./NutritionistDashboard.css";

function NutritionistDashboard() {
  const navigate = useNavigate();
  const [snapshot, setSnapshot] = useState(null);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    axios.get("/api/nutritionist/analytics")
      .then((response) => {
        setSnapshot(response.data);
        setLoadError("");
      })
      .catch((error) => {
        setSnapshot(null);
        setLoadError(error.response?.data?.message || "Unable to load your patient data.");
      });
  }, []);

  return (
    <div className="nutritionist-dashboard">
      <div className="nutritionist-header">
        <span>NUTRITIONIST DASHBOARD</span>
        <h1>Nutrition Planning</h1>
        <p>Manage patient nutrition, personalized diet plans, and measurable outcomes.</p>
      </div>
      {loadError && <p role="alert">{loadError}</p>}

      <div className="nutritionist-snapshot">
        <div>
          <span>CARE SNAPSHOT</span>
          <strong>{snapshot ? snapshot.trackedPatients : "--"}</strong>
          <p>Patients with tracking data</p>
        </div>
        <div>
          <strong>{snapshot ? snapshot.progressUpdates : "--"}</strong>
          <p>Progress updates recorded</p>
        </div>
        <div>
          <strong>{snapshot ? `${snapshot.averageWaterLitres} L` : "--"}</strong>
          <p>Average hydration</p>
        </div>
        <button onClick={() => navigate("/nutritionist/insights")}>Open patient insights</button>
      </div>

      <div className="nutritionist-modules">

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/patients")}
        >
          <div className="nutritionist-module-icon">⚖️</div>
          <h2>Patient BMI History</h2>
          <p>Review BMI assessments recorded by each assigned patient.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/diet-plan")}
        >
          <div className="nutritionist-module-icon">🥗</div>
          <h2>Diet Plan</h2>
          <p>Create and manage personalized diet plans.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/tracking")}
        >
          <div className="nutritionist-module-icon">📈</div>
          <h2>Nutrition Tracking</h2>
          <p>Track patient nutrition and meal progress.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/patients")}
        >
          <div className="nutritionist-module-icon">👤</div>
          <h2>Patient Management</h2>
          <p>View and manage patients assigned to the nutritionist.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/progress")}
        >
          <div className="nutritionist-module-icon">🎯</div>
          <h2>Progress</h2>
          <p>Monitor patient nutrition progress and updates.</p>
          <button>Open Module →</button>
        </div>

        <div
          className="nutritionist-module-card insights-module-card"
          onClick={() => navigate("/nutritionist/insights")}
        >
          <div className="nutritionist-module-icon">◎</div>
          <h2>Patient Insights</h2>
          <p>Compare hydration, meals, BMI, progress signals, and recent records.</p>
          <button>View Analysis →</button>
        </div>

        <div
          className="nutritionist-module-card"
          onClick={() => navigate("/nutritionist/appointments")}
        >
          <div className="nutritionist-module-icon">📅</div>
          <h2>Appointments</h2>
          <p>Review and manage appointment requests from your assigned patients.</p>
          <button type="button">Manage Appointments →</button>
        </div>

        <div className="nutritionist-module-card" onClick={() => navigate("/nutritionist/profile")}>
          <div className="nutritionist-module-icon">🩺</div>
          <h2>Professional Profile</h2>
          <p>Maintain your professional details and clinic information.</p>
          <button type="button">Edit Profile →</button>
        </div>

      </div>
    </div>
  );
}

export default NutritionistDashboard;