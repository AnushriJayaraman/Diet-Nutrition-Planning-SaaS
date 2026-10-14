import { useState } from "react";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./NutritionTracking.css";

const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

function NutritionTracking() {
  const { records, loading, error } = useBackendRecords("/api/nutrition-tracking", "tracking");
  const { records: patients, loading: patientsLoading, error: patientsError } =
    useBackendRecords("/api/nutrition-patients", "patients");
  const [patientName, setPatientName] = useState("");
  const [patientEmail, setPatientEmail] = useState("");
  const [trackedDate, setTrackedDate] = useState(today());

  const filteredRecords = patientEmail
    ? records.filter((record) => record.patientEmail === patientEmail)
    : records;
  const selectedDateRecords = trackedDate
    ? filteredRecords.filter((record) => record.trackedDate === trackedDate)
    : filteredRecords;

  return (
    <div className="nutrition-tracking-page">
      <div className="nutrition-tracking-header">
        <span>NUTRITIONIST MODULE</span>
        <h1>📈 Nutrition Tracking</h1>
        <p>Review meal and hydration records submitted by your assigned patients.</p>
      </div>

      <div className="nutrition-tracking-card">
        <h2>Patient Nutrition Records</h2>
        <div className="nutrition-tracking-filters">
          <label htmlFor="tracking-patient">Filter by assigned patient</label>
          <select
            id="tracking-patient"
            value={patientEmail}
            onChange={(event) => {
              const patient = patients.find((item) => item.email === event.target.value);
              setPatientEmail(event.target.value);
              setPatientName(patient?.patientName || "");
            }}
          >
            <option value="">{patientsLoading ? "Loading patients..." : "All assigned patients"}</option>
            {patients.map((patient) => <option key={patient.id} value={patient.email}>{patient.patientName} ({patient.email})</option>)}
          </select>
          {patientsError && <p className="nutrition-tracking-message" role="alert">{patientsError}</p>}

          <label htmlFor="nutritionist-tracked-date">Record date</label>
          <input id="nutritionist-tracked-date" type="date" value={trackedDate} onChange={(event) => setTrackedDate(event.target.value)} />
        </div>
      </div>

      <BackendRecords
        title={patientEmail ? `Saved nutrition history for ${patientName || patientEmail}` : "Assigned patient nutrition history"}
        records={selectedDateRecords}
        columns={[
          ["patientName", "Patient"],
          ["trackedDate", "Date"],
          ["waterLitres", "Water (L)"],
          ["mealsCompleted", "Meal entries"],
          ["totalCalories", "Calories (kcal)"],
          ["totalProteinGrams", "Protein (g)"],
          ["totalCarbohydratesGrams", "Carbs (g)"],
          ["totalFatGrams", "Fat (g)"],
          ["notes", "Notes"],
        ]}
        loading={loading}
        error={error}
        emptyMessage="No food logs recorded for this date."
        reverseRecords={false}
      />
      {!loading && !error && selectedDateRecords.filter((record) => record.meals?.length).map((record) => (
        <section className="nutrition-tracking-card" key={`meals-${record.id}`}>
          <h2>Meal details — {record.patientName}, {record.trackedDate}</h2>
          <div className="backend-records-table-wrap">
            <table className="backend-records-table">
              <thead><tr><th>Meal</th><th>Food</th><th>Portion</th><th>Calories</th><th>Protein</th><th>Carbs</th><th>Fat</th></tr></thead>
              <tbody>{record.meals.map((meal) => (
                <tr key={meal.id}>
                  <td>{meal.mealType}</td><td>{meal.foodName}</td><td>{meal.portion}</td>
                  <td>{meal.calories}</td><td>{meal.proteinGrams} g</td>
                  <td>{meal.carbohydratesGrams} g</td><td>{meal.fatGrams} g</td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        </section>
      ))}
    </div>
  );
}

export default NutritionTracking;
