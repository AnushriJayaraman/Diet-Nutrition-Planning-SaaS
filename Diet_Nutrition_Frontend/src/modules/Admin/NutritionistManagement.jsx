import BackendRecords from "../../components/BackendRecords";
import useBackendRecords from "../../hooks/useBackendRecords";
import "./NutritionistManagement.css";

function NutritionistManagement() {
  const { records, loading, error } = useBackendRecords("/api/nutritionists", "nutritionists");

  return (
    <div className="nutritionist-page">
      <div className="nutritionist-header">
        <span>ADMIN MODULE</span>
        <h1>🥗 Nutritionist Management</h1>
        <p>View nutritionists who have registered for clinic access.</p>
      </div>

      <div className="nutritionist-card">
        <p>New nutritionist accounts appear here after signup. Account access can be managed in User Management.</p>
      </div>
      <BackendRecords
        title="Registered Nutritionists"
        records={records}
        columns={[["name", "Name"], ["email", "Email"], ["enabled", "Account enabled"]]}
        loading={loading}
        error={error}
        emptyMessage="No nutritionists have registered yet."
      />
    </div>
  );
}

export default NutritionistManagement;