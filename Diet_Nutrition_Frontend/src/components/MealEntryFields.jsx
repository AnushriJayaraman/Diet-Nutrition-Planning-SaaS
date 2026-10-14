import "./MealEntryFields.css";

let nextMealKey = 1;

function createMeal() {
  return {
    rowKey: nextMealKey++,
    mealType: "breakfast",
    foodName: "",
    portion: "",
    calories: "",
    proteinGrams: "",
    carbohydratesGrams: "",
    fatGrams: "",
  };
}

function MealEntryFields({ meals, setMeals }) {
  const updateMeal = (index, field, value) => {
    setMeals((current) => current.map((meal, mealIndex) =>
      mealIndex === index ? { ...meal, [field]: value } : meal));
  };

  const totals = meals.reduce((sum, meal) => ({
    calories: sum.calories + Number(meal.calories || 0),
    proteinGrams: sum.proteinGrams + Number(meal.proteinGrams || 0),
    carbohydratesGrams: sum.carbohydratesGrams + Number(meal.carbohydratesGrams || 0),
    fatGrams: sum.fatGrams + Number(meal.fatGrams || 0),
  }), { calories: 0, proteinGrams: 0, carbohydratesGrams: 0, fatGrams: 0 });

  return (
    <section className="meal-entry-section">
      <div className="meal-entry-heading">
        <div>
          <h3>Food entries</h3>
          <p>Enter nutrition values from the food label or your trusted source.</p>
        </div>
        <button type="button" onClick={() => setMeals((current) => [...current, createMeal()])}>
          Add food
        </button>
      </div>

      {meals.map((meal, index) => (
        <fieldset className="meal-entry-card" key={meal.rowKey}>
          <legend>Food {index + 1}</legend>
          <label>
            Meal
            <select value={meal.mealType} onChange={(event) => updateMeal(index, "mealType", event.target.value)}>
              <option value="breakfast">Breakfast</option>
              <option value="lunch">Lunch</option>
              <option value="dinner">Dinner</option>
              <option value="snack">Snack</option>
            </select>
          </label>
          <label>
            Food
            <input value={meal.foodName} onChange={(event) => updateMeal(index, "foodName", event.target.value)} required />
          </label>
          <label>
            Portion
            <input value={meal.portion} placeholder="e.g. 1 cup" onChange={(event) => updateMeal(index, "portion", event.target.value)} required />
          </label>
          <label>
            Calories (kcal)
            <input type="number" min="0" step="0.1" value={meal.calories} onChange={(event) => updateMeal(index, "calories", event.target.value)} required />
          </label>
          <label>
            Protein (g)
            <input type="number" min="0" step="0.1" value={meal.proteinGrams} onChange={(event) => updateMeal(index, "proteinGrams", event.target.value)} required />
          </label>
          <label>
            Carbohydrates (g)
            <input type="number" min="0" step="0.1" value={meal.carbohydratesGrams} onChange={(event) => updateMeal(index, "carbohydratesGrams", event.target.value)} required />
          </label>
          <label>
            Fat (g)
            <input type="number" min="0" step="0.1" value={meal.fatGrams} onChange={(event) => updateMeal(index, "fatGrams", event.target.value)} required />
          </label>
          {meals.length > 1 && (
            <button type="button" className="meal-entry-remove" onClick={() => setMeals((current) => current.filter((_, mealIndex) => mealIndex !== index))}>
              Remove
            </button>
          )}
        </fieldset>
      ))}

      <div className="meal-entry-totals">
        <strong>Totals for these entries</strong>
        <span>{totals.calories.toFixed(1)} kcal</span>
        <span>Protein {totals.proteinGrams.toFixed(1)} g</span>
        <span>Carbs {totals.carbohydratesGrams.toFixed(1)} g</span>
        <span>Fat {totals.fatGrams.toFixed(1)} g</span>
      </div>
      <p className="meal-entry-note">Totals are calculated from the nutrition values you enter. No food database lookup is used.</p>
    </section>
  );
}

export { createMeal };
export default MealEntryFields;
