import { useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./Reports.css";

const reportTypes = [
  ["Complete Patient Report", "Complete Patient Report"],
  ["BMI and Progress Report", "BMI and Progress Report"],
  ["BMI Report", "BMI Report"],
  ["Patient Health Details", "Patient Health Details"],
  ["Nutrition Tracking Report", "Nutrition Tracking Report"],
  ["Diet Plan Report", "Diet Plan Report"],
  ["Appointment Report", "Appointment Report"],
];

function Reports() {
  const [modalOpen, setModalOpen] = useState(false);
  const [patients, setPatients] = useState([]);
  const [patientsLoading, setPatientsLoading] = useState(false);
  const [patientSearch, setPatientSearch] = useState("");
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [reportType, setReportType] = useState(reportTypes[0][0]);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const openReports = async () => {
    setModalOpen(true);
    setPatientsLoading(true);
    setMessage("");
    try {
      const response = await axios.get("/api/users", { params: { role: "patient" } });
      setPatients(response.data.users);
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to load registered patients."));
    } finally {
      setPatientsLoading(false);
    }
  };

  const closeReports = () => {
    setModalOpen(false);
    setReport(null);
    setMessage("");
  };

  const matchingPatients = patients.filter((patient) => {
    const search = patientSearch.trim().toLowerCase();
    return search && (
      patient.name.toLowerCase().includes(search)
      || patient.email.toLowerCase().includes(search)
    );
  });

  const viewSelectedReport = async (event) => {
    event.preventDefault();
    setMessage("");
    setReport(null);

    if (!selectedPatient) {
      setMessage("Search for and select a patient before viewing a report.");
      return;
    }
    setLoading(true);
    try {
      const response = await axios.get("/api/reports/patient", {
        params: {
          patientEmail: selectedPatient.email,
          reportType,
        },
      });
      setReport(response.data);
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to load the selected report."));
    } finally {
      setLoading(false);
    }
  };

  const reportSections = report && ["Complete Patient Report", "BMI and Progress Report"].includes(report.reportType)
    ? report.records
    : report ? [{ title: report.reportType, records: report.records }] : [];
  const hasReportRecords = reportSections.some((section) => section.records?.length);

  const showReportTable = (records) => (
    <div className="reports-table-wrap">
      <table>
        <thead>
          <tr>{Object.keys(records[0]).map((key) => <th key={key}>{key.replace(/([A-Z])/g, " $1")}</th>)}</tr>
        </thead>
        <tbody>
          {records.map((record, index) => (
            <tr key={record.id ?? index}>
              {Object.keys(records[0]).map((key) => {
                const value = record[key];
                return (
                  <td className="reports-long-text" key={key}>
                    {value == null || value === "" ? "—" : typeof value === "object" ? JSON.stringify(value) : String(value)}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  return (
    <div className="reports-page">
      <header className="reports-header">
        <span>ADMIN MODULE</span>
        <h1>Patient Reports</h1>
        <p>Review real health, BMI, nutrition, diet-plan, progress, and appointment records for a patient.</p>
      </header>

      <main className="reports-card">
        <div className="reports-card-mark" aria-hidden="true">+</div>
        <h2>Review a patient report</h2>
        <p>Select a patient and report type to view information saved in the clinic records.</p>
        <button type="button" className="reports-open-button" onClick={openReports}>
          View Reports
        </button>
      </main>

      {modalOpen && (
        <div
          className="reports-modal-backdrop"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeReports();
          }}
        >
          <section
            className="reports-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="reports-modal-title"
          >
            <div className="reports-modal-heading">
              <div>
                <span>ADMIN REPORTS</span>
                <h2 id="reports-modal-title">Choose report details</h2>
              </div>
              <button
                type="button"
                className="reports-close-icon"
                aria-label="Close reports"
                onClick={closeReports}
              >
                ×
              </button>
            </div>

            <form className="reports-form" onSubmit={viewSelectedReport}>
              <label htmlFor="report-patient-search">Patient name</label>
              <input
                id="report-patient-search"
                type="search"
                placeholder={patientsLoading ? "Loading registered patients..." : "Search by patient name or email"}
                value={patientSearch}
                onChange={(event) => {
                  setPatientSearch(event.target.value);
                  setSelectedPatient(null);
                  setReport(null);
                }}
                autoComplete="off"
                required
              />

              {selectedPatient ? (
                <div className="reports-selected-patient">
                  <span>Selected patient</span>
                  <strong>{selectedPatient.name}</strong>
                  <small>{selectedPatient.email}</small>
                </div>
              ) : (
                patientSearch.trim() && (
                  <div className="reports-patient-matches" role="listbox" aria-label="Matching patients">
                    {matchingPatients.length > 0 ? matchingPatients.map((patient) => (
                      <button
                        type="button"
                        key={patient.id}
                        role="option"
                        aria-selected="false"
                        onClick={() => {
                          setSelectedPatient(patient);
                          setPatientSearch(patient.name);
                          setReport(null);
                          setMessage("");
                        }}
                      >
                        <strong>{patient.name}</strong>
                        <span>{patient.email}</span>
                      </button>
                    )) : (
                      <p>{patientsLoading ? "Loading patients..." : "No matching patients found."}</p>
                    )}
                  </div>
                )
              )}

              {!patientSearch.trim() && !patientsLoading && patients.length === 0 && (
                <p className="reports-help">No registered patient accounts are available.</p>
              )}

              <label htmlFor="report-type">Report Type</label>
              <select
                id="report-type"
                value={reportType}
                onChange={(event) => {
                  setReportType(event.target.value);
                  setReport(null);
                }}
              >
                {reportTypes.map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>

              <p className="reports-help">Reports include only records saved for the selected patient.</p>

              {message && <p className="reports-message" role="alert">{message}</p>}

              <div className="reports-form-actions">
                <button type="submit" className="reports-open-button" disabled={loading || patientsLoading}>
                  {loading ? "Loading report..." : "View Selected Report"}
                </button>
                <button type="button" className="reports-cancel-button" onClick={closeReports}>
                  Cancel / Close
                </button>
              </div>
            </form>

            {report && (
              <section className="reports-result" aria-live="polite">
                <div className="reports-result-heading">
                  <h3>{report.reportType}</h3>
                  <p>{report.patientName} · {report.patientEmail}</p>
                </div>
                {!hasReportRecords ? (
                  <p className="reports-empty">No reports found for this patient and selection.</p>
                ) : reportSections.map((section) => (
                  <section className="reports-section" key={section.title}>
                    <h4>{section.title}</h4>
                    {section.records.length ? showReportTable(section.records) : <p className="reports-empty">No records saved.</p>}
                  </section>
                ))}
              </section>
            )}
          </section>
        </div>
      )}
    </div>
  );
}

export default Reports;
