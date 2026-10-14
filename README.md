# Diet and Nutrition Planning SaaS for Clinic

## Project Overview
A web application for clinics to manage patient records, create personalized diet plans, and track nutrition progress.

## Features
- Role-based user login
- Patient management
- Diet plan management
- BMI assessment
- Nutrition tracking and progress reports
- Admin clinic overview
- Email OTP verification for account signup

## Technology Stack
- React and Vite
- Java and Spring Boot
- Maven

## Account Verification Setup
New accounts are created only after an email OTP is successfully delivered and verified through Brevo. The login page uses email and password; its **Forgot password?** flow sends a Brevo email OTP before changing the password. OTPs expire after five minutes, allow five attempts, and signup codes can be resent every 30 seconds. Codes are never returned to the browser or written to logs.

### Local development
From the project root, run `.\setup-local-secrets.ps1` once. It prompts for your verified Brevo sender email and API key; the API key input is hidden. It stores these settings in `%USERPROFILE%\.nutricare\application-secrets.properties`, outside the project, with access restricted to your Windows account. After that, you do not need to enter them again: Spring Boot loads this file automatically whenever the backend starts, including separate VS Code runs. You can then start frontend and backend separately or use `.\start-local.ps1`.

To replace your local Brevo settings, delete `%USERPROFILE%\.nutricare\application-secrets.properties` and run the setup script again. Do not share or commit this file.

The frontend defaults to `http://localhost:8080` for local backend requests. The login/signup backend endpoints are under `/api/auth`.

### Railway deployment
Deploy the app as two Railway services plus a Railway MySQL service. Deploy the backend first so its public URL is available for the frontend configuration.

1. Create a Railway MySQL service.
2. Create a backend service from this repository with the root directory `/Diet_Nutrition_Backend`. Railway uses the included Dockerfile; the app listens on Railway's `PORT`.
3. In the backend service, add the MySQL connection variables below using Railway's variable references to the MySQL service. Add the Brevo settings, a generated signing secret, and the frontend origin when it is available.
4. Create a frontend service from this repository with the root directory `/Diet_Nutrition_Frontend`, build command `npm run build`, and start command `npm start`. Set `VITE_API_BASE_URL` to the backend's public Railway URL before building.
5. After Railway assigns a public frontend domain, set `APP_CORS_ALLOWED_ORIGIN` on the backend to that exact origin (scheme and host only, no trailing path), then redeploy the backend.

Set these backend service variables in Railway:

| Variable | Value |
| --- | --- |
| `BREVO_API_KEY` | Brevo API key (keep it secret) |
| `BREVO_SENDER_EMAIL` | Sender address verified in Brevo |
| `BREVO_SENDER_NAME` | Optional sender name (defaults to `NutriCare`) |
| `APP_CORS_ALLOWED_ORIGIN` | Public frontend origin, for example `https://your-frontend.up.railway.app` |
| `APP_AUTH_TOKEN_SECRET` | Random secret with at least 32 bytes; signs 12-hour login tokens. Generate one locally with `node -p "require('node:crypto').randomBytes(48).toString('base64url')"` and store it only in Railway Variables. |
| `MYSQLHOST` | Host from the attached Railway MySQL service |
| `MYSQLPORT` | Port from the attached Railway MySQL service |
| `MYSQLDATABASE` | Database name from the attached Railway MySQL service |
| `MYSQLUSER` | Username from the attached Railway MySQL service |
| `MYSQLPASSWORD` | Password from the attached Railway MySQL service (keep it secret) |

Attach a Railway MySQL service and provide its connection variables to the backend (Railway service-variable references can keep these values synchronized). The backend creates/updates its tables using JPA on startup. Do not use a Railway volume or `APP_DATA_FILE`; application and account records are stored in MySQL. Railway supplies `PORT` automatically.

Set `VITE_API_BASE_URL` on the frontend service to the backend's public Railway URL (scheme and host only, without `/api`). This is a Vite build-time variable, so redeploy the frontend after changing it. The backend refuses to start if `APP_AUTH_TOKEN_SECRET` is missing or shorter than 32 bytes; keep the same secret across deployments so existing login tokens remain valid until expiry.

Admins sign in separately at `/admin/login`. Public email-verified signup is available only to patients and nutritionists; administrator accounts must be provisioned by a trusted clinic operator. Local setup saves a strong token-signing secret in the Windows-only secrets file.

Protected API requests require a bearer token returned at login. Tokens expire after 12 hours. Nutritionists access their assigned clinic patient records, and patient record/profile endpoints use the signed-in patient’s email rather than a client-supplied identity.

For the care workflow, a patient first signs up and verifies their email. An administrator assigns that registered patient to a registered nutritionist under **Patient Management**. The patient then appears in the assigned nutritionist’s assessment and diet-plan workflows, and in their tracking and progress review pages. Patients enter their own BMI, nutrition tracking, and progress records; the assigned nutritionist can review those entries, but cannot submit tracking or progress records on the patient’s behalf. Other patients and nutritionists cannot read those entries.

### Local MySQL
Install and start MySQL, then create a database named `diet_nutrition`. Set `MYSQLUSER` and `MYSQLPASSWORD` in your local environment (or use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` to override the connection). The local Brevo secret setup is independent of database credentials. Tests use an isolated H2 database and do not require MySQL.
