import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./Profile.css";

const emptyProfile = {
  name: "",
  email: "",
  phone: "",
  specialization: "",
  qualification: "",
  experienceYears: "",
  bio: "",
};

function Profile() {
  const [profile, setProfile] = useState(emptyProfile);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    let active = true;
    axios.get("/api/nutritionist/profile")
      .then(({ data }) => {
        if (active) setProfile({ ...emptyProfile, ...data.profile, experienceYears: data.profile.experienceYears ?? "" });
      })
      .catch((error) => {
        if (active) setMessage(apiErrorMessage(error, "Unable to load your profile."));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  const update = (field) => (event) => setProfile((current) => ({ ...current, [field]: event.target.value }));

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      const { data } = await axios.put("/api/nutritionist/profile", {
        ...profile,
        experienceYears: profile.experienceYears === "" ? null : Number(profile.experienceYears),
      });
      setProfile({ ...emptyProfile, ...data.data, experienceYears: data.data.experienceYears ?? "" });
      setMessage("Nutritionist profile saved successfully.");
    } catch (error) {
      setMessage(apiErrorMessage(error, "Unable to save your profile."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="nutritionist-profile-page">
      <header>
        <span>NUTRITIONIST MODULE</span>
        <h1>Professional Profile</h1>
        <p>Keep your professional details up to date for patients and clinic records.</p>
      </header>
      <section className="nutritionist-profile-card">
        {loading ? <p>Loading your saved profile...</p> : (
          <form onSubmit={save}>
            <label htmlFor="nutritionist-name">Full name</label>
            <input id="nutritionist-name" value={profile.name} onChange={update("name")} required />
            <label htmlFor="nutritionist-email">Email</label>
            <input id="nutritionist-email" value={profile.email} readOnly />
            <label htmlFor="nutritionist-phone">Phone</label>
            <input id="nutritionist-phone" type="tel" value={profile.phone} onChange={update("phone")} />
            <label htmlFor="nutritionist-specialization">Specialization</label>
            <input id="nutritionist-specialization" value={profile.specialization} onChange={update("specialization")} required />
            <label htmlFor="nutritionist-qualification">Qualification</label>
            <input id="nutritionist-qualification" value={profile.qualification} onChange={update("qualification")} />
            <label htmlFor="nutritionist-experience">Years of experience</label>
            <input id="nutritionist-experience" type="number" min="0" step="1" value={profile.experienceYears} onChange={update("experienceYears")} />
            <label htmlFor="nutritionist-bio">Professional bio</label>
            <textarea id="nutritionist-bio" rows="4" value={profile.bio} onChange={update("bio")} />
            <button type="submit" disabled={saving}>{saving ? "Saving..." : "Save Profile"}</button>
          </form>
        )}
        {message && <p role="status">{message}</p>}
      </section>
    </main>
  );
}

export default Profile;
