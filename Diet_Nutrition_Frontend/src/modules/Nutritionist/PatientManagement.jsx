import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./PatientManagement.css";

const profileFields = [
  ["name", "Full name"],
  ["email", "Email"],
  ["phone", "Phone"],
  ["dateOfBirth", "Date of birth"],
  ["gender", "Gender"],
  ["heightCm", "Height (cm)"],
  ["currentWeightKg", "Current weight (kg)"],
  ["disease", "Disease"],
  ["medicalConditions", "Medical conditions"],
  ["allergies", "Food allergies"],
  ["dietaryRestrictions", "Dietary restrictions"],
  ["foodPreference", "Food preference"],
  ["foodPreferences", "Food preferences"],
  ["weightGoalKg", "Weight goal (kg)"],
];

function PatientManagement() {
  const { records, loading, error } = useBackendRecords("/api/nutrition-patients", "patients");
  const [details, setDetails] = useState(null);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState("");

  const viewDetails = async (patient) => {
    setSelectedPatient(patient);
    setDetails(null);
    setDetailLoading(true);
    setDetailError("");
    try {
      const { data } = await axios.get(
        `/api/nutritionist/patients/${encodeURIComponent(patient.email)}/details`
      );
      setDetails(data);
    } catch (requestError) {
      setDetailError(apiErrorMessage(requestError, "Unable to load this patient's records."));
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <div className="nutrition-patient-page">
      <div className="nutrition-patient-header">
        <span>NUTRITIONIST MODULE</span>
        <h1>Patient Management</h1>
        <p>View patients assigned by your clinic administrator and review their health profiles.</p>
      </div>

      <section className="nutrition-patient-card">
        <h2>My Assigned Patients</h2>
        {loading && <p>Loading assigned patients...</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && !error && records.length === 0 && (
          <p>No patients have been assigned to you yet. Contact your clinic administrator.</p>
        )}
        {!loading && !error && records.length > 0 && (
          <div className="nutrition-patient-table-wrap">
            <table>
              <thead><tr><th>Patient</th><th>Email</th><th>Health profile</th></tr></thead>
              <tbody>{records.map((patient) => (
                <tr key={patient.id}>
                  <td>{patient.patientName}</td>
                  <td>{patient.email}</td>
                  <td><button type="button" onClick={() => viewDetails(patient)}>View details</button></td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        )}
      </section>

      {selectedPatient && (
        <div className="nutrition-patient-modal-backdrop" onMouseDown={(event) => {
          if (event.target === event.currentTarget) setSelectedPatient(null);
        }}>
          <section className="nutrition-patient-modal" role="dialog" aria-modal="true" aria-labelledby="patient-details-title">
            <header>
              <div>
                <span>ASSIGNED PATIENT</span>
                <h2 id="patient-details-title">{selectedPatient.patientName}</h2>
                <p>{selectedPatient.email}</p>
              </div>
              <button type="button" aria-label="Close patient details" onClick={() => setSelectedPatient(null)}>×</button>
            </header>
            {detailLoading && <p>Loading saved patient information...</p>}
            {detailError && <p role="alert">{detailError}</p>}
            {details && (
              <>
                <section>
                  <h3>Health profile</h3>
                  {details.profile ? (
                    <dl className="nutrition-patient-profile">
                      {profileFields.map(([key, label]) => (
                          <div key={key}><dt>{label}</dt><dd>{details.profile[key] ?? "—"}</dd></div>
                        ))}
                    </dl>
                  ) : <p>No patient profile has been saved.</p>}
                </section>
              </>
            )}
          </section>
        </div>
      )}
    </div>
  );
}

export default PatientManagement;
