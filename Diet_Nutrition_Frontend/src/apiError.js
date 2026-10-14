export default function apiErrorMessage(error, fallback) {
  if (error.response?.status === 401) {
    return "Your login session expired. Please sign in again.";
  }
  if (error.response?.status === 403) {
    return error.response.data?.message || "Your account does not have permission for this action.";
  }
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (!error.response) {
    return "Cannot reach the backend. Check that it is running and try again.";
  }
  return fallback;
}
