import axios from "axios";

axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
axios.interceptors.request.use((config) => {
  try {
    const currentUser = JSON.parse(localStorage.getItem("currentUser") || "null");
    if (currentUser?.token) {
      config.headers.Authorization = `Bearer ${currentUser.token}`;
    }
  } catch {
    localStorage.removeItem("currentUser");
  }
  return config;
});

export default axios;
