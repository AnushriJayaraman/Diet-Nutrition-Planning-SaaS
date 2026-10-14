import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./Overview.css";

const metrics = [
  ["patients", "Patients", "Registered patient records"],
  ["nutritionists", "Nutritionists", "Care team members"],
  ["users", "User accounts", "Registered accounts across all roles"],
  ["dietPlans", "Diet plans", "Plans created by the care team"],
  ["bmiAssessments", "BMI assessments", "Health checks recorded"],
  ["trackingRecords", "Tracking records", "Nutrition logs collected"],
  ["progressUpdates", "Progress updates", "Patient milestones logged"],
  ["patientProfiles", "Profiles", "Completed patient profiles"],
];

function Overview() {
  const [overview, setOverview] = useState(null);
  const [message, setMessage] = useState("");

  const loadOverview = async () => {
    try {
      const response = await axios.get("/api/admin/overview");
      setOverview(response.data);
      setMessage("");
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to load the clinic overview."));
    }
  };

  useEffect(() => {
    loadOverview();
  }, []);

  return (
    <div className="admin-overview-page">
      <div className="admin-overview-header">
        <div>
          <span>ADMIN MODULE</span>
          <h1>Clinic Overview</h1>
          <p>A live view of your clinic's people, plans, and nutrition activity.</p>
        </div>
        <button onClick={loadOverview}>Refresh overview</button>
      </div>

      {message && <p className="overview-message">{message}</p>}

      {overview && (
        <>
          <div className="overview-status">
            <span className="status-dot" />
            <strong>{overview.serviceStatus}</strong>
            <span>Ready for daily operations</span>
          </div>

          <div className="overview-metrics">
            {metrics.map(([key, title, description]) => (
              <div className="overview-metric" key={key}>
                <span>{title}</span>
                <strong>{overview[key]}</strong>
                <small>{description}</small>
              </div>
            ))}
          </div>

          <div className="overview-note">
            <div className="overview-note-mark">+</div>
            <div>
              <h2>Keep the care loop moving</h2>
              <p>Review new patient records, check progress updates, and use reports to spot where your team needs attention.</p>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export default Overview;
