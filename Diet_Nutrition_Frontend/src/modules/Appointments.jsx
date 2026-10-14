import { useEffect, useState } from "react";
import axios from "../api";
import apiErrorMessage from "../apiError";
import useBackendRecords from "../hooks/useBackendRecords";
import "../components/BackendRecords.css";
import "./Appointments.css";

const localDate = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

const weekdays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
const defaultAvailability = weekdays.map((_, index) => ({
  dayOfWeek: index + 1,
  startTime: "09:00",
  endTime: "17:00",
  active: false,
}));

function Appointments({ role }) {
  const { records, loading, error, reload } = useBackendRecords("/api/appointments", "appointments");
  const [nutritionists, setNutritionists] = useState([]);
  const [nutritionistEmail, setNutritionistEmail] = useState("");
  const [appointmentDate, setAppointmentDate] = useState(localDate());
  const [slots, setSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState("");
  const [availability, setAvailability] = useState(defaultAvailability);
  const [patientNote, setPatientNote] = useState("");
  const [consultationNotes, setConsultationNotes] = useState({});
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (role !== "patient") return undefined;
    let active = true;
    axios.get("/api/appointment-options")
      .then((response) => {
        if (active) setNutritionists(response.data.nutritionists || []);
      })
      .catch((loadError) => {
        if (active) setMessage(apiErrorMessage(loadError, "Unable to load your assigned nutritionist."));
      });
    return () => { active = false; };
  }, [role]);

  useEffect(() => {
    if (role !== "nutritionist") return undefined;
    let active = true;
    axios.get("/api/nutritionist/availability")
      .then(({ data }) => {
        if (!active) return;
        const savedByDay = new Map((data.availability || []).map((day) => [day.dayOfWeek, day]));
        setAvailability(defaultAvailability.map((day) => ({ ...day, ...savedByDay.get(day.dayOfWeek) })));
      })
      .catch((loadError) => {
        if (active) setMessage(apiErrorMessage(loadError, "Unable to load your weekly availability."));
      });
    return () => { active = false; };
  }, [role]);

  useEffect(() => {
    if (role !== "patient" || !nutritionistEmail || !appointmentDate) {
      setSlots([]);
      setSelectedSlot("");
      return undefined;
    }
    let active = true;
    setSlots([]);
    setSelectedSlot("");
    axios.get("/api/appointment-slots", {
      params: { nutritionistEmail, date: appointmentDate },
    })
      .then(({ data }) => {
        if (active) setSlots(data.slots || []);
      })
      .catch((loadError) => {
        if (active) setMessage(apiErrorMessage(loadError, "Unable to load available appointment slots."));
      });
    return () => { active = false; };
  }, [role, nutritionistEmail, appointmentDate]);

  const updateStatus = async (appointment, status) => {
    setSaving(true);
    setMessage("");
    try {
      await axios.patch(`/api/appointments/${appointment.id}`, {
        status,
        consultationNote: consultationNotes[appointment.id] || "",
      });
      await reload();
      setMessage(`Appointment ${status.toLowerCase()} successfully.`);
    } catch (updateError) {
      setMessage(apiErrorMessage(updateError, "Unable to update the appointment."));
    } finally {
      setSaving(false);
    }
  };

  const book = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      await axios.post("/api/appointments", { nutritionistEmail, startsAt: selectedSlot, patientNote });
      await reload();
      setMessage("Appointment request sent successfully.");
      setPatientNote("");
      setSelectedSlot("");
    } catch (bookingError) {
      setMessage(apiErrorMessage(bookingError, "Unable to request the appointment."));
    } finally {
      setSaving(false);
    }
  };

  const saveAvailability = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      const { data } = await axios.put("/api/nutritionist/availability", { availability });
      const savedByDay = new Map((data.availability || []).map((day) => [day.dayOfWeek, day]));
      setAvailability(defaultAvailability.map((day) => ({ ...day, ...savedByDay.get(day.dayOfWeek) })));
      setMessage("Weekly appointment availability saved.");
    } catch (saveError) {
      setMessage(apiErrorMessage(saveError, "Unable to save your weekly availability."));
    } finally {
      setSaving(false);
    }
  };

  const isPast = (dateTime) => new Date(dateTime) <= new Date();
  const title = role === "patient" ? "My Appointments" : role === "admin" ? "Clinic Appointments" : "Patient Appointments";

  return (
    <div className="appointments-page">
      <header className="appointments-header">
        <span>{role.toUpperCase()} MODULE</span>
        <h1>{title}</h1>
        <p>{role === "patient"
          ? "Request a visit with your assigned nutritionist and follow its status."
          : "Review appointment requests and keep appointment status up to date."}</p>
      </header>

      {role === "patient" && (
        <section className="appointments-card">
          <h2>Request an Appointment</h2>
          {nutritionists.length === 0 && <p>Your account does not currently have an active assigned nutritionist.</p>}
          <form onSubmit={book}>
            <label htmlFor="appointment-nutritionist">Assigned nutritionist</label>
            <select id="appointment-nutritionist" value={nutritionistEmail} onChange={(event) => setNutritionistEmail(event.target.value)} required>
              <option value="">Select your nutritionist</option>
              {nutritionists.map((nutritionist) => (
                <option key={nutritionist.id} value={nutritionist.email}>{nutritionist.name}</option>
              ))}
            </select>
            <label htmlFor="appointment-date">Appointment date</label>
            <input id="appointment-date" type="date" min={localDate()} value={appointmentDate} onChange={(event) => setAppointmentDate(event.target.value)} required />
            <label htmlFor="appointment-slot">Available time (15 minutes)</label>
            <select id="appointment-slot" value={selectedSlot} onChange={(event) => setSelectedSlot(event.target.value)} required>
              <option value="">{slots.length ? "Select an available time" : "No available times for this date"}</option>
              {slots.map((slot) => (
                <option key={slot} value={slot}>{new Date(slot).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</option>
              ))}
            </select>
            <label htmlFor="appointment-note">Note for the nutritionist (optional)</label>
            <textarea id="appointment-note" value={patientNote} onChange={(event) => setPatientNote(event.target.value)} />
            <button type="submit" disabled={saving || nutritionists.length === 0 || !selectedSlot}>Request Appointment</button>
          </form>
        </section>
      )}

      {role === "nutritionist" && (
        <section className="appointments-card">
          <h2>Weekly Appointment Availability</h2>
          <p>Choose the weekdays and working hours patients can book. Time ranges use 15-minute slots.</p>
          <form onSubmit={saveAvailability} className="availability-form">
            {availability.map((day) => (
              <div className="availability-row" key={day.dayOfWeek}>
                <label>
                  <input
                    type="checkbox"
                    checked={day.active}
                    onChange={(event) => setAvailability((current) => current.map((item) =>
                      item.dayOfWeek === day.dayOfWeek ? { ...item, active: event.target.checked } : item))}
                  />
                  {weekdays[day.dayOfWeek - 1]}
                </label>
                <input
                  aria-label={`${weekdays[day.dayOfWeek - 1]} start time`}
                  type="time"
                  step="900"
                  value={day.startTime}
                  onChange={(event) => setAvailability((current) => current.map((item) =>
                    item.dayOfWeek === day.dayOfWeek ? { ...item, startTime: event.target.value } : item))}
                  required
                />
                <span>to</span>
                <input
                  aria-label={`${weekdays[day.dayOfWeek - 1]} end time`}
                  type="time"
                  step="900"
                  value={day.endTime}
                  onChange={(event) => setAvailability((current) => current.map((item) =>
                    item.dayOfWeek === day.dayOfWeek ? { ...item, endTime: event.target.value } : item))}
                  required
                />
              </div>
            ))}
            <button type="submit" disabled={saving}>{saving ? "Saving..." : "Save Availability"}</button>
          </form>
        </section>
      )}

      {message && <p className="appointments-message" role="status">{message}</p>}
      <section className="appointments-card">
        <h2>Appointment History</h2>
        {loading && <p>Loading appointments...</p>}
        {error && <p className="appointments-error" role="alert">{error}</p>}
        {!loading && !error && records.length === 0 && <p>No appointments have been requested yet.</p>}
        {!loading && !error && records.length > 0 && (
          <div className="appointments-list">
            {records.map((appointment) => (
              <article className="appointment-item" key={appointment.id}>
                <div className="appointment-summary">
                  <div>
                    <h3>{role === "patient" ? appointment.nutritionistName : appointment.patientName}</h3>
                    <p>{role === "patient" ? appointment.nutritionistEmail : appointment.patientEmail}</p>
                  </div>
                  <span className={`appointment-status status-${appointment.status.toLowerCase()}`}>{appointment.status}</span>
                </div>
                <dl>
                  <dt>Date and time</dt><dd>{new Date(appointment.startsAt).toLocaleString()}</dd>
                  {appointment.patientNote && <><dt>Patient note</dt><dd>{appointment.patientNote}</dd></>}
                  {appointment.consultationNote && <><dt>Nutritionist note</dt><dd>{appointment.consultationNote}</dd></>}
                </dl>
                {role === "nutritionist" && appointment.status !== "Cancelled" && appointment.status !== "Completed" && (
                  <>
                    <label htmlFor={`consult-note-${appointment.id}`}>Consultation note</label>
                    <textarea
                      id={`consult-note-${appointment.id}`}
                      value={consultationNotes[appointment.id] ?? appointment.consultationNote ?? ""}
                      onChange={(event) => setConsultationNotes((current) => ({ ...current, [appointment.id]: event.target.value }))}
                    />
                    <div className="appointment-actions">
                      {appointment.status === "Requested" && <button disabled={saving} onClick={() => updateStatus(appointment, "Confirmed")}>Confirm</button>}
                      {appointment.status === "Confirmed" && <button disabled={saving || isPast(appointment.startsAt)} onClick={() => updateStatus(appointment, "Completed")}>Mark completed</button>}
                      <button className="appointment-cancel" disabled={saving} onClick={() => updateStatus(appointment, "Cancelled")}>Cancel</button>
                    </div>
                  </>
                )}
                {role === "patient" && ["Requested", "Confirmed"].includes(appointment.status) && !isPast(appointment.startsAt) && (
                  <button className="appointment-cancel" disabled={saving} onClick={() => updateStatus(appointment, "Cancelled")}>Cancel appointment</button>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default Appointments;
