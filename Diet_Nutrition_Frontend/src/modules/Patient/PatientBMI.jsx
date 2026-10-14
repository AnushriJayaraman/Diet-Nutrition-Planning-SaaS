import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./PatientBMI.css";

function PatientBMI() {
  const { records, loading, error, reload } = useBackendRecords("/api/patient-bmi", "assessments");
  const [height, setHeight] = useState("");
  const [weight, setWeight] = useState("");
  const [bmi, setBmi] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;
    axios.get("/api/patient-profile")
      .then(({ data }) => {
        if (!active || !data.profile) return;
        setHeight((current) => current || data.profile.heightCm || "");
        setWeight((current) => current || data.profile.currentWeightKg || "");
      })
      .catch((error) => {
        if (active && error.response?.status !== 404) {
          setMessage(apiErrorMessage(error, "BMI can still be entered, but profile values could not be loaded."));
        }
      });
    return () => { active = false; };
  }, []);

  const bmiCategory = (value) => {
    if (!Number.isFinite(Number(value)) || Number(value) <= 0) return "Not available";
    if (value < 18.5) return "Underweight";
    if (value < 25) return "Healthy range";
    if (value < 30) return "Overweight";
    return "Obesity range";
  };

  const calculateBMI = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post(
        "/api/patient-bmi",
        {
          heightCm: Number(height),
          weightKg: Number(weight),
        }
      );
      await reload();
      setBmi(response.data.data.bmi.toFixed(2));
      setMessage(`BMI information saved successfully. Category: ${bmiCategory(response.data.data.bmi)}.`);
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to save your BMI."));
    }
  };

  return (
    <div className="patient-bmi-page">
      <div className="patient-bmi-header">
        <span>PATIENT MODULE</span>

        <h1>⚖️ BMI Assessment</h1>

        <p>
          View and record your BMI assessment information.
        </p>
      </div>

      <div className="patient-bmi-card">
        <h2>My BMI Assessment</h2>

        <form onSubmit={calculateBMI}>
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
            step="0.1"
            min="1"
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
          <div className="patient-bmi-result">
            <span>Your BMI</span>
            <strong>{bmi}</strong>
            <span className="patient-bmi-category">{bmiCategory(Number(bmi))}</span>
            <small>BMI is a screening measure and is not a medical diagnosis.</small>
          </div>
        )}

        {message && (
          <p className="patient-bmi-message">
            {message}
          </p>
        )}
      </div>
      <BackendRecords
        title="Saved BMI History"
        records={records.map((record) => ({ ...record, category: bmiCategory(Number(record.bmi)) }))}
        columns={[["heightCm", "Height (cm)"], ["weightKg", "Weight (kg)"], ["bmi", "BMI"], ["category", "Category"], ["assessedAt", "Assessment date"]]}
        loading={loading}
        error={error}
        emptyMessage="No BMI records have been saved yet."
        reverseRecords={false}
      />
    </div>
  );
}

export default PatientBMI;