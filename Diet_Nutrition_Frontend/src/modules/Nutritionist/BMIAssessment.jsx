import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./BMIAssessment.css";

function BMIAssessment() {
  const { records, loading, error, reload } = useBackendRecords("/api/bmi", "assessments");
  const { records: patients, loading: patientsLoading, error: patientsError } =
    useBackendRecords("/api/nutrition-patients", "patients");
  const [patientName, setPatientName] = useState("");
  const [patientEmail, setPatientEmail] = useState("");
  const [height, setHeight] = useState("");
  const [weight, setWeight] = useState("");
  const [bmi, setBmi] = useState("");
  const [message, setMessage] = useState("");

  const calculateBMI = async (e) => {
    e.preventDefault();

    const heightInMeter = Number(height) / 100;
    const calculatedBMI =
      Number(weight) / (heightInMeter * heightInMeter);

    setBmi(calculatedBMI.toFixed(2));

    try {
      await axios.post("/api/bmi", {
        patientName,
        patientEmail,
        heightCm: Number(height),
        weightKg: Number(weight),
        bmi: calculatedBMI,
      });
      await reload();
      setMessage("BMI assessment saved successfully.");
      setPatientName("");
      setPatientEmail("");
      setHeight("");
      setWeight("");
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to save the BMI assessment."));
    }
  };

  return (
    <div className="bmi-page">
      <div className="bmi-header">
        <span>NUTRITIONIST MODULE</span>

        <h1>⚖️ BMI Assessment</h1>

        <p>
          Calculate and record patient body mass information.
        </p>
      </div>

      <div className="bmi-card">
        <h2>Patient BMI Assessment</h2>

        <form onSubmit={calculateBMI}>
          <label>Assigned Patient</label>
          <select
            value={patientEmail}
            onChange={(event) => {
              const patient = patients.find((item) => item.email === event.target.value);
              setPatientEmail(event.target.value);
              setPatientName(patient?.patientName || "");
            }}
            required
          >
            <option value="">{patientsLoading ? "Loading patients..." : "Select assigned patient"}</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.email}>{patient.patientName} ({patient.email})</option>
            ))}
          </select>
          {patientsError && <p className="bmi-message">{patientsError}</p>}

          <label>Height (cm)</label>

          <input
            type="number"
            min="1"
            placeholder="Enter height"
            value={height}
            onChange={(e) => setHeight(e.target.value)}
            required
          />

          <label>Weight (kg)</label>

          <input
            type="number"
            min="0.1"
            placeholder="Enter weight"
            value={weight}
            onChange={(e) => setWeight(e.target.value)}
            required
          />

          <button type="submit">
            Calculate BMI
          </button>
        </form>

        {bmi && (
          <div className="bmi-result">
            <span>Your BMI</span>
            <strong>{bmi}</strong>
          </div>
        )}

        {message && (
          <p className="bmi-message">
            {message}
          </p>
        )}
      </div>
      <BackendRecords
        title="Saved BMI Assessments"
        records={records}
          columns={[["patientName", "Patient"], ["patientEmail", "Patient email"], ["heightCm", "Height (cm)"], ["weightKg", "Weight (kg)"], ["bmi", "BMI"], ["assessedAt", "Assessment date"]]}
        loading={loading}
        error={error}
        emptyMessage="No BMI assessments have been saved yet."
        reverseRecords={false}
      />
    </div>
  );
}

export default BMIAssessment;