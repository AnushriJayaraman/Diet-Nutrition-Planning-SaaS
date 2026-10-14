import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./DietPlan.css";

const emptyPlan = {
  patientName: "",
  patientEmail: "",
  goal: "",
  plan: "",
  notes: "",
  breakfast: "",
  lunch: "",
  dinner: "",
  snacks: "",
  calorieTarget: "",
  proteinTargetGrams: "",
  carbohydratesTargetGrams: "",
  fatTargetGrams: "",
  breakfastPortion: "",
  breakfastTime: "",
  lunchPortion: "",
  lunchTime: "",
  dinnerPortion: "",
  dinnerTime: "",
  snacksPortion: "",
  snacksTime: "",
};

function DietPlan() {
  const { records, loading, error, reload } = useBackendRecords("/api/diet-plans", "dietPlans");
  const { records: patients, loading: patientsLoading, error: patientsError } =
    useBackendRecords("/api/nutrition-patients", "patients");
  const [form, setForm] = useState(emptyPlan);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const update = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
  };

  const resetForm = () => {
    setForm(emptyPlan);
    setEditingId(null);
  };

  const editPlan = (plan) => {
    setForm({
      patientName: plan.patientName || "",
      patientEmail: plan.patientEmail || "",
      goal: plan.goal || "",
      plan: plan.plan || "",
      notes: plan.notes || "",
      breakfast: plan.breakfast || "",
      lunch: plan.lunch || "",
      dinner: plan.dinner || "",
      snacks: plan.snacks || "",
      calorieTarget: plan.calorieTarget ?? "",
      proteinTargetGrams: plan.proteinTargetGrams ?? "",
      carbohydratesTargetGrams: plan.carbohydratesTargetGrams ?? "",
      fatTargetGrams: plan.fatTargetGrams ?? "",
      breakfastPortion: plan.breakfastPortion || "",
      breakfastTime: plan.breakfastTime || "",
      lunchPortion: plan.lunchPortion || "",
      lunchTime: plan.lunchTime || "",
      dinnerPortion: plan.dinnerPortion || "",
      dinnerTime: plan.dinnerTime || "",
      snacksPortion: plan.snacksPortion || "",
      snacksTime: plan.snacksTime || "",
    });
    setEditingId(plan.id);
    setMessage("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    const patient = patients.find((item) => item.email === form.patientEmail);
    const payload = {
      ...form,
      patientName: patient?.patientName || form.patientName,
      calorieTarget: form.calorieTarget === "" ? null : Number(form.calorieTarget),
      proteinTargetGrams: form.proteinTargetGrams === "" ? null : Number(form.proteinTargetGrams),
      carbohydratesTargetGrams: form.carbohydratesTargetGrams === "" ? null : Number(form.carbohydratesTargetGrams),
      fatTargetGrams: form.fatTargetGrams === "" ? null : Number(form.fatTargetGrams),
    };
    try {
      if (editingId) {
        await axios.put(`/api/diet-plans/${editingId}`, payload);
      } else {
        await axios.post("/api/diet-plans", payload);
      }
      await reload();
      setMessage(editingId ? "Diet plan updated successfully." : "Diet plan saved successfully.");
      resetForm();
    } catch (saveError) {
      setMessage(apiErrorMessage(saveError, "Unable to save the diet plan."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="diet-plan-page">
      <div className="diet-plan-header">
        <span>NUTRITIONIST MODULE</span>
        <h1>🥗 Diet Plan</h1>
        <p>Create and manage personalized plans for patients assigned to you.</p>
      </div>

      <div className="diet-plan-card">
        <h2>{editingId ? "Edit Diet Plan" : "Create Diet Plan"}</h2>
        <form onSubmit={handleSubmit}>
          <label htmlFor="plan-patient">Assigned Patient</label>
          <select
            id="plan-patient"
            value={form.patientEmail}
            onChange={(event) => {
              const patient = patients.find((item) => item.email === event.target.value);
              setForm((current) => ({
                ...current,
                patientEmail: event.target.value,
                patientName: patient?.patientName || "",
              }));
            }}
            required
            disabled={Boolean(editingId)}
          >
            <option value="">{patientsLoading ? "Loading patients..." : "Select assigned patient"}</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.email}>{patient.patientName} ({patient.email})</option>
            ))}
          </select>
          {patientsError && <p className="diet-plan-message" role="alert">{patientsError}</p>}

          <label htmlFor="plan-goal">Nutrition Goal</label>
          <select id="plan-goal" value={form.goal} onChange={update("goal")} required>
            <option value="">Select goal</option>
            <option value="Healthy Nutrition">Healthy Nutrition</option>
            <option value="Balanced Diet">Balanced Diet</option>
            <option value="Medical Nutrition">Medical Nutrition</option>
          </select>

          <label htmlFor="plan-calories">Daily calorie target (optional)</label>
          <input id="plan-calories" type="number" min="1" step="1" value={form.calorieTarget} onChange={update("calorieTarget")} />

          <label htmlFor="plan-protein">Daily protein target (g, optional)</label>
          <input id="plan-protein" type="number" min="0.1" step="0.1" value={form.proteinTargetGrams} onChange={update("proteinTargetGrams")} />

          <label htmlFor="plan-carbohydrates">Daily carbohydrate target (g, optional)</label>
          <input id="plan-carbohydrates" type="number" min="0.1" step="0.1" value={form.carbohydratesTargetGrams} onChange={update("carbohydratesTargetGrams")} />

          <label htmlFor="plan-fat">Daily fat target (g, optional)</label>
          <input id="plan-fat" type="number" min="0.1" step="0.1" value={form.fatTargetGrams} onChange={update("fatTargetGrams")} />

          <label htmlFor="plan-breakfast">Breakfast</label>
          <textarea id="plan-breakfast" placeholder="Meal suggestions and portion guidance" value={form.breakfast} onChange={update("breakfast")} />
          <div className="diet-plan-meal-fields">
            <label htmlFor="plan-breakfast-portion">Breakfast portion guidance</label>
            <input id="plan-breakfast-portion" value={form.breakfastPortion} onChange={update("breakfastPortion")} placeholder="For example, 1 cup or 2 servings" />
            <label htmlFor="plan-breakfast-time">Suggested time</label>
            <input id="plan-breakfast-time" type="time" value={form.breakfastTime} onChange={update("breakfastTime")} />
          </div>

          <label htmlFor="plan-lunch">Lunch</label>
          <textarea id="plan-lunch" placeholder="Meal suggestions and portion guidance" value={form.lunch} onChange={update("lunch")} />
          <div className="diet-plan-meal-fields">
            <label htmlFor="plan-lunch-portion">Lunch portion guidance</label>
            <input id="plan-lunch-portion" value={form.lunchPortion} onChange={update("lunchPortion")} placeholder="For example, 1 cup or 2 servings" />
            <label htmlFor="plan-lunch-time">Suggested time</label>
            <input id="plan-lunch-time" type="time" value={form.lunchTime} onChange={update("lunchTime")} />
          </div>

          <label htmlFor="plan-dinner">Dinner</label>
          <textarea id="plan-dinner" placeholder="Meal suggestions and portion guidance" value={form.dinner} onChange={update("dinner")} />
          <div className="diet-plan-meal-fields">
            <label htmlFor="plan-dinner-portion">Dinner portion guidance</label>
            <input id="plan-dinner-portion" value={form.dinnerPortion} onChange={update("dinnerPortion")} placeholder="For example, 1 cup or 2 servings" />
            <label htmlFor="plan-dinner-time">Suggested time</label>
            <input id="plan-dinner-time" type="time" value={form.dinnerTime} onChange={update("dinnerTime")} />
          </div>

          <label htmlFor="plan-snacks">Snacks</label>
          <textarea id="plan-snacks" placeholder="Snack suggestions" value={form.snacks} onChange={update("snacks")} />
          <div className="diet-plan-meal-fields">
            <label htmlFor="plan-snacks-portion">Snack portion guidance</label>
            <input id="plan-snacks-portion" value={form.snacksPortion} onChange={update("snacksPortion")} placeholder="For example, 1 fruit" />
            <label htmlFor="plan-snacks-time">Suggested time</label>
            <input id="plan-snacks-time" type="time" value={form.snacksTime} onChange={update("snacksTime")} />
          </div>

          <label htmlFor="plan-details">Plan summary</label>
          <textarea id="plan-details" placeholder="Summarize the patient's diet plan and guidance" value={form.plan} onChange={update("plan")} required />

          <label htmlFor="plan-notes">Additional Notes</label>
          <textarea id="plan-notes" placeholder="Notes for the patient" value={form.notes} onChange={update("notes")} />

          <button type="submit" disabled={saving}>{saving ? "Saving..." : editingId ? "Update Diet Plan" : "Save Diet Plan"}</button>
          {editingId && <button type="button" className="diet-plan-cancel" onClick={resetForm}>Cancel edit</button>}
        </form>
        {message && <p className="diet-plan-message" role="status">{message}</p>}
      </div>

      <BackendRecords
        title="Saved Diet Plans"
        records={records}
        columns={[
          ["patientName", "Patient"],
          ["goal", "Goal"],
          ["calorieTarget", "Calories/day"],
          ["breakfast", "Breakfast"],
          ["lunch", "Lunch"],
          ["dinner", "Dinner"],
          ["snacks", "Snacks"],
          ["createdAt", "Created"],
        ]}
        loading={loading}
        error={error}
        emptyMessage="No diet plans have been saved yet."
      />
      {!loading && !error && records.length > 0 && (
        <section className="diet-plan-card diet-plan-edit-list">
          <h2>Manage saved plans</h2>
          {records.slice().reverse().map((plan) => (
            <div className="diet-plan-edit-row" key={plan.id}>
              <span><strong>{plan.patientName}</strong> — {plan.goal}</span>
              <button type="button" onClick={() => editPlan(plan)}>Edit plan</button>
            </div>
          ))}
        </section>
      )}
    </div>
  );
}

export default DietPlan;
