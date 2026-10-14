import { useEffect, useState } from "react";
import axios from "../../api";
import "./DietPlan.css";

function PatientDietPlan() {
  const [dietPlans, setDietPlans] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const loadDietPlans = async () => {
      try {
        const response = await axios.get("/api/diet-plans");
        setDietPlans(response.data.dietPlans || []);
      } catch (error) {
        setMessage("Unable to load diet plans. Check that the backend is running.");
      }
    };

    loadDietPlans();
  }, []);

  return (
    <div className="patient-diet-page">
      <div className="patient-diet-header">
        <span>PATIENT MODULE</span>
        <h1>Personalized Diet Plan</h1>
        <p>Review the nutrition plan prepared for your health goals.</p>
      </div>

      <div className="patient-diet-content">
        {dietPlans.length === 0 && !message && (
          <div className="patient-diet-empty">
            <div className="patient-diet-mark">+</div>
            <h2>Your plan is being prepared</h2>
            <p>Your nutritionist has not published a diet plan yet.</p>
          </div>
        )}

        {dietPlans.slice().reverse().map((dietPlan) => (
          <article className="patient-diet-card" key={dietPlan.id}>
            <div className="patient-diet-card-top">
              <div>
                <span>PLAN FOR</span>
                <h2>{dietPlan.patientName}</h2>
              </div>
              <div className="patient-diet-meta">
                <strong>{dietPlan.goal}</strong>
                {dietPlan.createdAt && <span>Created {new Date(dietPlan.createdAt).toLocaleDateString()}</span>}
              </div>
            </div>
            {[dietPlan.calorieTarget, dietPlan.proteinTargetGrams, dietPlan.carbohydratesTargetGrams, dietPlan.fatTargetGrams]
              .some((target) => target != null) && (
              <div className="patient-diet-calorie-target">
                {dietPlan.calorieTarget != null && <><strong>{dietPlan.calorieTarget} kcal</strong> daily calorie target</>}
                {dietPlan.proteinTargetGrams != null && <span> · Protein {dietPlan.proteinTargetGrams} g</span>}
                {dietPlan.carbohydratesTargetGrams != null && <span> · Carbohydrates {dietPlan.carbohydratesTargetGrams} g</span>}
                {dietPlan.fatTargetGrams != null && <span> · Fat {dietPlan.fatTargetGrams} g</span>}
              </div>
            )}
            {["breakfast", "lunch", "dinner", "snacks"].some((meal) => dietPlan[meal]) && (
              <div className="patient-diet-meals">
                {[
                  ["breakfast", "Breakfast"],
                  ["lunch", "Lunch"],
                  ["dinner", "Dinner"],
                  ["snacks", "Snacks"],
                ].filter(([field]) => dietPlan[field]).map(([field, label]) => (
                  <section key={field}>
                    <h3>{label}</h3>
                    <p>{dietPlan[field]}</p>
                    {dietPlan[`${field}Portion`] && <p><strong>Portion:</strong> {dietPlan[`${field}Portion`]}</p>}
                    {dietPlan[`${field}Time`] && <p><strong>Suggested time:</strong> {dietPlan[`${field}Time`]}</p>}
                  </section>
                ))}
              </div>
            )}
            <div className="patient-diet-plan">
              <h3>Nutrition plan</h3>
              <p>{dietPlan.plan}</p>
            </div>
            {dietPlan.notes && (
              <div className="patient-diet-notes">
                <h3>Nutritionist notes</h3>
                <p>{dietPlan.notes}</p>
              </div>
            )}
          </article>
        ))}
        {message && <p className="patient-diet-message">{message}</p>}
      </div>
    </div>
  );
}

export default PatientDietPlan;
