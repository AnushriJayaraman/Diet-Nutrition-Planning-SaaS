package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.cors.allowed-origin:*}")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/nutritionists")
    public ResponseEntity<Map<String, Object>> getNutritionists() {
        return ok("Nutritionists retrieved successfully", Map.of("nutritionists", moduleService.getNutritionists()));
    }

    @PostMapping("/patients")
    public ResponseEntity<Map<String, Object>> addPatient(@RequestBody PatientRequest request) {
        if (blank(request.name()) || blank(request.email()) || blank(request.phone())) {
            return badRequest("Name, email and phone are required");
        }
        return ok("Patient added successfully", moduleService.savePatient(request));
    }

    @GetMapping("/patients")
    public ResponseEntity<Map<String, Object>> getPatients() {
        return ok("Patients retrieved successfully", Map.of("patients", moduleService.getPatients()));
    }

    @GetMapping("/patient-assignments")
    public ResponseEntity<Map<String, Object>> getPatientAssignmentOptions() {
        return ResponseEntity.ok(moduleService.getPatientAssignmentOptions());
    }

    @PostMapping("/patient-assignments")
    public ResponseEntity<Map<String, Object>> assignPatient(@RequestBody PatientAssignmentRequest request) {
        if (request == null || blank(request.patientEmail()) || blank(request.nutritionistEmail())) {
            return badRequest("Patient and nutritionist email are required");
        }
        return ok("Patient assigned successfully", moduleService.assignPatient(request));
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String role
    ) {
        return ok("Registered accounts retrieved successfully", Map.of("users", moduleService.getUsers(search, role)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        return ok("Registered account retrieved successfully", moduleService.getUser(id));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
        @PathVariable Long id,
        @RequestBody AccountStatusRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || request.enabled() == null) {
            return badRequest("Account enabled status is required");
        }
        return ok(
            "Account status updated successfully",
            moduleService.setAccountEnabled(id, request.enabled(), user.email())
        );
    }

    @PostMapping("/bmi")
    public ResponseEntity<Map<String, Object>> saveBmi(
        @RequestBody BmiRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.patientName()) || blank(request.patientEmail())
            || !positiveFinite(request.heightCm()) || !positiveFinite(request.weightKg())) {
            return badRequest("Patient name, email, height and weight are required");
        }
        if (!moduleService.isRegisteredPatient(request.patientEmail())) {
            return badRequest("Patient email must belong to a verified patient account");
        }
        String nutritionistEmail = user.role().equals("nutritionist") ? user.email() : null;
        return ok("BMI assessment saved successfully", moduleService.saveBmi(request, nutritionistEmail));
    }

    @GetMapping("/bmi")
    public ResponseEntity<Map<String, Object>> getBmi(@RequestAttribute("authenticatedUser") AuthenticatedUser user) {
        return ok("BMI assessments retrieved successfully",
            Map.of("assessments", moduleService.getBmiAssessments(user.email(), user.role())));
    }

    @PostMapping("/diet-plans")
    public ResponseEntity<Map<String, Object>> saveDietPlan(
        @RequestBody DietPlanRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.patientName()) || blank(request.patientEmail())
            || blank(request.goal()) || blank(request.plan())
            || !validPlanTargets(request.calorieTarget(), request.proteinTargetGrams(),
                request.carbohydratesTargetGrams(), request.fatTargetGrams())) {
            return badRequest("Patient name, email, goal and plan are required");
        }
        if (!moduleService.isRegisteredPatient(request.patientEmail())) {
            return badRequest("Patient email must belong to a verified patient account");
        }
        String nutritionistEmail = user.role().equals("nutritionist") ? user.email() : null;
        return ok("Diet plan saved successfully", moduleService.saveDietPlan(request, nutritionistEmail));
    }

    @PutMapping("/diet-plans/{id}")
    public ResponseEntity<Map<String, Object>> updateDietPlan(
        @PathVariable Long id,
        @RequestBody DietPlanRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.patientEmail()) || blank(request.goal()) || blank(request.plan())
            || !validPlanTargets(request.calorieTarget(), request.proteinTargetGrams(),
                request.carbohydratesTargetGrams(), request.fatTargetGrams())) {
            return badRequest("Patient email, goal and diet plan are required");
        }
        return ok("Diet plan updated successfully", moduleService.updateDietPlan(id, request, user.email()));
    }

    @GetMapping("/diet-plans")
    public ResponseEntity<Map<String, Object>> getDietPlans(@RequestAttribute("authenticatedUser") AuthenticatedUser user) {
        return ok("Diet plans retrieved successfully", Map.of("dietPlans", moduleService.getDietPlans(user.email(), user.role())));
    }

    @GetMapping("/nutritionist/analytics")
    public ResponseEntity<Map<String, Object>> getNutritionistAnalytics(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ResponseEntity.ok(moduleService.getAnalytics(user.email()));
    }

    @GetMapping("/nutrition-tracking")
    public ResponseEntity<Map<String, Object>> getNutritionTracking(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Nutrition tracking retrieved successfully",
            Map.of("tracking", moduleService.getNutritionTracking(user.email(), user.role())));
    }

    @PostMapping("/nutrition-tracking")
    public ResponseEntity<Map<String, Object>> saveNutritionTracking(
        @RequestBody NutritionTrackingRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.patientName()) || blank(request.patientEmail())
            || !nonNegativeFinite(request.waterLitres()) || request.mealsCompleted() == null
            || request.mealsCompleted() < 0 || !validMealEntries(request.meals())) {
            return badRequest("Patient name, email, water intake and meals completed are required");
        }
        if (!moduleService.isRegisteredPatient(request.patientEmail())) {
            return badRequest("Patient email must belong to a verified patient account");
        }
        String nutritionistEmail = user.role().equals("nutritionist") ? user.email() : null;
        return ok("Nutrition tracking data saved successfully", moduleService.saveNutritionTracking(request, nutritionistEmail));
    }

    @GetMapping("/nutrition-patients")
    public ResponseEntity<Map<String, Object>> getNutritionPatients(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Patient information retrieved successfully",
            Map.of("patients", moduleService.getNutritionPatients(user.email(), user.role())));
    }

    @PostMapping("/nutrition-progress")
    public ResponseEntity<Map<String, Object>> saveNutritionProgress(
        @RequestBody NutritionProgressRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (blank(request.patientName()) || blank(request.patientEmail())
            || request.currentWeight() == null || request.targetWeight() == null || blank(request.progress())) {
            return badRequest("Patient name, email, weights and progress status are required");
        }
        if (!moduleService.isRegisteredPatient(request.patientEmail())) {
            return badRequest("Patient email must belong to a verified patient account");
        }
        String nutritionistEmail = user.role().equals("nutritionist") ? user.email() : null;
        return ok("Patient progress saved successfully", moduleService.saveNutritionProgress(request, nutritionistEmail));
    }

    @GetMapping("/nutrition-progress")
    public ResponseEntity<Map<String, Object>> getNutritionProgress(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Nutrition progress retrieved successfully",
            Map.of("progress", moduleService.getNutritionProgress(user.email(), user.role())));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<Map<String, Object>> summaryReport() {
        return ResponseEntity.ok(moduleService.getReport());
    }

    @GetMapping("/reports/patient")
    public ResponseEntity<Map<String, Object>> patientReport(
        @RequestParam String patientEmail,
        @RequestParam String reportType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            return badRequest("From date must be on or before to date");
        }
        return ok(
            "Patient report retrieved successfully",
            moduleService.getPatientReport(patientEmail, reportType, fromDate, toDate)
        );
    }

    @GetMapping("/admin/overview")
    public ResponseEntity<Map<String, Object>> adminOverview() {
        return ResponseEntity.ok(moduleService.getOverview());
    }

    @PostMapping("/patient-bmi")
    public ResponseEntity<Map<String, Object>> savePatientBmi(
        @RequestBody PatientBmiRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (!positiveFinite(request.heightCm()) || !positiveFinite(request.weightKg())) {
            return badRequest("Height and weight are required");
        }
        return ok("BMI information saved successfully", moduleService.savePatientBmi(request, user.email()));
    }

    @GetMapping("/patient-bmi")
    public ResponseEntity<Map<String, Object>> getPatientBmi(@RequestAttribute("authenticatedUser") AuthenticatedUser user) {
        if (user.role().equals("patient")) {
            return ok("BMI information retrieved successfully", Map.of("assessments", moduleService.getPatientBmi(user.email())));
        }
        return ok("BMI information retrieved successfully",
            Map.of("assessments", moduleService.getBmiAssessments(user.email(), user.role())));
    }

    @PostMapping("/patient-nutrition-tracking")
    public ResponseEntity<Map<String, Object>> savePatientNutritionTracking(
        @RequestBody PatientNutritionTrackingRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || !nonNegativeFinite(request.waterLitres())
            || request.mealsCompleted() == null || request.mealsCompleted() < 0
            || !validMealEntries(request.meals())) {
            return badRequest("Water intake and meals completed must be zero or greater");
        }
        return ok("Nutrition tracking saved successfully", moduleService.savePatientNutritionTracking(request, user.email()));
    }

    @GetMapping("/patient-nutrition-tracking")
    public ResponseEntity<Map<String, Object>> getPatientNutritionTracking(@RequestAttribute("authenticatedUser") AuthenticatedUser user) {
        if (user.role().equals("patient")) {
            return ok("Nutrition tracking retrieved successfully",
                Map.of("tracking", moduleService.getPatientNutritionTracking(user.email())));
        }
        return ok("Nutrition tracking retrieved successfully",
            Map.of("tracking", moduleService.getNutritionTracking(user.email(), user.role())));
    }

    @PostMapping("/patient-progress")
    public ResponseEntity<Map<String, Object>> savePatientProgress(
        @RequestBody PatientProgressRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request.currentWeight() == null || request.currentWeight() <= 0
            || request.targetWeight() == null || request.targetWeight() <= 0 || blank(request.progress())) {
            return badRequest("Valid current and target weights and progress status are required");
        }
        return ok("Patient progress saved successfully", moduleService.savePatientProgress(request, user.email()));
    }

    @GetMapping("/patient-progress")
    public ResponseEntity<Map<String, Object>> getPatientProgress(@RequestAttribute("authenticatedUser") AuthenticatedUser user) {
        if (user.role().equals("patient")) {
            return ok("Patient progress retrieved successfully", Map.of("progress", moduleService.getPatientProgress(user.email())));
        }
        return ok("Patient progress retrieved successfully",
            Map.of("progress", moduleService.getNutritionProgress(user.email(), user.role())));
    }

    @GetMapping("/patient-profile")
    public ResponseEntity<Map<String, Object>> getPatientProfile(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user,
        @RequestParam(required = false) String email
    ) {
        if (user.role().equals("admin") && blank(email)) {
            return ok("Patient profiles retrieved successfully",
                Map.of("profiles", moduleService.getPatientProfiles()));
        }
        String targetEmail = user.role().equals("patient") ? user.email() : email;
        if (blank(targetEmail)) {
            return badRequest("Patient email is required");
        }
        if (!validEmail(targetEmail)) {
            return badRequest("Enter a valid patient email address");
        }
        if (user.role().equals("nutritionist")
            && !moduleService.isPatientAssignedToNutritionist(targetEmail, user.email())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You can only view profiles of your assigned patients"));
        }
        PatientProfileRecord profile = moduleService.getPatientProfile(targetEmail);
        return profile == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(Map.of("profile", profile));
    }

    @PostMapping("/patient-profile")
    public ResponseEntity<Map<String, Object>> savePatientProfile(
        @RequestBody PatientProfileRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.name()) || request.name().trim().length() > 120
            || blank(request.email()) || !validEmail(request.email()) || blank(request.phone())
            || blank(request.dateOfBirth()) || blank(request.gender())
            || request.gender().trim().length() > 24 || !validPhoneNumber(request.phone())
            || !validDateOfBirth(request.dateOfBirth())
            || !optionalPositiveFinite(request.heightCm()) || !optionalPositiveFinite(request.currentWeightKg())
            || !optionalPositiveFinite(request.weightGoalKg())
            || !validFoodPreference(request.foodPreference())) {
            return badRequest("Enter a valid name, email, phone number, date of birth, gender, and positive height or weight values");
        }
        if (!request.email().trim().equalsIgnoreCase(user.email())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You can only update your own profile"));
        }
        return ok("Profile information saved successfully", moduleService.savePatientProfile(request, user.email()));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getAppointments(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Appointments retrieved successfully", Map.of(
            "appointments", moduleService.getAppointments(user.email(), user.role())
        ));
    }

    @GetMapping("/appointment-options")
    public ResponseEntity<Map<String, Object>> getAppointmentOptions(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (!user.role().equals("patient")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Only patients can request appointment options"));
        }
        return ok("Appointment options retrieved successfully", Map.of(
            "nutritionists", moduleService.getAppointmentNutritionists(user.email())
        ));
    }

    @GetMapping("/appointment-slots")
    public ResponseEntity<Map<String, Object>> getAppointmentSlots(
        @RequestParam String nutritionistEmail,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (!user.role().equals("patient")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Only patients can request appointment slots"));
        }
        return ok("Available appointment slots retrieved successfully",
            Map.of("slots", moduleService.getAppointmentSlots(nutritionistEmail, date, user.email())));
    }

    @PostMapping("/appointments")
    public ResponseEntity<Map<String, Object>> bookAppointment(
        @RequestBody AppointmentRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (!user.role().equals("patient")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Only patients can book appointments"));
        }
        if (request == null || blank(request.nutritionistEmail()) || request.startsAt() == null) {
            return badRequest("Nutritionist and appointment date/time are required");
        }
        return ok("Appointment request saved successfully",
            moduleService.bookAppointment(request, user.email()));
    }

    @PatchMapping("/appointments/{id}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
        @PathVariable Long id,
        @RequestBody AppointmentUpdateRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.status())) {
            return badRequest("Appointment status is required");
        }
        return ok("Appointment updated successfully", moduleService.updateAppointment(id, request, user));
    }

    @GetMapping("/nutritionist/profile")
    public ResponseEntity<Map<String, Object>> getNutritionistProfile(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Nutritionist profile retrieved successfully",
            Map.of("profile", moduleService.getNutritionistProfile(user.email())));
    }

    @PutMapping("/nutritionist/profile")
    public ResponseEntity<Map<String, Object>> updateNutritionistProfile(
        @RequestBody NutritionistProfileRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.name()) || blank(request.specialization())
            || (request.experienceYears() != null && request.experienceYears() < 0)) {
            return badRequest("Name and specialization are required; experience cannot be negative");
        }
        return ok("Nutritionist profile updated successfully",
            moduleService.saveNutritionistProfile(request, user.email()));
    }

    @GetMapping("/nutritionist/availability")
    public ResponseEntity<Map<String, Object>> getNutritionistAvailability(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Availability retrieved successfully",
            Map.of("availability", moduleService.getNutritionistAvailability(user.email())));
    }

    @PutMapping("/nutritionist/availability")
    public ResponseEntity<Map<String, Object>> saveNutritionistAvailability(
        @RequestBody AvailabilityUpdateRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || request.availability() == null) {
            return badRequest("Weekly availability is required");
        }
        return ok("Availability saved successfully",
            Map.of("availability", moduleService.saveNutritionistAvailability(user.email(), request.availability())));
    }

    @GetMapping("/nutritionist/patients/{patientEmail}/details")
    public ResponseEntity<Map<String, Object>> getAssignedPatientDetails(
        @PathVariable String patientEmail,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Assigned patient details retrieved successfully",
            moduleService.getAssignedPatientDetails(patientEmail, user.email()));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        return ResponseEntity.ok(moduleService.getAdminDashboard());
    }

    @GetMapping("/patient/dashboard")
    public ResponseEntity<Map<String, Object>> patientDashboard(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ResponseEntity.ok(moduleService.getPatientDashboard(user.email()));
    }

    @GetMapping("/admin/profile")
    public ResponseEntity<Map<String, Object>> getAdminProfile(
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        return ok("Administrator profile retrieved successfully", moduleService.getAdminProfile(user.email()));
    }

    @PatchMapping("/admin/profile")
    public ResponseEntity<Map<String, Object>> updateAdminProfile(
        @RequestBody AdminProfileRequest request,
        @RequestAttribute("authenticatedUser") AuthenticatedUser user
    ) {
        if (request == null || blank(request.name())) {
            return badRequest("Administrator name is required");
        }
        return ok("Administrator profile updated successfully",
            moduleService.updateAdminProfile(user.email(), request.name()));
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object payload) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        if (payload instanceof Map<?, ?> values) {
            values.forEach((key, value) -> {
                if (key instanceof String stringKey) {
                    response.put(stringKey, value);
                }
            });
        } else {
            response.put("data", payload);
        }
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean positiveFinite(Double value) {
        return value != null && Double.isFinite(value) && value > 0;
    }

    private boolean optionalPositiveFinite(Double value) {
        return value == null || positiveFinite(value);
    }

    private boolean validEmail(String value) {
        return value != null && value.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean validPhoneNumber(String value) {
        if (value == null || !value.trim().matches("[+()0-9 -]+")) {
            return false;
        }
        int digits = value.replaceAll("[^0-9]", "").length();
        return digits >= 7 && digits <= 15;
    }

    private boolean validDateOfBirth(String value) {
        try {
            return value != null && !LocalDate.parse(value.trim()).isAfter(LocalDate.now());
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private boolean validFoodPreference(String value) {
        return blank(value) || java.util.Set.of("vegetarian", "non-vegetarian", "other")
            .contains(value.trim().toLowerCase(java.util.Locale.ROOT));
    }

    private boolean validPlanTargets(Integer calories, Double protein, Double carbohydrates, Double fat) {
        return (calories == null || calories > 0)
            && optionalPositiveFinite(protein)
            && optionalPositiveFinite(carbohydrates)
            && optionalPositiveFinite(fat);
    }

    private boolean nonNegativeFinite(Double value) {
        return value != null && Double.isFinite(value) && value >= 0;
    }

    private boolean validMealEntries(java.util.List<MealRecordRequest> meals) {
        if (meals == null) {
            return true;
        }
        return meals.stream().allMatch(meal -> meal != null
            && java.util.Set.of("breakfast", "lunch", "dinner", "snack").contains(
                meal.mealType() == null ? "" : meal.mealType().trim().toLowerCase(java.util.Locale.ROOT))
            && !blank(meal.foodName()) && !blank(meal.portion())
            && nonNegativeFinite(meal.calories()) && nonNegativeFinite(meal.proteinGrams())
            && nonNegativeFinite(meal.carbohydratesGrams()) && nonNegativeFinite(meal.fatGrams()));
    }
}

record PatientRequest(String name, String email, String phone) {}
record PatientRecord(Long id, String name, String email, String phone) {}
record AccountRecord(Long id, String name, String email, String role, boolean enabled) {}
record AccountStatusRequest(Boolean enabled) {}
record BmiRequest(String patientName, String patientEmail, Double heightCm, Double weightKg, Double bmi) {}
record BmiRecord(Long id, String patientName, String patientEmail, Double heightCm, Double weightKg, Double bmi, java.time.LocalDateTime assessedAt) {}
record DietPlanRequest(
    String patientName,
    String patientEmail,
    String goal,
    String plan,
    String notes,
    String breakfast,
    String lunch,
    String dinner,
    String snacks,
    Integer calorieTarget,
    Double proteinTargetGrams,
    Double carbohydratesTargetGrams,
    Double fatTargetGrams,
    String breakfastPortion,
    String breakfastTime,
    String lunchPortion,
    String lunchTime,
    String dinnerPortion,
    String dinnerTime,
    String snacksPortion,
    String snacksTime
) {}
record DietPlanRecord(
    Long id,
    String patientName,
    String patientEmail,
    String goal,
    String plan,
    String notes,
    String breakfast,
    String lunch,
    String dinner,
    String snacks,
    Integer calorieTarget,
    Double proteinTargetGrams,
    Double carbohydratesTargetGrams,
    Double fatTargetGrams,
    String breakfastPortion,
    String breakfastTime,
    String lunchPortion,
    String lunchTime,
    String dinnerPortion,
    String dinnerTime,
    String snacksPortion,
    String snacksTime,
    String status,
    java.time.LocalDateTime createdAt
) {}
record NutritionTrackingRequest(
    String patientName,
    String patientEmail,
    Double waterLitres,
    Integer mealsCompleted,
    String notes,
    java.time.LocalDate trackedDate,
    java.util.List<MealRecordRequest> meals
) {}
record NutritionTrackingRecord(
    Long id,
    String patientName,
    String patientEmail,
    Double waterLitres,
    Integer mealsCompleted,
    String notes,
    java.time.LocalDate trackedDate,
    java.util.List<MealRecordView> meals,
    Double totalCalories,
    Double totalProteinGrams,
    Double totalCarbohydratesGrams,
    Double totalFatGrams
) {}
record MealRecordRequest(
    String mealType,
    String foodName,
    String portion,
    Double calories,
    Double proteinGrams,
    Double carbohydratesGrams,
    Double fatGrams
) {}
record MealRecordView(
    Long id,
    String mealType,
    String foodName,
    String portion,
    Double calories,
    Double proteinGrams,
    Double carbohydratesGrams,
    Double fatGrams
) {}
record NutritionPatientRequest(String patientName, String email, String notes) {}
record NutritionPatientRecord(Long id, String patientName, String email, String notes) {}
record PatientAssignmentRequest(String patientEmail, String nutritionistEmail) {}
record RegisteredPatientRecord(Long id, String name, String email, String nutritionistEmail, String nutritionistName) {}
record RegisteredNutritionistRecord(Long id, String name, String email) {}
record NutritionProgressRequest(String patientName, String patientEmail, Double currentWeight, Double targetWeight, String progress, String notes) {}
record NutritionProgressRecord(
    Long id,
    String patientName,
    String patientEmail,
    Double currentWeight,
    Double targetWeight,
    String progress,
    String notes,
    java.time.LocalDateTime recordedAt
) {}
record PatientBmiRequest(String patientName, Double heightCm, Double weightKg, Double bmi) {}
record PatientBmiRecord(Long id, String patientName, String patientEmail, Double heightCm, Double weightKg, Double bmi, java.time.LocalDateTime assessedAt) {}
record PatientNutritionTrackingRequest(
    String patientName,
    Double waterLitres,
    Integer mealsCompleted,
    String notes,
    java.time.LocalDate trackedDate,
    java.util.List<MealRecordRequest> meals
) {}
record PatientNutritionTrackingRecord(Long id, String patientName, String patientEmail, Double waterLitres, Integer mealsCompleted, String notes) {}
record PatientProgressRequest(String patientName, Double currentWeight, Double targetWeight, String progress, String notes) {}
record PatientProgressRecord(
    Long id,
    String patientName,
    String patientEmail,
    Double currentWeight,
    Double targetWeight,
    String progress,
    String notes,
    java.time.LocalDateTime recordedAt
) {}
record PatientProfileRequest(
    String name,
    String email,
    String phone,
    String dateOfBirth,
    String gender,
    Double heightCm,
    Double currentWeightKg,
    String disease,
    String medicalConditions,
    String allergies,
    String foodPreferences,
    String dietaryRestrictions,
    String foodPreference,
    Double weightGoalKg
) {}
record PatientProfileRecord(
    Long id,
    String name,
    String email,
    String phone,
    String dateOfBirth,
    String gender,
    Double heightCm,
    Double currentWeightKg,
    String disease,
    String medicalConditions,
    String allergies,
    String foodPreferences,
    String dietaryRestrictions,
    String foodPreference,
    Double weightGoalKg
) {}
record AppointmentRequest(String nutritionistEmail, java.time.LocalDateTime startsAt, String patientNote) {}
record AppointmentUpdateRequest(String status, String consultationNote) {}
record AppointmentRecord(
    Long id,
    String patientEmail,
    String patientName,
    String nutritionistEmail,
    String nutritionistName,
    java.time.LocalDateTime startsAt,
    String status,
    String patientNote,
    String consultationNote
) {}
record AdminProfileRequest(String name) {}
record NutritionistProfileRequest(
    String name,
    String phone,
    String specialization,
    String qualification,
    Integer experienceYears,
    String bio
) {}
record NutritionistProfileRecord(
    String name,
    String email,
    String phone,
    String specialization,
    String qualification,
    Integer experienceYears,
    String bio
) {}
record AvailabilitySlotRequest(Integer dayOfWeek, String startTime, String endTime, Boolean active) {}
record AvailabilityUpdateRequest(java.util.List<AvailabilitySlotRequest> availability) {}
record AvailabilityRecord(Integer dayOfWeek, String startTime, String endTime, boolean active) {}
record AppointmentSlotRecord(String startsAt) {}
