import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import MealEntryFields, { createMeal } from "../../components/MealEntryFields";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./PatientNutritionTracking.css";

const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

function PatientNutritionTracking() {
  const { records, loading, error, reload } =
    useBackendRecords("/api/patient-nutrition-tracking", "tracking");
  const [trackedDate, setTrackedDate] = useState(today());
  const [waterLitres, setWaterLitres] = useState("");
  const [notes, setNotes] = useState("");
  const [meals, setMeals] = useState([createMeal()]);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      await axios.post("/api/patient-nutrition-tracking", {
        trackedDate,
        waterLitres: Number(waterLitres),
        mealsCompleted: meals.length,
        notes,
        meals: meals.map(({ mealType, foodName, portion, calories, proteinGrams, carbohydratesGrams, fatGrams }) => ({
          mealType,
          foodName,
          portion,
          calories: Number(calories),
          proteinGrams: Number(proteinGrams),
          carbohydratesGrams: Number(carbohydratesGrams),
          fatGrams: Number(fatGrams),
        })),
      });
      await reload();
      setMessage("Nutrition and meal records saved successfully.");
      setTrackedDate(today());
      setWaterLitres("");
      setNotes("");
      setMeals([createMeal()]);
    } catch (saveError) {
      setMessage(apiErrorMessage(saveError, "Unable to save your nutrition tracking."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="patient-tracking-page">
      <div className="patient-tracking-header">
        <span>PATIENT MODULE</span>
        <h1>🍎 Nutrition Tracking</h1>
        <p>Record daily meals and water intake. Each date can be saved once.</p>
      </div>

      <div className="patient-tracking-card">
        <h2>My Daily Record</h2>
        <form onSubmit={handleSubmit}>
          <label htmlFor="patient-tracked-date">Record date</label>
          <input id="patient-tracked-date" type="date" value={trackedDate} onChange={(event) => setTrackedDate(event.target.value)} required />

          <label htmlFor="patient-water">Water intake (litres)</label>
          <input id="patient-water" type="number" step="0.1" min="0" placeholder="Enter water intake" value={waterLitres} onChange={(event) => setWaterLitres(event.target.value)} required />

          <MealEntryFields meals={meals} setMeals={setMeals} />

          <label htmlFor="patient-tracking-notes">Nutrition notes</label>
          <textarea id="patient-tracking-notes" placeholder="Optional notes about your day" value={notes} onChange={(event) => setNotes(event.target.value)} />

          <button type="submit" disabled={saving}>{saving ? "Saving..." : "Save Daily Record"}</button>
        </form>
        {message && <p className="patient-tracking-message" role="status">{message}</p>}
      </div>

      <BackendRecords
        title="Saved Nutrition and Meal History"
        records={records}
        columns={[
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
        emptyMessage="No nutrition tracking records have been saved yet."
        reverseRecords={false}
      />
      {!loading && !error && records.filter((record) => record.meals?.length).map((record) => (
        <section className="patient-tracking-card" key={`meals-${record.id}`}>
          <h2>Meal details — {record.trackedDate || "Earlier record"}</h2>
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

export default PatientNutritionTracking;
