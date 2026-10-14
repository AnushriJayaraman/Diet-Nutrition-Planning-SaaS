
import { useEffect, useState } from "react";
import axios from "../../api";
import apiErrorMessage from "../../apiError";
import "./Profile.css";

const emptyProfile = {
  name: "",
  email: "",
  phone: "",
  dateOfBirth: "",
  gender: "",
  heightCm: "",
  currentWeightKg: "",
  disease: "",
  existingMedicalConditions: "",
  foodAllergies: "",
  dietaryRestrictions: "",
  foodPreference: "",
};

function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function calculateAge(dateOfBirth) {
  if (!dateOfBirth) return "";

  const dob = new Date(`${dateOfBirth}T00:00:00`);

  if (Number.isNaN(dob.getTime())) return "";

  const today = new Date();

  if (dob > today) return "";

  let age = today.getFullYear() - dob.getFullYear();

  if (
    today.getMonth() < dob.getMonth() ||
    (today.getMonth() === dob.getMonth() &&
      today.getDate() < dob.getDate())
  ) {
    age--;
  }

  return age >= 0 ? age : "";
}

function getStorageKey(email) {
  return `patientProfile_${email.toLowerCase()}`;
}

function getResponseProfile(data) {
  if (!data || typeof data !== "object") return {};

  const result = data.profile ?? data.data ?? data;

  if (!result || typeof result !== "object" || Array.isArray(result)) {
    return {};
  }

  return result;
}

function mapServerProfile(serverProfile, fallbackProfile = {}) {
  return {
    ...serverProfile,
    disease: serverProfile.disease || fallbackProfile.disease || "",
    existingMedicalConditions:
      serverProfile.medicalConditions || fallbackProfile.existingMedicalConditions || "",
    foodAllergies: serverProfile.allergies || fallbackProfile.foodAllergies || "",
  };
}

function Profile() {
  const [profile, setProfile] = useState(emptyProfile);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  useEffect(() => {
    let active = true;

    async function loadProfile() {
      try {
        const currentUser = JSON.parse(
          localStorage.getItem("currentUser") || "null"
        );

        if (!currentUser?.email) {
          if (active) {
            setMessage("Please sign in again to view your profile.");
            setMessageType("error");
          }
          return;
        }

        const email = String(currentUser.email).trim().toLowerCase();
        const storageKey = getStorageKey(email);

        let localData = {};

        try {
          const parsed = JSON.parse(
            localStorage.getItem(storageKey) || "{}"
          );

          if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
            localData = parsed;
          }
        } catch {
          localData = {};
        }

        let serverData = {};
        let serverLoaded = false;

        try {
          const response = await axios.get("/api/patient-profile", {
            params: { email },
          });

          serverData = mapServerProfile(getResponseProfile(response.data), localData);
          serverLoaded = true;
        } catch (error) {
          if (error.response?.status !== 404 && active) {
            setMessage(
              localStorage.getItem(storageKey)
                ? "Unable to load the latest server data. Showing your browser-saved profile."
                : apiErrorMessage(error, "Unable to load your profile.")
            );
            setMessageType("error");
          }
        }

        if (!active) return;

        const loadedProfile = {
          ...emptyProfile,
          ...localData,
          ...serverData,
          name:
            serverData.name ||
            localData.name ||
            currentUser.name ||
            "",
          email,
          dateOfBirth:
            serverData.dateOfBirth ||
            localData.dateOfBirth ||
            "",
        };

        loadedProfile.dateOfBirth = loadedProfile.dateOfBirth
          ? String(loadedProfile.dateOfBirth).slice(0, 10)
          : "";

        setProfile(loadedProfile);

        if (serverLoaded) {
          try {
            localStorage.setItem(
              storageKey,
              JSON.stringify(loadedProfile)
            );
          } catch {
            setMessage("Profile loaded, but the browser backup could not be updated.");
            setMessageType("error");
          }
        }
      } catch (error) {
        if (active) {
          setMessage(
            apiErrorMessage(error, "Unable to load your profile.")
          );
          setMessageType("error");
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    loadProfile();

    return () => {
      active = false;
    };
  }, []);

  const update = (field) => (event) => {
    setProfile((current) => ({
      ...current,
      [field]: event.target.value,
    }));

    setMessage("");
    setMessageType("");
  };

  async function handleSubmit(event) {
    event.preventDefault();

    if (saving) return;

    setMessage("");
    setMessageType("");

    const currentUser = (() => {
      try {
        return JSON.parse(localStorage.getItem("currentUser") || "null");
      } catch {
        return null;
      }
    })();

    if (!currentUser?.email) {
      setMessage("Please sign in again before saving your profile.");
      setMessageType("error");
      return;
    }

    if (!profile.name.trim()) {
      setMessage("Please enter your full name.");
      setMessageType("error");
      return;
    }

    if (!profile.phone.trim()) {
      setMessage("Please enter your contact number.");
      setMessageType("error");
      return;
    }

    if (!profile.dateOfBirth || calculateAge(profile.dateOfBirth) === "") {
      setMessage("Please enter a valid date of birth.");
      setMessageType("error");
      return;
    }

    if (profile.heightCm !== "") {
      const height = Number(profile.heightCm);

      if (!Number.isFinite(height) || height <= 0 || height > 300) {
        setMessage("Height must be greater than 0 and not more than 300 cm.");
        setMessageType("error");
        return;
      }
    }

    if (profile.currentWeightKg !== "") {
      const weight = Number(profile.currentWeightKg);

      if (!Number.isFinite(weight) || weight <= 0 || weight > 700) {
        setMessage("Weight must be greater than 0 and not more than 700 kg.");
        setMessageType("error");
        return;
      }
    }

    setSaving(true);

    const email = String(currentUser.email).trim().toLowerCase();
    const storageKey = getStorageKey(email);

    const payload = {
      ...profile,
      name: profile.name.trim(),
      email,
      phone: profile.phone.trim(),
      heightCm:
        profile.heightCm === "" ? null : Number(profile.heightCm),
      currentWeightKg:
        profile.currentWeightKg === ""
          ? null
          : Number(profile.currentWeightKg),
      medicalConditions: profile.existingMedicalConditions,
      allergies: profile.foodAllergies,
    };

    let localSaved = false;

    try {
      localStorage.setItem(storageKey, JSON.stringify(payload));
      localSaved = true;
    } catch {
      setMessage("Unable to save the browser backup. Please check your browser storage.");
      setMessageType("error");
      setSaving(false);
      return;
    }

    try {
      const response = await axios.post(
        "/api/patient-profile",
        payload
      );

      const serverData = mapServerProfile(getResponseProfile(response.data));

      const savedProfile = {
        ...payload,
        ...serverData,
        email,
      };

      try {
        localStorage.setItem(
          storageKey,
          JSON.stringify(savedProfile)
        );
      } catch {
        setProfile(savedProfile);
        setMessage(
          "Server save completed, but the browser backup could not be updated."
        );
        setMessageType("error");
        return;
      }

      setProfile(savedProfile);
      setMessage("Profile saved successfully.");
      setMessageType("success");
    } catch (error) {
      setMessage(
        localSaved
          ? "Browser backup saved, but server saving failed. " +
              apiErrorMessage(error, "Please check your backend API.")
          : apiErrorMessage(error, "Unable to save your profile.")
      );
      setMessageType("error");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="patient-profile-page">
        <div className="patient-profile-card">
          <p>Loading your saved profile...</p>
        </div>
      </div>
    );
  }

  const age = calculateAge(profile.dateOfBirth);

  return (
    <div className="patient-profile-page">
      <div className="patient-profile-header">
        <span>PATIENT MODULE</span>
        <h1>My Health Profile</h1>
        <p>
          Keep your personal, health, and food preference details
          up to date for your care team.
        </p>
      </div>

      <div className="patient-profile-card">
        <h2>Personal and health information</h2>

        <form onSubmit={handleSubmit}>
          <label htmlFor="profile-name">Full name</label>
          <input
            id="profile-name"
            type="text"
            value={profile.name}
            onChange={update("name")}
            maxLength={100}
            autoComplete="name"
            required
          />

          <label htmlFor="profile-birth">Date of birth</label>
          <input
            id="profile-birth"
            type="date"
            value={profile.dateOfBirth}
            onChange={update("dateOfBirth")}
            max={getTodayDate()}
            required
          />

          <label htmlFor="profile-age">Age</label>
          <input
            id="profile-age"
            type="text"
            value={age === "" ? "" : `${age} years`}
            readOnly
          />

          <label htmlFor="profile-gender">Gender</label>
          <select
            id="profile-gender"
            value={profile.gender}
            onChange={update("gender")}
            required
          >
            <option value="">Select gender</option>
            <option value="Female">Female</option>
            <option value="Male">Male</option>
            <option value="Other">Other</option>
            <option value="Prefer not to say">
              Prefer not to say
            </option>
          </select>

          <label htmlFor="profile-email">Email address</label>
          <input
            id="profile-email"
            type="email"
            value={profile.email}
            readOnly
          />

          <label htmlFor="profile-phone">Contact number</label>
          <input
            id="profile-phone"
            type="tel"
            value={profile.phone}
            onChange={update("phone")}
            maxLength={20}
            autoComplete="tel"
            required
          />

          <label htmlFor="profile-height">Height (cm)</label>
          <input
            id="profile-height"
            type="number"
            min="0.1"
            max="300"
            step="0.1"
            value={profile.heightCm ?? ""}
            onChange={update("heightCm")}
          />

          <label htmlFor="profile-weight">Current weight (kg)</label>
          <input
            id="profile-weight"
            type="number"
            min="0.1"
            max="700"
            step="0.1"
            value={profile.currentWeightKg ?? ""}
            onChange={update("currentWeightKg")}
          />

          <label htmlFor="profile-disease">Disease (if any)</label>
          <textarea
            id="profile-disease"
            rows={2}
            value={profile.disease ?? ""}
            onChange={update("disease")}
            maxLength={2000}
            placeholder="Enter disease or None"
          />

          <label htmlFor="profile-conditions">
            Existing medical conditions
          </label>
          <textarea
            id="profile-conditions"
            rows={3}
            value={profile.existingMedicalConditions ?? ""}
            onChange={update("existingMedicalConditions")}
            maxLength={2000}
            placeholder="Enter existing medical conditions"
          />

          <label htmlFor="profile-allergies">Food allergies</label>
          <textarea
            id="profile-allergies"
            rows={2}
            value={profile.foodAllergies ?? ""}
            onChange={update("foodAllergies")}
            maxLength={2000}
            placeholder="Enter allergies or None"
          />

          <label htmlFor="profile-restrictions">
            Dietary restrictions
          </label>
          <textarea
            id="profile-restrictions"
            rows={2}
            value={profile.dietaryRestrictions ?? ""}
            onChange={update("dietaryRestrictions")}
            maxLength={2000}
            placeholder="Enter dietary restrictions"
          />

          <label htmlFor="profile-food">Food preference</label>
          <select
            id="profile-food"
            value={profile.foodPreference}
            onChange={update("foodPreference")}
            required
          >
            <option value="">Select food preference</option>
            <option value="Vegetarian">Vegetarian</option>
            <option value="Non-Vegetarian">Non-Vegetarian</option>
            <option value="Eggetarian">Eggetarian</option>
            <option value="Vegan">Vegan</option>
          </select>

          <button type="submit" disabled={saving}>
            {saving ? "Saving..." : "Save Profile"}
          </button>
        </form>

        {message && (
          <p
            className={`patient-profile-message ${
              messageType === "success"
                ? "success"
                : "error"
            }`}
            role="status"
            aria-live="polite"
          >
            {message}
          </p>
        )}
      </div>
    </div>
  );
}

export default Profile;
