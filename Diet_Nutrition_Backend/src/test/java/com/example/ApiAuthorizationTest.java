package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private NutritionPatientRepository nutritionPatientRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private BmiAssessmentRepository bmiAssessmentRepository;

    @Autowired
    private DietPlanRepository dietPlanRepository;

    @Test
    void patientRecordsRequireTokenAndAreScopedToSignedInEmail() throws Exception {
        AccountEntity patient = accountRepository.findByEmail("patient@example.com")
            .orElseGet(() -> accountRepository.save(new AccountEntity("Patient One", "patient@example.com", "test-hash", "patient")));
        ensureAccount("Nutritionist One", "nutritionist@example.com", "nutritionist");
        ensureAccount("Other Patient", "other@example.com", "patient");
        ensureAccount("Clinic Admin", "clinic-admin@example.com", "admin");
        if (!nutritionPatientRepository.existsByEmailAndNutritionistEmail(patient.getEmail(), "nutritionist@example.com")) {
            NutritionPatientEntity assignment = new NutritionPatientEntity();
            assignment.setPatientName(patient.getName());
            assignment.setEmail(patient.getEmail());
            assignment.setNutritionistEmail("nutritionist@example.com");
            nutritionPatientRepository.save(assignment);
        }

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users")
                .header("Authorization", bearer("patient@example.com", "patient")))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + bearer("patient@example.com", "patient").substring(7, 20) + "tampered"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/patient-bmi")
                .header("Authorization", bearer("patient@example.com", "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "patientName", "Patient One",
                    "heightCm", 165,
                    "weightKg", 60,
                    "bmi", 22.04
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/patient-bmi")
                .header("Authorization", bearer("patient@example.com", "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assessments[0].patientEmail").value("patient@example.com"))
            .andExpect(jsonPath("$.assessments[0].assessedAt").isNotEmpty());

        mockMvc.perform(get("/api/patient-bmi")
                .header("Authorization", bearer("other@example.com", "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assessments").isEmpty());

        mockMvc.perform(get("/api/bmi")
                .header("Authorization", bearer("nutritionist@example.com", "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assessments[0].patientEmail").value("patient@example.com"));
    }

    @Test
    void patientProfileIdentityComesFromTheLoginToken() throws Exception {
        ensureAccount("Profile Owner", "profile-owner@example.com", "patient");
        mockMvc.perform(post("/api/patient-profile")
                .header("Authorization", bearer("profile-owner@example.com", "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Profile Owner",
                    "email", "profile-owner@example.com",
                    "phone", "5550100",
                    "dateOfBirth", "1990-01-01",
                    "gender", "Other"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/patient-profile")
                .param("email", "someone-else@example.com")
                .header("Authorization", bearer("profile-owner@example.com", "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.email").value("profile-owner@example.com"));

        mockMvc.perform(post("/api/patient-profile")
                .header("Authorization", bearer("profile-owner@example.com", "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Profile Owner",
                    "email", "someone-else@example.com",
                    "phone", "5550100",
                    "dateOfBirth", "1990-01-01",
                    "gender", "Other"
                ))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminAssignmentsArePersistedAndPatientProfilesAreAssignmentScoped() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString();
        String patientEmail = "assigned-" + suffix + "@example.com";
        String nutritionistEmail = "nutritionist-" + suffix + "@example.com";
        String otherNutritionistEmail = "other-nutritionist-" + suffix + "@example.com";
        ensureAccount("Assigned Patient", patientEmail, "patient");
        ensureAccount("Assigned Nutritionist", nutritionistEmail, "nutritionist");
        ensureAccount("Other Nutritionist", otherNutritionistEmail, "nutritionist");
        ensureAccount("Clinic Admin", "clinic-admin@example.com", "admin");

        mockMvc.perform(get("/api/patient-assignments")
                .header("Authorization", bearer("clinic-admin@example.com", "admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patients[?(@.email=='" + patientEmail + "')].nutritionistName").value("Unassigned"))
            .andExpect(jsonPath("$.nutritionists[?(@.email=='" + nutritionistEmail + "')].name").value("Assigned Nutritionist"));

        mockMvc.perform(post("/api/patient-assignments")
                .header("Authorization", bearer("clinic-admin@example.com", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "patientEmail", patientEmail,
                    "nutritionistEmail", nutritionistEmail
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(patientEmail));

        PatientProfileEntity profile = new PatientProfileEntity();
        profile.setName("Assigned Patient");
        profile.setEmail(patientEmail);
        profile.setPhone("5550100");
        profile.setDateOfBirth("1990-01-01");
        profile.setGender("Other");
        patientProfileRepository.save(profile);

        mockMvc.perform(get("/api/nutrition-patients")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patients[0].email").value(patientEmail));

        mockMvc.perform(get("/api/nutrition-patients")
                .header("Authorization", bearer(otherNutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patients").isEmpty());

        mockMvc.perform(get("/api/patient-profile")
                .param("email", patientEmail)
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.email").value(patientEmail));

        mockMvc.perform(get("/api/patient-profile")
                .param("email", patientEmail)
                .header("Authorization", bearer(otherNutritionistEmail, "nutritionist")))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/patient-assignments")
                .header("Authorization", bearer(otherNutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "patientEmail", patientEmail,
                    "nutritionistEmail", otherNutritionistEmail
                ))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/diet-plans")
                .header("Authorization", bearer("clinic-admin@example.com", "admin")))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/bmi")
                .header("Authorization", bearer("clinic-admin@example.com", "admin")))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/patient-assignments")
                .header("Authorization", bearer("clinic-admin@example.com", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "patientEmail", patientEmail,
                    "nutritionistEmail", otherNutritionistEmail
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adminUserManagementUsesRegisteredAccountsAndDisablesAccess() throws Exception {
        String email = "managed-" + java.util.UUID.randomUUID() + "@example.com";
        ensureAccount("Managed Patient", email, "patient");
        ensureAccount("Clinic Admin", "clinic-admin@example.com", "admin");
        String adminToken = bearer("clinic-admin@example.com", "admin");

        mockMvc.perform(get("/api/users")
                .param("search", email.substring(0, 8))
                .param("role", "patient")
                .header("Authorization", adminToken))
            .andExpect(jsonPath("$.users.length()").value(1))
            .andExpect(jsonPath("$.users[0].email").value(email))
            .andExpect(jsonPath("$.users[0].enabled").value(true))
            .andExpect(jsonPath("$.users[0].passwordHash").doesNotExist());

        String nutritionistEmail = "roster-" + java.util.UUID.randomUUID() + "@example.com";
        ensureAccount("Roster Nutritionist", nutritionistEmail, "nutritionist");
        mockMvc.perform(get("/api/nutritionists")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nutritionists[?(@.email=='" + nutritionistEmail + "')].enabled").value(true));

        Long accountId = accountRepository.findByEmail(email).orElseThrow().getId();
        mockMvc.perform(get("/api/users/{id}", accountId)
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        mockMvc.perform(get("/api/admin/overview")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").value(accountRepository.count()))
            .andExpect(jsonPath("$.patients").value(accountRepository.countByRole("patient")))
            .andExpect(jsonPath("$.nutritionists").value(accountRepository.countByRole("nutritionist")));

        mockMvc.perform(get("/api/diet-plans")
                .header("Authorization", adminToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/patient-bmi")
                .header("Authorization", adminToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/users/{id}/status", accountId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(get("/api/patient-bmi")
                .header("Authorization", bearer(email, "patient")))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users")
                .header("Authorization", bearer(email, "patient")))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                    "/api/users/{id}/status", accountRepository.findByEmail("clinic-admin@example.com").orElseThrow().getId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                    "/api/users/{id}/status", accountId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void adminCanViewSelectedPatientReportsAndDateFilterBmiRecords() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString();
        String patientEmail = "report-patient-" + suffix + "@example.com";
        String adminEmail = "report-admin-" + suffix + "@example.com";
        ensureAccount("Report Patient", patientEmail, "patient");
        ensureAccount("Report Admin", adminEmail, "admin");
        String adminToken = bearer(adminEmail, "admin");

        BmiAssessmentEntity bmi = new BmiAssessmentEntity();
        bmi.setPatientName("Report Patient");
        bmi.setPatientEmail(patientEmail);
        bmi.setHeightCm(165.0);
        bmi.setWeightKg(60.0);
        bmi.setBmi(22.04);
        bmi.setAssessedAt(java.time.LocalDateTime.of(2026, 5, 10, 9, 30));
        bmiAssessmentRepository.save(bmi);

        PatientProfileEntity profile = new PatientProfileEntity();
        profile.setName("Report Patient");
        profile.setEmail(patientEmail);
        profile.setPhone("5550100");
        profile.setDateOfBirth("1990-01-01");
        profile.setGender("Other");
        patientProfileRepository.save(profile);

        DietPlanEntity dietPlan = new DietPlanEntity();
        dietPlan.setPatientName("Report Patient");
        dietPlan.setPatientEmail(patientEmail);
        dietPlan.setGoal("Balanced Diet");
        dietPlan.setPlan("Saved patient diet plan");
        dietPlan.setNotes("Saved report notes");
        dietPlan.setCreatedAt(java.time.LocalDateTime.of(2026, 5, 12, 10, 0));
        dietPlanRepository.save(dietPlan);

        mockMvc.perform(get("/api/reports/patient")
                .param("patientEmail", patientEmail)
                .param("reportType", "BMI Report")
                .param("fromDate", "2026-05-10")
                .param("toDate", "2026-05-10")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records.length()").value(1))
            .andExpect(jsonPath("$.records[0].bmi").value(22.04))
            .andExpect(jsonPath("$.dateFilteringSupported").value(true));

        mockMvc.perform(get("/api/reports/patient")
                .param("patientEmail", patientEmail)
                .param("reportType", "Patient Health Details")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records[0].phone").value("5550100"))
            .andExpect(jsonPath("$.dateFilteringSupported").value(false));

        mockMvc.perform(get("/api/reports/patient")
                .param("patientEmail", patientEmail)
                .param("reportType", "Diet Plan Report")
                .param("fromDate", "2026-05-12")
                .param("toDate", "2026-05-12")
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records[0].plan").value("Saved patient diet plan"))
            .andExpect(jsonPath("$.dateFilteringSupported").value(true));

        mockMvc.perform(get("/api/reports/patient")
                .param("patientEmail", patientEmail)
                .param("reportType", "BMI Report")
                .header("Authorization", bearer(patientEmail, "patient")))
            .andExpect(status().isForbidden());
    }

    @Test
    void mealTotalsAndAppointmentsAreScopedToThePatientAndAssignedNutritionist() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString();
        String patientEmail = "care-patient-" + suffix + "@example.com";
        String nutritionistEmail = "care-nutritionist-" + suffix + "@example.com";
        String otherPatientEmail = "other-care-patient-" + suffix + "@example.com";
        ensureAccount("Care Patient", patientEmail, "patient");
        ensureAccount("Care Nutritionist", nutritionistEmail, "nutritionist");
        ensureAccount("Other Care Patient", otherPatientEmail, "patient");

        NutritionPatientEntity assignment = new NutritionPatientEntity();
        assignment.setPatientName("Care Patient");
        assignment.setEmail(patientEmail);
        assignment.setNutritionistEmail(nutritionistEmail);
        nutritionPatientRepository.save(assignment);

        mockMvc.perform(post("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(patientEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "trackedDate", java.time.LocalDate.now().toString(),
                    "waterLitres", 1.5,
                    "mealsCompleted", 1,
                    "notes", "Meal test",
                    "meals", java.util.List.of(Map.of(
                        "mealType", "lunch",
                        "foodName", "Test meal",
                        "portion", "1 serving",
                        "calories", 400,
                        "proteinGrams", 25,
                        "carbohydratesGrams", 50,
                        "fatGrams", 10
                    ))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCalories").value(400.0))
            .andExpect(jsonPath("$.data.totalProteinGrams").value(25.0));

        mockMvc.perform(get("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(patientEmail, "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tracking[0].meals[0].foodName").value("Test meal"));

        mockMvc.perform(get("/api/nutrition-tracking")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tracking[0].patientEmail").value(patientEmail))
            .andExpect(jsonPath("$.tracking[0].totalCalories").value(400.0));

        mockMvc.perform(get("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(otherPatientEmail, "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tracking").isEmpty());

        java.time.LocalDateTime startsAt = java.time.LocalDateTime.now().plusDays(2)
            .withMinute(0).withSecond(0).withNano(0);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/nutritionist/availability")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("availability", java.util.List.of(Map.of(
                    "dayOfWeek", startsAt.getDayOfWeek().getValue(),
                    "startTime", "00:00",
                    "endTime", "23:45",
                    "active", true
                ))))))
            .andExpect(status().isOk());

        var bookingResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", bearer(patientEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "nutritionistEmail", nutritionistEmail,
                    "startsAt", startsAt.toString(),
                    "patientNote", "Review progress"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("Requested"))
            .andReturn();
        long appointmentId = objectMapper.readTree(bookingResult.getResponse().getContentAsString())
            .path("data").path("id").asLong();

        mockMvc.perform(get("/api/appointments")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.appointments[0].patientEmail").value(patientEmail));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                    "/api/appointments/{id}", appointmentId)
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "Confirmed", "consultationNote", "See you soon"))))
            .andExpect(status().isOk());
    }

    @Test
    void nutritionistTrackingAndProgressStayWithinAssignedPatients() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString();
        String nutritionistEmail = "scope-nutritionist-" + suffix + "@example.com";
        String otherNutritionistEmail = "scope-other-nutritionist-" + suffix + "@example.com";
        String nurseEmail = "scope-nurse-" + suffix + "@example.com";
        String assignedEmail = "scope-assigned-" + suffix + "@example.com";
        String unassignedEmail = "scope-unassigned-" + suffix + "@example.com";
        ensureAccount("Primary Nutritionist", nutritionistEmail, "nutritionist");
        ensureAccount("Other Nutritionist", otherNutritionistEmail, "nutritionist");
        ensureAccount("Clinic Nurse", nurseEmail, "nurse");
        ensureAccount("Assigned Patient", assignedEmail, "patient");
        ensureAccount("Unassigned Patient", unassignedEmail, "patient");

        NutritionPatientEntity assignment = new NutritionPatientEntity();
        assignment.setPatientName("Assigned Patient");
        assignment.setEmail(assignedEmail);
        assignment.setNutritionistEmail(nutritionistEmail);
        nutritionPatientRepository.save(assignment);

        mockMvc.perform(post("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(assignedEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "trackedDate", java.time.LocalDate.now().toString(),
                    "waterLitres", 1.0,
                    "mealsCompleted", 1,
                    "notes", "Assigned patient meal",
                    "meals", java.util.List.of(Map.of(
                        "mealType", "breakfast",
                        "foodName", "Oatmeal",
                        "portion", "1 bowl",
                        "calories", 250,
                        "proteinGrams", 10,
                        "carbohydratesGrams", 30,
                        "fatGrams", 5
                    ))
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/patient-progress")
                .header("Authorization", bearer(assignedEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "currentWeight", 68.0,
                    "targetWeight", 65.0,
                    "progress", "Improving",
                    "notes", "Good week"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(unassignedEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "trackedDate", java.time.LocalDate.now().toString(),
                    "waterLitres", 2.0,
                    "mealsCompleted", 1,
                    "notes", "Other patient meal",
                    "meals", java.util.List.of(Map.of(
                        "mealType", "dinner",
                        "foodName", "Rice",
                        "portion", "1 plate",
                        "calories", 350,
                        "proteinGrams", 8,
                        "carbohydratesGrams", 45,
                        "fatGrams", 6
                    ))
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/nutrition-tracking")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tracking[?(@.patientEmail=='" + assignedEmail + "')].patientEmail").exists())
            .andExpect(jsonPath("$.tracking[?(@.patientEmail=='" + unassignedEmail + "')]").doesNotExist());

        mockMvc.perform(get("/api/nutrition-progress")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.progress[?(@.patientEmail=='" + assignedEmail + "')].patientEmail").exists())
            .andExpect(jsonPath("$.progress[?(@.patientEmail=='" + unassignedEmail + "')]").doesNotExist());

        mockMvc.perform(post("/api/nutrition-tracking")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/nutrition-progress")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/nutrition-tracking")
                .header("Authorization", bearer(nurseEmail, "nurse")))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/nutrition-progress")
                .header("Authorization", bearer(nurseEmail, "nurse")))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/patient-nutrition-tracking")
                .header("Authorization", bearer(nurseEmail, "nurse"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/patient-progress")
                .header("Authorization", bearer(nurseEmail, "nurse"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void nutritionistProfileAvailabilityAndPatientDetailsAreProtectedAndConnected() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString();
        String patientEmail = "detail-patient-" + suffix + "@example.com";
        String nutritionistEmail = "detail-nutritionist-" + suffix + "@example.com";
        ensureAccount("Detail Patient", patientEmail, "patient");
        ensureAccount("Detail Nutritionist", nutritionistEmail, "nutritionist");

        NutritionPatientEntity assignment = new NutritionPatientEntity();
        assignment.setPatientName("Detail Patient");
        assignment.setEmail(patientEmail);
        assignment.setNutritionistEmail(nutritionistEmail);
        nutritionPatientRepository.save(assignment);

        mockMvc.perform(get("/api/nutritionist/profile")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.email").value(patientEmail.replace("detail-patient-", "detail-nutritionist-")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/nutritionist/profile")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Updated Nutritionist",
                    "specialization", "Clinical Nutrition",
                    "qualification", "RD",
                    "experienceYears", 4,
                    "bio", "Clinic profile"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.specialization").value("Clinical Nutrition"));

        mockMvc.perform(get("/api/nutritionist/profile")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.qualification").value("RD"));

        java.time.LocalDate appointmentDate = java.time.LocalDate.now().plusDays(2);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/nutritionist/availability")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("availability", java.util.List.of(Map.of(
                    "dayOfWeek", appointmentDate.getDayOfWeek().getValue(),
                    "startTime", "09:00",
                    "endTime", "10:00",
                    "active", true
                ))))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availability[0].startTime").value("09:00"));

        mockMvc.perform(get("/api/nutritionist/availability")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availability[0].dayOfWeek").value(appointmentDate.getDayOfWeek().getValue()));

        mockMvc.perform(get("/api/appointment-slots")
                .param("nutritionistEmail", nutritionistEmail)
                .param("date", appointmentDate.toString())
                .header("Authorization", bearer(patientEmail, "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.slots[0]").value(appointmentDate + "T09:00"));

        mockMvc.perform(post("/api/patient-profile")
                .header("Authorization", bearer(patientEmail, "patient"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.ofEntries(
                    Map.entry("name", "Detail Patient"),
                    Map.entry("email", patientEmail),
                    Map.entry("phone", "5550100"),
                    Map.entry("dateOfBirth", "1990-01-01"),
                    Map.entry("gender", "Other"),
                    Map.entry("heightCm", 165),
                    Map.entry("currentWeightKg", 60),
                    Map.entry("disease", "None"),
                    Map.entry("medicalConditions", "Hypertension"),
                    Map.entry("allergies", "Peanuts"),
                    Map.entry("dietaryRestrictions", "Low sodium"),
                    Map.entry("foodPreference", "Vegetarian")
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/nutritionist/patients/{email}/details", patientEmail)
                .header("Authorization", bearer(nutritionistEmail, "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.patientEmail").value(patientEmail))
            .andExpect(jsonPath("$.profile.name").value("Detail Patient"))
            .andExpect(jsonPath("$.profile.disease").value("None"))
            .andExpect(jsonPath("$.profile.medicalConditions").value("Hypertension"))
            .andExpect(jsonPath("$.profile.allergies").value("Peanuts"))
            .andExpect(jsonPath("$.profile.dietaryRestrictions").value("Low sodium"))
            .andExpect(jsonPath("$.profile.foodPreference").value("Vegetarian"))
            .andExpect(jsonPath("$.bmiHistory").isArray())
            .andExpect(jsonPath("$.nutritionHistory").isArray());

        mockMvc.perform(get("/api/nutritionist/patients/{email}/details", patientEmail)
                .header("Authorization", bearer(patientEmail, "patient")))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/bmi")
                .header("Authorization", bearer(nutritionistEmail, "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "patientName", "Detail Patient",
                    "patientEmail", patientEmail,
                    "heightCm", 165,
                    "weightKg", 60
                ))))
            .andExpect(status().isForbidden());
    }

    @Test
    void completePatientReportReturnsRealSectionsWithoutInventingRecords() throws Exception {
        String patientEmail = "report-" + java.util.UUID.randomUUID() + "@example.com";
        ensureAccount("Report Patient", patientEmail, "patient");
        ensureAccount("Report Admin", "report-admin@example.com", "admin");

        mockMvc.perform(get("/api/reports/patient")
                .param("patientEmail", patientEmail)
                .param("reportType", "Complete Patient Report")
                .header("Authorization", bearer("report-admin@example.com", "admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records.length()").value(6))
            .andExpect(jsonPath("$.records[0].records").isEmpty())
            .andExpect(jsonPath("$.patientEmail").value(patientEmail));

        for (String type : java.util.List.of(
            "BMI and Progress Report",
            "BMI Report",
            "Patient Health Details",
            "Nutrition Tracking Report",
            "Diet Plan Report",
            "Appointment Report"
        )) {
            mockMvc.perform(get("/api/reports/patient")
                    .param("patientEmail", patientEmail)
                    .param("reportType", type)
                    .header("Authorization", bearer("report-admin@example.com", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value(type));
        }
    }

    private void ensureAccount(String name, String email, String role) {
        AccountEntity account = accountRepository.findByEmail(email)
            .orElseGet(() -> new AccountEntity(name, email, "test-hash", role));
        account.setEnabled(true);
        accountRepository.save(account);
    }

    private String bearer(String email, String role) {
        return "Bearer " + authTokenService.issue(email, role);
    }
}
