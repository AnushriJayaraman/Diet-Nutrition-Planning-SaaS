import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./PatientProgress.css";

function PatientProgress() {
  const { records, loading, error, reload } = useBackendRecords("/api/patient-progress", "progress");
  const { records: bmiRecords, loading: bmiLoading, error: bmiError } =
    useBackendRecords("/api/patient-bmi", "assessments");
  const { records: trackingRecords, loading: trackingLoading, error: trackingError } =
    useBackendRecords("/api/patient-nutrition-tracking", "tracking");
  const [currentWeight, setCurrentWeight] = useState("");
  const [targetWeight, setTargetWeight] = useState("");
  const [progress, setProgress] = useState("");
  const [notes, setNotes] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post(
        "/api/patient-progress",
        {
          currentWeight: Number(currentWeight),
          targetWeight: Number(targetWeight),
          progress,
          notes,
        }
      );
      await reload();

      setMessage("Progress information saved successfully.");

      setCurrentWeight("");
      setTargetWeight("");
      setProgress("");
      setNotes("");
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to save your progress."));
    }
  };

  return (
    <div className="patient-progress-page">
      <div className="patient-progress-header">
        <span>PATIENT MODULE</span>

        <h1>📈 My Progress</h1>

        <p>
          View and record your nutrition progress and health updates.
        </p>
      </div>

      <div className="patient-progress-card">
        <h2>My Progress Information</h2>

        <form onSubmit={handleSubmit}>
          <label>Current Weight (kg)</label>

          <input
            type="number"
            step="0.1"
            min="0.1"
            placeholder="Enter current weight"
            value={currentWeight}
            onChange={(e) => setCurrentWeight(e.target.value)}
            required
          />

          <label>Target Weight (kg)</label>

          <input
            type="number"
            step="0.1"
            min="0.1"
            placeholder="Enter target weight"
            value={targetWeight}
            onChange={(e) => setTargetWeight(e.target.value)}
            required
          />

          <label>Progress Status</label>

          <select
            value={progress}
            onChange={(e) => setProgress(e.target.value)}
            required
          >
            <option value="">Select progress status</option>
            <option value="Good Progress">Good Progress</option>
            <option value="Improving">Improving</option>
            <option value="Needs Attention">Needs Attention</option>
            <option value="No Change">No Change</option>
          </select>

          <label>Progress Notes</label>

          <textarea
            placeholder="Enter your progress notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
          />

          <button type="submit">
            Save Progress
          </button>
        </form>

        {message && (
          <p className="patient-progress-message">
            {message}
          </p>
        )}
      </div>
      <BackendRecords
        title="Saved Progress History"
        records={records}
        columns={[["currentWeight", "Current weight (kg)"], ["targetWeight", "Target weight (kg)"], ["progress", "Status"], ["recordedAt", "Recorded at"], ["notes", "Notes"]]}
        loading={loading}
        error={error}
        emptyMessage="No progress records have been saved yet."
        reverseRecords={false}
      />
      <BackendRecords
        title="BMI History"
        records={bmiRecords}
        columns={[["assessedAt", "Assessment date"], ["heightCm", "Height (cm)"], ["weightKg", "Weight (kg)"], ["bmi", "BMI"]]}
        loading={bmiLoading}
        error={bmiError}
        emptyMessage="No BMI assessments have been saved yet."
        reverseRecords={false}
      />
      <BackendRecords
        title="Nutrition History"
        records={trackingRecords}
        columns={[["trackedDate", "Date"], ["waterLitres", "Water (L)"], ["totalCalories", "Calories (kcal)"], ["totalProteinGrams", "Protein (g)"], ["totalCarbohydratesGrams", "Carbs (g)"], ["totalFatGrams", "Fat (g)"]]}
        loading={trackingLoading}
        error={trackingError}
        emptyMessage="No nutrition records have been saved yet."
        reverseRecords={false}
      />
    </div>
  );
}

export default PatientProgress;