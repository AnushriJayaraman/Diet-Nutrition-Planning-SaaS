import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import "./PatientManagement.css";

function PatientManagement() {
  const [patients, setPatients] = useState([]);
  const [nutritionists, setNutritionists] = useState([]);
  const [patientEmail, setPatientEmail] = useState("");
  const [nutritionistEmail, setNutritionistEmail] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const reload = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await axios.get("/api/patient-assignments");
      setPatients(response.data.patients);
      setNutritionists(response.data.nutritionists);
    } catch (loadError) {
      setError(apiErrorMessage(loadError, "Unable to load registered patients and nutritionists."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    reload();
  }, []);

  const availablePatients = patients.filter((patient) => !patient.nutritionistEmail);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    try {
      await axios.post("/api/patient-assignments", {
        patientEmail,
        nutritionistEmail,
      });
      await reload();

      setMessage("Patient assigned successfully.");
      setPatientEmail("");
      setNutritionistEmail("");
    } catch (submitError) {
      setMessage(apiErrorMessage(submitError, "Unable to assign the patient."));
    }
  };

  return (
    <div className="patient-management-page">
      <div className="patient-management-header">
        <span>ADMIN MODULE</span>
        <h1>👤 Patient Management</h1>
        <p>Assign registered patients to the nutritionist responsible for their care.</p>
      </div>

      <div className="patient-management-card">
        <h2>Assign Patient</h2>

        <form onSubmit={handleSubmit}>
          <label htmlFor="patient-assignment-patient">Registered patient</label>
          <select
            id="patient-assignment-patient"
            value={patientEmail}
            onChange={(e) => setPatientEmail(e.target.value)}
            required
          >
            <option value="">Select a patient</option>
            {availablePatients.map((patient) => (
              <option key={patient.email} value={patient.email}>
                {patient.name} ({patient.email})
              </option>
            ))}
          </select>

          <label htmlFor="patient-assignment-nutritionist">Registered nutritionist</label>
          <select
            id="patient-assignment-nutritionist"
            value={nutritionistEmail}
            onChange={(e) => setNutritionistEmail(e.target.value)}
            required
          >
            <option value="">Select a nutritionist</option>
            {nutritionists.map((nutritionist) => (
              <option key={nutritionist.email} value={nutritionist.email}>
                {nutritionist.name} ({nutritionist.email})
              </option>
            ))}
          </select>

          <button type="submit" disabled={!availablePatients.length || !nutritionists.length}>
            Assign Patient
          </button>
        </form>

        {!loading && patients.length === 0 && <p>No verified patient accounts are available yet.</p>}
        {!loading && patients.length > 0 && nutritionists.length === 0
          && <p>No registered nutritionist accounts are available to assign.</p>}
        {error && <p className="patient-management-message" role="alert">{error}</p>}
        {message && <p className="patient-management-message" role="status">{message}</p>}
      </div>
      <BackendRecords
        title="Registered Patient Assignments"
        records={patients}
        columns={[["name", "Name"], ["email", "Email"], ["nutritionistName", "Assigned Nutritionist"]]}
        loading={loading}
        error={error}
        emptyMessage="No registered patients are available."
      />
    </div>
  );
}

export default PatientManagement;