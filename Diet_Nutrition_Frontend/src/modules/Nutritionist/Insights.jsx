import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./Insights.css";

const API_URL = "/api/nutritionist/analytics";

function Insights() {
  const [analytics, setAnalytics] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadAnalytics = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API_URL);
      setAnalytics(response.data);
      setMessage("");
    } catch (error) {
      setAnalytics(null);
      setMessage(apiErrorMessage(error, "Unable to load nutrition insights."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  const statuses = analytics?.progressStatuses || {};
  const needsAttention = statuses["Needs Attention"] || 0;
  const maxStatusCount = Math.max(...Object.values(statuses), 1);

  return (
    <div className="nutrition-insights-page">
      <div className="nutrition-insights-header">
        <div>
          <span>NUTRITIONIST MODULE</span>
          <h1>Patient Insights</h1>
          <p>Turn your saved assessments into a clear care plan for every patient.</p>
        </div>
        <button className="insights-refresh" onClick={loadAnalytics} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh insights"}
        </button>
      </div>

      {message && <p className="insights-message">{message}</p>}

      {analytics && (
        <>
          <section className="insights-metrics" aria-label="Nutritionist summary">
            <div className="insight-metric metric-green">
              <span>Patients tracked</span>
              <strong>{analytics.trackedPatients}</strong>
              <small>Unique names in tracking records</small>
            </div>
            <div className="insight-metric metric-teal">
              <span>Average hydration</span>
              <strong>{analytics.averageWaterLitres} L</strong>
              <small>Across saved daily logs</small>
            </div>
            <div className="insight-metric metric-yellow">
              <span>Average calories</span>
              <strong>{analytics.averageCalories} kcal</strong>
              <small>Sum of patient-entered food values</small>
            </div>
            <div className="insight-metric metric-coral">
              <span>Average BMI</span>
              <strong>{analytics.averageBmi || "--"}</strong>
              <small>From recorded assessments</small>
            </div>
          </section>

          <section className="insights-grid">
            <div className="insights-panel status-panel">
              <div className="panel-heading">
                <div>
                  <span className="panel-kicker">CARE SIGNALS</span>
                  <h2>Progress pulse</h2>
                </div>
                <strong>{analytics.progressUpdates} updates</strong>
              </div>
              {Object.keys(statuses).length === 0 ? (
                <p className="empty-state">Save a progress update to see patient momentum here.</p>
              ) : (
                <div className="status-list">
                  {Object.entries(statuses).map(([status, count]) => (
                    <div className="status-row" key={status}>
                      <div className="status-label"><span>{status}</span><strong>{count}</strong></div>
                      <div className="status-track"><span style={{ width: `${(count / maxStatusCount) * 100}%` }} /></div>
                    </div>
                  ))}
                </div>
              )}
              {needsAttention > 0 && (
                <div className="attention-note">{needsAttention} patient update{needsAttention > 1 ? "s" : ""} need a follow-up.</div>
              )}
            </div>

            <div className="insights-panel library-panel">
              <div className="panel-heading">
                <div>
                  <span className="panel-kicker">WORKLOAD</span>
                  <h2>Care library</h2>
                </div>
              </div>
              <div className="library-items">
                <div><strong>{analytics.patientsManaged}</strong><span>Patient records</span></div>
                <div><strong>{analytics.dietPlans}</strong><span>Diet plans</span></div>
                <div><strong>{analytics.progressUpdates}</strong><span>Progress notes</span></div>
                <div><strong>{analytics.upcomingAppointments}</strong><span>Upcoming appointments</span></div>
                <div><strong>{analytics.pendingFollowUps}</strong><span>Follow-ups needed</span></div>
              </div>
              <p className="library-caption">Your saved work, gathered in one place for faster consultations.</p>
            </div>
          </section>

          <section className="insights-panel recent-panel">
            <div className="panel-heading">
              <div>
                <span className="panel-kicker">RECENT RECORDS</span>
                <h2>Latest nutrition tracking</h2>
              </div>
              <span className="record-count">{analytics.recentTracking.length} saved</span>
            </div>
            {analytics.recentTracking.length === 0 ? (
              <p className="empty-state">No tracking records yet. Add one from Nutrition Tracking.</p>
            ) : (
              <div className="record-grid">
                {analytics.recentTracking.slice(0, 6).map((record) => (
                  <article className="record-item" key={record.id}>
                    <div className="record-avatar">{record.patientName.charAt(0).toUpperCase()}</div>
                    <div>
                      <strong>{record.patientName}</strong>
                      <p>{record.trackedDate || "Date unavailable"} <span>/</span> {record.waterLitres} L water <span>/</span> {record.mealsCompleted} meals</p>
                      <p>{record.totalCalories} kcal <span>/</span> {record.totalProteinGrams} g protein <span>/</span> {record.totalCarbohydratesGrams} g carbs <span>/</span> {record.totalFatGrams} g fat</p>
                      {record.notes && <small>{record.notes}</small>}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}

export default Insights;
