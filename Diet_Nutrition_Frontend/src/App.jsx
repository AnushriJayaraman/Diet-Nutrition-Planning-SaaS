import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Navbar from "./components/Navbar";

import Home from "./pages/Home";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import AdminLogin from "./pages/AdminLogin";
import SignUp from "./pages/SignUp";
import RoleSelection from "./pages/RoleSelection";

import AdminDashboard from "./pages/Dashboard/AdminDashboard";
import NutritionistDashboard from "./pages/Dashboard/NutritionistDashboard";
import PatientDashboard from "./pages/Dashboard/PatientDashboard";

import NutritionistManagement from "./modules/Admin/NutritionistManagement";
import PatientManagement from "./modules/Admin/PatientManagement";
import Reports from "./modules/Admin/Reports";
import UserManagement from "./modules/Admin/UserManagement";
import AdminOverview from "./modules/Admin/Overview";
import DietPlan from "./modules/Nutritionist/DietPlan";
import NutritionTracking from "./modules/Nutritionist/NutritionTracking";
import NutritionistPatientManagement from "./modules/Nutritionist/PatientManagement";
import Progress from "./modules/Nutritionist/Progress";
import Insights from "./modules/Nutritionist/Insights";

import PatientDietPlan from "./modules/Patient/DietPlan";
import PatientBMI from "./modules/Patient/PatientBMI";
import PatientNutritionTracking from "./modules/Patient/PatientNutritionTracking";
import PatientProgress from "./modules/Patient/PatientProgress";
import Profile from "./modules/Patient/Profile";
import Appointments from "./modules/Appointments";
import NutritionistProfile from "./modules/Nutritionist/Profile";

const getDashboardRoute = () => {
  const storedUser = localStorage.getItem("currentUser");

  if (!storedUser) {
    return "/login";
  }

  try {
    const user = JSON.parse(storedUser);
    if (!user?.token) return "/login";
    if (user.role === "admin") return "/admin/dashboard";
    if (user.role === "nutritionist") return "/nutritionist/dashboard";
    if (user.role === "patient") return "/patient/dashboard";
  } catch (error) {
    localStorage.removeItem("currentUser");
  }

  return "/login";
};

function RequireRole({ role, children }) {
  try {
    const user = JSON.parse(localStorage.getItem("currentUser") || "null");
    return user?.token && user.role === role
      ? children
      : <Navigate to="/login" replace />;
  } catch {
    localStorage.removeItem("currentUser");
    return <Navigate to="/login" replace />;
  }
}

function App() {
  return (
    <BrowserRouter>
      <Navbar />

      <Routes>
        <Route path="/" element={<Home />} />

        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/role-selection" element={<RoleSelection />} />

        <Route path="/dashboard" element={<Navigate to={getDashboardRoute()} replace />} />

        <Route path="/admin/dashboard" element={<RequireRole role="admin"><AdminDashboard /></RequireRole>} />
        <Route path="/admin/overview" element={<RequireRole role="admin"><AdminOverview /></RequireRole>} />
        <Route path="/admin/nutritionists" element={<RequireRole role="admin"><NutritionistManagement /></RequireRole>} />
        <Route path="/admin/patients" element={<RequireRole role="admin"><PatientManagement /></RequireRole>} />
        <Route path="/admin/reports" element={<RequireRole role="admin"><Reports /></RequireRole>} />
        <Route path="/admin/users" element={<RequireRole role="admin"><UserManagement /></RequireRole>} />
        <Route path="/admin/appointments" element={<RequireRole role="admin"><Appointments role="admin" /></RequireRole>} />

        <Route path="/nutritionist/dashboard" element={<RequireRole role="nutritionist"><NutritionistDashboard /></RequireRole>} />
        <Route path="/nutritionist/bmi" element={<RequireRole role="nutritionist"><Navigate to="/nutritionist/patients" replace /></RequireRole>} />
        <Route path="/nutritionist/diet-plan" element={<RequireRole role="nutritionist"><DietPlan /></RequireRole>} />
        <Route path="/nutritionist/tracking" element={<RequireRole role="nutritionist"><NutritionTracking /></RequireRole>} />
        <Route path="/nutritionist/patients" element={<RequireRole role="nutritionist"><NutritionistPatientManagement /></RequireRole>} />
        <Route path="/nutritionist/progress" element={<RequireRole role="nutritionist"><Progress /></RequireRole>} />
        <Route path="/nutritionist/insights" element={<RequireRole role="nutritionist"><Insights /></RequireRole>} />
        <Route path="/nutritionist/appointments" element={<RequireRole role="nutritionist"><Appointments role="nutritionist" /></RequireRole>} />
        <Route path="/nutritionist/profile" element={<RequireRole role="nutritionist"><NutritionistProfile /></RequireRole>} />

        <Route path="/patient/dashboard" element={<RequireRole role="patient"><PatientDashboard /></RequireRole>} />
        <Route path="/patient/diet-plan" element={<RequireRole role="patient"><PatientDietPlan /></RequireRole>} />
        <Route path="/patient/bmi" element={<RequireRole role="patient"><PatientBMI /></RequireRole>} />
        <Route path="/patient/tracking" element={<RequireRole role="patient"><PatientNutritionTracking /></RequireRole>} />
        <Route path="/patient/progress" element={<RequireRole role="patient"><PatientProgress /></RequireRole>} />
        <Route path="/patient/profile" element={<RequireRole role="patient"><Profile /></RequireRole>} />
        <Route path="/patient/appointments" element={<RequireRole role="patient"><Appointments role="patient" /></RequireRole>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;