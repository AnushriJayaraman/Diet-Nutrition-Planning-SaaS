import { Link, useNavigate } from "react-router-dom";
import "./Home.css";

/* SERVICE ICONS */

const PatientIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle cx="32" cy="18" r="9" fill="#168447" />
    <path
      d="M15 52c1-12 8-19 17-19s16 7 17 19"
      fill="none"
      stroke="#168447"
      strokeWidth="5"
      strokeLinecap="round"
    />
    <path
      d="M32 34v13M25 40h14"
      stroke="#0f766e"
      strokeWidth="4"
      strokeLinecap="round"
    />
  </svg>
);

const BMIIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle
      cx="32"
      cy="32"
      r="22"
      fill="none"
      stroke="#159957"
      strokeWidth="5"
    />
    <path
      d="M20 40l8-9 6 5 10-13"
      fill="none"
      stroke="#0f766e"
      strokeWidth="4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="20" cy="40" r="3" fill="#168447" />
    <circle cx="44" cy="23" r="3" fill="#168447" />
  </svg>
);

const DietIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <path
      d="M18 39c0-13 9-23 22-23 5 0 8 2 8 2s0 13-9 21c-8 7-21 6-21 6z"
      fill="#b5e9ca"
      stroke="#168447"
      strokeWidth="4"
    />
    <path
      d="M20 46c7-8 14-14 25-22"
      fill="none"
      stroke="#0f766e"
      strokeWidth="4"
      strokeLinecap="round"
    />
    <path
      d="M18 31c-6-1-10-5-10-11 7-1 13 3 15 9"
      fill="#86efac"
    />
  </svg>
);

const NutritionIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <path
      d="M32 54C17 46 10 35 10 24c0-8 5-14 13-14 5 0 8 3 9 6 2-3 5-6 10-6 8 0 13 6 13 14 0 11-7 22-23 30z"
      fill="#d9f5ef"
      stroke="#0f766e"
      strokeWidth="4"
    />
    <path
      d="M21 29h7l3-8 5 17 3-9h5"
      fill="none"
      stroke="#168447"
      strokeWidth="3"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

const AllergyIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle
      cx="32"
      cy="32"
      r="22"
      fill="#f1f4d8"
      stroke="#168447"
      strokeWidth="4"
    />
    <path
      d="M32 18v20"
      stroke="#168447"
      strokeWidth="5"
      strokeLinecap="round"
    />
    <circle cx="32" cy="46" r="3" fill="#0f766e" />
  </svg>
);

const DoctorIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle cx="32" cy="18" r="9" fill="#0f766e" />
    <path
      d="M15 53c1-12 8-19 17-19s16 7 17 19"
      fill="#d9f5ef"
      stroke="#168447"
      strokeWidth="4"
    />
    <path
      d="M32 38v10M27 43h10"
      stroke="#168447"
      strokeWidth="4"
      strokeLinecap="round"
    />
  </svg>
);

/* WHY CHOOSE US ICONS */

const PersonalizedIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle
      cx="32"
      cy="32"
      r="22"
      fill="none"
      stroke="#168447"
      strokeWidth="4"
    />
    <circle cx="32" cy="25" r="7" fill="#86efac" />
    <path
      d="M19 48c2-8 7-12 13-12s11 4 13 12"
      fill="none"
      stroke="#168447"
      strokeWidth="4"
      strokeLinecap="round"
    />
  </svg>
);

const RecordsIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <rect
      x="16"
      y="10"
      width="32"
      height="44"
      rx="5"
      fill="#e2f4e7"
      stroke="#168447"
      strokeWidth="4"
    />
    <path
      d="M24 23h16M24 32h16M24 41h10"
      stroke="#0f766e"
      strokeWidth="3"
      strokeLinecap="round"
    />
  </svg>
);

const TrackingIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <path
      d="M12 43h8l6-18 8 26 7-20 5 12h6"
      fill="none"
      stroke="#168447"
      strokeWidth="4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M12 15h40"
      stroke="#0f766e"
      strokeWidth="3"
      strokeLinecap="round"
    />
  </svg>
);

const ClinicalIcon = () => (
  <svg viewBox="0 0 64 64" width="45" height="45">
    <circle
      cx="32"
      cy="32"
      r="22"
      fill="#f1f4d8"
      stroke="#168447"
      strokeWidth="4"
    />
    <path
      d="M32 19v26M19 32h26"
      stroke="#0f766e"
      strokeWidth="5"
      strokeLinecap="round"
    />
  </svg>
);

/* CONTACT ICONS */

const PhoneIcon = () => (
  <svg viewBox="0 0 64 64" width="25" height="25">
    <path
      d="M20 12l9 7-5 8c4 8 7 11 15 15l8-5 7 9c1 2 0 5-2 6-5 3-14 0-23-7-9-7-15-15-17-23-2-8 0-12 4-13 2 0 3 1 4 3z"
      fill="none"
      stroke="#168447"
      strokeWidth="4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

const EmailIcon = () => (
  <svg viewBox="0 0 64 64" width="25" height="25">
    <rect
      x="9"
      y="16"
      width="46"
      height="32"
      rx="5"
      fill="none"
      stroke="#0f766e"
      strokeWidth="4"
    />
    <path
      d="M12 20l20 16 20-16"
      fill="none"
      stroke="#0f766e"
      strokeWidth="4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

const LocationIcon = () => (
  <svg viewBox="0 0 64 64" width="25" height="25">
    <path
      d="M32 55S15 39 15 27c0-10 7-18 17-18s17 8 17 18c0 12-17 28-17 28z"
      fill="#dff7e8"
      stroke="#168447"
      strokeWidth="4"
    />
    <circle cx="32" cy="27" r="6" fill="#168447" />
  </svg>
);

const TimeIcon = () => (
  <svg viewBox="0 0 64 64" width="25" height="25">
    <circle
      cx="32"
      cy="32"
      r="22"
      fill="none"
      stroke="#0f766e"
      strokeWidth="4"
    />
    <path
      d="M32 19v14l9 6"
      fill="none"
      stroke="#168447"
      strokeWidth="4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

/* SERVICES */

const services = [
  {
    icon: <PatientIcon />,
    title: "Patient Management",
    description:
      "Manage patient details, health information, height, weight and other important records digitally.",
    className: "service-blue",
  },
  {
    icon: <BMIIcon />,
    title: "BMI Calculation",
    description:
      "Calculate and monitor BMI using patient height and weight for better health assessment.",
    className: "service-purple",
  },
  {
    icon: <DietIcon />,
    title: "Personalized Diet Plans",
    description:
      "Create personalized diet plans based on patient health conditions, goals and nutritional needs.",
    className: "service-green",
  },
  {
    icon: <NutritionIcon />,
    title: "Nutrient Tracking",
    description:
      "Track daily calories, proteins, carbohydrates, fats, vitamins and other essential nutrients.",
    className: "service-orange",
  },
  {
    icon: <AllergyIcon />,
    title: "Disease & Allergy Management",
    description:
      "Record diseases and food allergies to create safer and more suitable nutrition plans.",
    className: "service-yellow",
  },
  {
    icon: <DoctorIcon />,
    title: "Doctor & Dietitian Support",
    description:
      "Help doctors and dietitians manage patient nutrition information and improve clinical care.",
    className: "service-lavender",
  },
];

const serviceRoutes = {
  "Patient Management": {
    admin: "/admin/patients",
    nutritionist: "/nutritionist/patients",
    patient: "/patient/profile",
  },
  "BMI Calculation": {
    admin: "/admin/reports",
    nutritionist: "/nutritionist/patients",
    patient: "/patient/bmi",
  },
  "Personalized Diet Plans": {
    admin: "/admin/reports",
    nutritionist: "/nutritionist/diet-plan",
    patient: "/patient/diet-plan",
  },
  "Nutrient Tracking": {
    admin: "/admin/reports",
    nutritionist: "/nutritionist/tracking",
    patient: "/patient/tracking",
  },
  "Disease & Allergy Management": {
    admin: "/admin/patients",
    nutritionist: "/nutritionist/patients",
    patient: "/patient/profile",
  },
  "Doctor & Dietitian Support": {
    admin: "/admin/nutritionists",
    nutritionist: "/nutritionist/patients",
    patient: "/patient/profile",
  },
};

/* WHY CHOOSE US */

const whyChooseUs = [
  {
    icon: <PersonalizedIcon />,
    title: "Personalized Planning",
    description:
      "Create diet plans according to individual patient health conditions and nutritional requirements.",
  },
  {
    icon: <RecordsIcon />,
    title: "Easy Patient Records",
    description:
      "Store and manage patient information digitally instead of depending on paper records.",
  },
  {
    icon: <TrackingIcon />,
    title: "Nutrition Tracking",
    description:
      "Monitor nutrient intake and follow patient nutrition progress easily.",
  },
  {
    icon: <ClinicalIcon />,
    title: "Better Clinical Care",
    description:
      "Give doctors and dietitians useful information for better nutrition-based decisions.",
  },
];

function Home() {
  const navigate = useNavigate();

  const openService = (serviceTitle) => {
    const storedUser = localStorage.getItem("currentUser");
    if (!storedUser) {
      navigate("/login");
      return;
    }

    try {
      const user = JSON.parse(storedUser);
      const route = serviceRoutes[serviceTitle]?.[user?.role];
      navigate(route || "/dashboard");
    } catch (error) {
      localStorage.removeItem("currentUser");
      navigate("/login");
    }
  };

  return (
    <div className="home-page">

      {/* HERO */}

      <section className="hero-section" id="home">
        <div className="hero-content">

          <div className="hero-badge">
            🥗 Smart Diet & Nutrition Planning
          </div>

          <h1>
            Personalized Nutrition,
            <br />
            <span>Better Health.</span>
          </h1>

          <p>
            A smart digital platform for clinics to manage patient
            records, calculate BMI, create personalized diet plans
            and track daily nutrient intake in one place.
          </p>

          <div className="hero-buttons">
            <a href="#services" className="primary-btn">
              Explore Services →
            </a>

            <a href="#about" className="secondary-btn">
              Learn More
            </a>
          </div>

          <div className="hero-stats">
            <div>
              <strong>BMI</strong>
              <span>Health Analysis</span>
            </div>

            <div>
              <strong>Diet</strong>
              <span>Personalized Plans</span>
            </div>

            <div>
              <strong>Track</strong>
              <span>Nutrition Intake</span>
            </div>
          </div>

        </div>

        <div className="hero-image-area">
          <div className="hero-image-card">
            <img
              src="https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=85"
              alt="Healthy nutrition"
            />

            <div className="floating-care-card">
              <div className="care-icon">🥗</div>

              <div>
                <strong>Smart Nutrition</strong>
                <small>Personalized diet planning</small>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* SERVICES */}

      <section className="services-section" id="services">

        <div className="section-heading">
          <span className="section-label">OUR SERVICES</span>

          <h2>
            Everything You Need for
            <br />
            <span>Better Nutrition Management</span>
          </h2>

          <p>
            Manage patient nutrition information, diet planning and
            health tracking through one simple digital platform.
          </p>
        </div>

        <div className="services-grid">

          {services.map((service, index) => (
            <div
              className={`service-card ${service.className}`}
              key={index}
              role="button"
              tabIndex="0"
              onClick={() => openService(service.title)}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  openService(service.title);
                }
              }}
            >
              <div className="service-icon">
                {service.icon}
              </div>

              <div className="service-content">
                <h3>{service.title}</h3>

                <p>{service.description}</p>

                <div className="service-link">
                  Learn More <b>→</b>
                </div>
              </div>
            </div>
          ))}

        </div>
      </section>

      {/* ABOUT */}

      <section className="about-section" id="about">

        <div className="about-image">

          <img
            src="https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=900&q=85"
            alt="Healthy food"
          />

        </div>

        <div className="about-content">

          <span className="section-label">
            ABOUT OUR PLATFORM
          </span>

          <h2>
            Smarter Way to Manage
            <br />
            <span>Diet & Nutrition</span>
          </h2>

          <p>
            Our Diet and Nutrient Planning platform helps clinics
            manage patient nutrition information digitally. It
            brings patient records, BMI calculation, diet planning
            and nutrient tracking together in one system.
          </p>

          <p>
            Doctors and dietitians can use patient health details,
            diseases and allergies to create more suitable and
            personalized nutrition plans.
          </p>

          <div className="about-points">

            <div>
              <span>✓</span>
              <p>Digital patient health records</p>
            </div>

            <div>
              <span>✓</span>
              <p>BMI and health analysis</p>
            </div>

            <div>
              <span>✓</span>
              <p>Personalized diet planning</p>
            </div>

            <div>
              <span>✓</span>
              <p>Daily nutrient monitoring</p>
            </div>

          </div>

          <Link to="/signup" className="about-btn">
            Get Started →
          </Link>

        </div>
      </section>

      {/* WHY CHOOSE US */}

      <section className="why-section" id="why">

        <div className="section-heading">

          <span className="section-label">
            WHY CHOOSE US
          </span>

          <h2>
            Designed for
            <br />
            <span>Better Nutrition Care</span>
          </h2>

          <p>
            Simple tools that help clinics and nutrition
            professionals manage patient care more efficiently.
          </p>

        </div>

        <div className="why-grid">

          {whyChooseUs.map((item, index) => (
            <div className="why-card" key={index}>

              <div className="why-icon">
                {item.icon}
              </div>

              <h3>{item.title}</h3>

              <p>{item.description}</p>

            </div>
          ))}

        </div>
      </section>

      {/* CTA */}

      <section className="cta-section">

        <div className="cta-content">

          <span>START YOUR SMART NUTRITION JOURNEY</span>

          <h2>
            Plan Smarter.
            <br />
            Live Healthier.
          </h2>

          <p>
            Bring patient records, BMI analysis, diet plans and
            nutrient tracking together in one easy-to-use platform.
          </p>

          <Link to="/signup" className="cta-button">
            Create Your Account →
          </Link>

        </div>

      </section>

      {/* CONTACT */}

      <section className="contact-section" id="contact">

        <div className="contact-heading">

          <span className="section-label">
            CONTACT US
          </span>

          <h2>Get in Touch With Us</h2>

          <p>
            Have questions about diet planning or nutrition
            management? Contact our team for more information.
          </p>

        </div>

        <div className="contact-container">

          <div className="contact-info">

            <div className="contact-card">

              <div className="contact-icon phone-icon">
                <PhoneIcon />
              </div>

              <div>
                <h3>Phone</h3>
                <p>+91 98765 43210</p>
                <span>Mon - Sat, 9:00 AM - 6:00 PM</span>
              </div>

            </div>

            <div className="contact-card">

              <div className="contact-icon email-icon">
                <EmailIcon />
              </div>

              <div>
                <h3>Email</h3>
                <p>support@nutricare.com</p>
                <span>We reply within 24 hours</span>
              </div>

            </div>

            <div className="contact-card">

              <div className="contact-icon location-icon">
                <LocationIcon />
              </div>

              <div>
                <h3>Location</h3>
                <p>Trichy</p>
                <span>Tamil Nadu, India</span>
              </div>

            </div>

            <div className="contact-card">

              <div className="contact-icon time-icon">
                <TimeIcon />
              </div>

              <div>
                <h3>Working Hours</h3>
                <p>9:00 AM - 6:00 PM</p>
                <span>Monday - Saturday</span>
              </div>

            </div>

          </div>

          <div className="contact-message">

            <span className="contact-message-badge">
              🥗 Nutrition Support
            </span>

            <h3>
              Plan smarter.
              <br />
              <span>Live healthier.</span>
            </h3>

            <p>
              Manage patient nutrition, BMI, diet plans and
              nutrient tracking in one simple digital platform.
            </p>

            <Link to="/signup" className="contact-btn">
              Get Started →
            </Link>

          </div>

        </div>

      </section>

      {/* FOOTER */}

      <footer className="home-footer">

        <div className="footer-brand">

          <h3>NutriCare</h3>

          <p>
            A smart diet and nutrient planning platform designed
            to help clinics manage patient nutrition care digitally.
          </p>

        </div>

        <div className="footer-links">

          <h4>Quick Links</h4>

          <a href="#home">Home</a>
          <a href="#services">Services</a>
          <a href="#about">About</a>
          <a href="#contact">Contact</a>

        </div>

        <div className="footer-contact">

          <h4>Contact</h4>

          <p>+91 98765 43210</p>
          <p>support@nutricare.com</p>
          <p>Tamil Nadu, India</p>

        </div>

        <div className="footer-bottom">

          <p>
            © 2026 NutriCare. All Rights Reserved.
          </p>

          <span>
            Diet & Nutrient Planning SaaS
          </span>

        </div>

      </footer>

    </div>
  );
}

export default Home;