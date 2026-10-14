import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./UserManagement.css";
import "../../components/BackendRecords.css";

function UserManagement() {
  const [records, setRecords] = useState([]);
  const [search, setSearch] = useState("");
  const [role, setRole] = useState("all");
  const [selectedUser, setSelectedUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadUsers = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await axios.get("/api/users", { params: { search, role } });
      setRecords(response.data.users);
    } catch (loadError) {
      setError(apiErrorMessage(loadError, "Unable to load registered accounts."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [search, role]);

  const openDetails = async (id) => {
    setMessage("");
    try {
      const response = await axios.get(`/api/users/${id}`);
      setSelectedUser(response.data.data);
    } catch (detailError) {
      setMessage(apiErrorMessage(detailError, "Unable to load account details."));
    }
  };

  const updateStatus = async () => {
    if (!selectedUser) return;
    const enabled = !selectedUser.enabled;
    setMessage("");
    try {
      const response = await axios.patch(`/api/users/${selectedUser.id}/status`, { enabled });
      setSelectedUser(response.data.data);
      await loadUsers();
      setMessage(`Account ${enabled ? "enabled" : "disabled"} successfully.`);
    } catch (statusError) {
      setMessage(apiErrorMessage(statusError, "Unable to update account status."));
    }
  };

  return (
    <div className="user-management-page">
      <div className="user-management-header">
        <span>ADMIN MODULE</span>
        <h1>👤 User Management</h1>
        <p>Review registered accounts, inspect account details, and manage clinic access.</p>
      </div>

      <section className="user-management-card">
        <h2>Registered Accounts</h2>
        <div className="user-management-filters">
          <label htmlFor="account-search">Search by name or email</label>
          <input
            id="account-search"
            type="search"
            placeholder="Search accounts"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
          <label htmlFor="account-role-filter">Filter by role</label>
          <select id="account-role-filter" value={role} onChange={(event) => setRole(event.target.value)}>
            <option value="all">All roles</option>
            <option value="patient">Patient</option>
            <option value="nutritionist">Nutritionist</option>
            <option value="admin">Admin</option>
          </select>
        </div>

        <section className="backend-records" aria-live="polite">
          {loading && <p>Loading registered accounts...</p>}
          {error && <p className="backend-records-error" role="alert">{error}</p>}
          {!loading && !error && records.length === 0 && <p>No registered accounts match these filters.</p>}
          {!loading && !error && records.length > 0 && (
            <div className="backend-records-table-wrap">
              <table className="backend-records-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Access</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {records.map((account) => (
                    <tr key={account.id}>
                      <td>{account.name}</td>
                      <td>{account.email}</td>
                      <td>{account.role}</td>
                      <td>{account.enabled ? "Active" : "Disabled"}</td>
                      <td>
                        <button type="button" onClick={() => openDetails(account.id)}>View details</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>

      {selectedUser && (
        <section className="user-management-card account-details">
          <h2>Account Details</h2>
          <dl>
            <dt>Name</dt><dd>{selectedUser.name}</dd>
            <dt>Email</dt><dd>{selectedUser.email}</dd>
            <dt>Role</dt><dd>{selectedUser.role}</dd>
            <dt>Access</dt><dd>{selectedUser.enabled ? "Active" : "Disabled"}</dd>
          </dl>
          <button type="button" onClick={updateStatus}>
            {selectedUser.enabled ? "Disable account" : "Enable account"}
          </button>
        </section>
      )}

      {message && <p className="user-management-message" role="status">{message}</p>}
    </div>
  );
}

export default UserManagement;
