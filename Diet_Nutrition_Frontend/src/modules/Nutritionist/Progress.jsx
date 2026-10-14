import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./Progress.css";

function Progress() {
  const { records, loading, error } = useBackendRecords("/api/nutrition-progress", "progress");
  const { records: patients, loading: patientsLoading, error: patientsError } =
    useBackendRecords("/api/nutrition-patients", "patients");
  const [patientName, setPatientName] = useState("");
  const [patientEmail, setPatientEmail] = useState("");
  const [patientDetails, setPatientDetails] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState("");

  const selectedPatientProgress = patientEmail
    ? records.filter((record) => record.patientEmail === patientEmail)
    : [];

  const loadPatientDetails = async (email) => {
    if (!email) {
      setPatientDetails(null);
      return;
    }
    setPatientDetails(null);
    setDetailsLoading(true);
    setDetailsError("");
    try {
      const response = await axios.get(`/api/nutritionist/patients/${encodeURIComponent(email)}/details`);
      setPatientDetails(response.data);
    } catch (error) {
      setDetailsError(apiErrorMessage(error, "Unable to load the patient's combined history."));
    } finally {
      setDetailsLoading(false);
    }
  };

  return (
    <div className="nutrition-progress-page">
      <div className="nutrition-progress-header">
        <span>NUTRITIONIST MODULE</span>

        <h1>🎯 Patient Progress</h1>

        <p>
          Review progress and health updates submitted by your assigned patients.
        </p>
      </div>

      <div className="nutrition-progress-card">
        <h2>Patient Progress Records</h2>
        <div className="nutrition-progress-filters">
          <label htmlFor="progress-patient">Select assigned patient</label>

          <select
            id="progress-patient"
            value={patientEmail}
            onChange={(event) => {
              const patient = patients.find((item) => item.email === event.target.value);
              setPatientEmail(event.target.value);
              setPatientName(patient?.patientName || "");
              loadPatientDetails(event.target.value);
            }}
          >
            <option value="">{patientsLoading ? "Loading patients..." : "Select assigned patient"}</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.email}>{patient.patientName} ({patient.email})</option>
            ))}
          </select>
          {patientsError && <p className="nutrition-progress-message">{patientsError}</p>}
        </div>
      </div>
      {patientEmail && (
        <section className="nutrition-progress-history">
          <h2>Selected patient's combined history</h2>
          {detailsLoading && <p>Loading BMI, nutrition, and weight history...</p>}
          {detailsError && <p role="alert">{detailsError}</p>}
          {patientDetails && [
            ["bmiHistory", "BMI assessments"],
            ["nutritionHistory", "Nutrition tracking"],
            ["progressHistory", "Weight progress"],
          ].map(([key, title]) => {
            const items = patientDetails[key] || [];
            const columns = items.length ? Object.keys(items[0]) : [];
            return (
              <div className="nutrition-progress-history-section" key={key}>
                <h3>{title}</h3>
                {items.length === 0 ? <p>No records saved.</p> : (
                  <div className="backend-records-table-wrap">
                    <table className="backend-records-table">
                      <thead><tr>{columns.map((column) => <th key={column}>{column.replace(/([A-Z])/g, " $1")}</th>)}</tr></thead>
                      <tbody>{items.map((record, index) => (
                        <tr key={record.id ?? index}>{columns.map((column) => {
                          const value = record[column];
                          return <td key={column}>{value == null || value === "" ? "—" : Array.isArray(value) ? `${value.length} saved item(s)` : typeof value === "object" ? JSON.stringify(value) : String(value)}</td>;
                        })}</tr>
                      ))}</tbody>
                    </table>
                  </div>
                )}
              </div>
            );
          })}
        </section>
      )}
      <BackendRecords
        title={patientEmail ? `Saved progress for ${patientName || patientEmail}` : "Saved patient progress"}
        records={selectedPatientProgress}
        columns={[["patientName", "Patient"], ["patientEmail", "Patient email"], ["currentWeight", "Current weight (kg)"], ["targetWeight", "Target weight (kg)"], ["progress", "Status"], ["recordedAt", "Recorded at"], ["notes", "Notes"]]}
        loading={loading}
        error={error}
        emptyMessage={patientEmail ? "No progress records saved for this patient yet." : "Select an assigned patient to review progress."}
        reverseRecords={false}
      />
    </div>
  );
}

export default Progress;