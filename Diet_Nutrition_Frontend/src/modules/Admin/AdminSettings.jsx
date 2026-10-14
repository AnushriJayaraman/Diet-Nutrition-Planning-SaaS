import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./AdminSettings.css";

function AdminSettings() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    axios.get("/api/admin/profile")
      .then((response) => {
        const account = response.data.data;
        setName(account.name);
        setEmail(account.email);
      })
      .catch((loadError) => setError(apiErrorMessage(loadError, "Unable to load administrator settings.")))
      .finally(() => setLoading(false));
  }, []);

  const saveProfile = async (event) => {
    event.preventDefault();
    setSavingProfile(true);
    setMessage("");
    setError("");
    try {
      const response = await axios.patch("/api/admin/profile", { name });
      const updated = response.data.data;
      setName(updated.name);
      setMessage("Administrator profile saved.");
    } catch (saveError) {
      setError(apiErrorMessage(saveError, "Unable to save administrator profile."));
    } finally {
      setSavingProfile(false);
    }
  };

  const changePassword = async (event) => {
    event.preventDefault();
    setSavingPassword(true);
    setMessage("");
    setError("");
    try {
      const response = await axios.post("/api/auth/change-password", { currentPassword, newPassword });
      setMessage(response.data.message || "Password changed successfully.");
      setCurrentPassword("");
      setNewPassword("");
    } catch (saveError) {
      setError(apiErrorMessage(saveError, "Unable to change the password."));
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <div className="admin-settings-page">
      <header className="admin-settings-header">
        <span>ADMIN MODULE</span>
        <h1>Administrator Settings</h1>
        <p>Update your administrator display name and account password.</p>
      </header>
      {loading ? <p>Loading settings...</p> : (
        <div className="admin-settings-grid">
          <section className="admin-settings-card">
            <h2>Account Details</h2>
            <form onSubmit={saveProfile}>
              <label htmlFor="admin-setting-name">Name</label>
              <input id="admin-setting-name" value={name} onChange={(event) => setName(event.target.value)} required maxLength="120" />
              <label htmlFor="admin-setting-email">Email</label>
              <input id="admin-setting-email" type="email" value={email} readOnly />
              <button type="submit" disabled={savingProfile}>{savingProfile ? "Saving..." : "Save Profile"}</button>
            </form>
          </section>

          <section className="admin-settings-card">
            <h2>Change Password</h2>
            <form onSubmit={changePassword}>
              <label htmlFor="admin-current-password">Current password</label>
              <input id="admin-current-password" type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required />
              <label htmlFor="admin-new-password">New password</label>
              <input id="admin-new-password" type="password" autoComplete="new-password" minLength="8" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required />
              <p>Use at least 8 characters, with uppercase and lowercase letters, a number, and one of @$!%*?&amp;.</p>
              <button type="submit" disabled={savingPassword}>{savingPassword ? "Updating..." : "Change Password"}</button>
            </form>
          </section>
        </div>
      )}
      {error && <p className="admin-settings-error" role="alert">{error}</p>}
      {message && <p className="admin-settings-message" role="status">{message}</p>}
    </div>
  );
}

export default AdminSettings;
