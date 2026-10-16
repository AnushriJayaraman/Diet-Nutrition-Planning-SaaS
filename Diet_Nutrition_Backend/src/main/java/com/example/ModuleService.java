package com.example;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleService {

    private final PatientRepository patients;
    private final BmiAssessmentRepository bmiAssessments;
    private final DietPlanRepository dietPlans;
    private final NutritionTrackingRepository nutritionTracking;
    private final NutritionPatientRepository nutritionPatients;
    private final NutritionProgressRepository nutritionProgress;
    private final PatientBmiRepository patientBmi;
    private final PatientNutritionTrackingRepository patientNutritionTracking;
    private final PatientProgressRepository patientProgress;
    private final PatientProfileRepository patientProfiles;
    private final AccountRepository accounts;
    private final AppointmentRepository appointments;
    private final MealRecordRepository mealRecords;
    private final NutritionistRepository nutritionists;
    private final NutritionistAvailabilityRepository availability;

    public ModuleService(
        PatientRepository patients,
        BmiAssessmentRepository bmiAssessments,
        DietPlanRepository dietPlans,
        NutritionTrackingRepository nutritionTracking,
        NutritionPatientRepository nutritionPatients,
        NutritionProgressRepository nutritionProgress,
        PatientBmiRepository patientBmi,
        PatientNutritionTrackingRepository patientNutritionTracking,
        PatientProgressRepository patientProgress,
        PatientProfileRepository patientProfiles,
        AccountRepository accounts,
        AppointmentRepository appointments,
        MealRecordRepository mealRecords,
        NutritionistRepository nutritionists,
        NutritionistAvailabilityRepository availability
    ) {
        this.patients = patients;
        this.bmiAssessments = bmiAssessments;
        this.dietPlans = dietPlans;
        this.nutritionTracking = nutritionTracking;
        this.nutritionPatients = nutritionPatients;
        this.nutritionProgress = nutritionProgress;
        this.patientBmi = patientBmi;
        this.patientNutritionTracking = patientNutritionTracking;
        this.patientProgress = patientProgress;
        this.patientProfiles = patientProfiles;
        this.accounts = accounts;
        this.appointments = appointments;
        this.mealRecords = mealRecords;
        this.nutritionists = nutritionists;
        this.availability = availability;
    }

    public PatientRecord savePatient(PatientRequest request) {
        PatientEntity entity = new PatientEntity();
        entity.setName(request.name().trim());
        entity.setEmail(request.email().trim().toLowerCase());
        entity.setPhone(request.phone().trim());
        return map(patients.save(entity));
    }

    public BmiRecord saveBmi(BmiRequest request, String nutritionistEmail) {
        requireAssignedPatient(request.patientEmail(), nutritionistEmail);
        BmiAssessmentEntity entity = new BmiAssessmentEntity();
        entity.setPatientName(textOrEmpty(request.patientName()));
        entity.setPatientEmail(normalizeEmail(request.patientEmail()));
        entity.setNutritionistEmail(normalizeEmail(nutritionistEmail));
        entity.setHeightCm(request.heightCm());
        entity.setWeightKg(request.weightKg());
        entity.setBmi(calculateBmi(request.heightCm(), request.weightKg()));
        return map(bmiAssessments.save(entity));
    }

    public DietPlanRecord saveDietPlan(DietPlanRequest request, String nutritionistEmail) {
        requireAssignedPatient(request.patientEmail(), nutritionistEmail);
        AccountEntity patient = requirePatientAccount(request.patientEmail());
        DietPlanEntity entity = new DietPlanEntity();
        entity.setPatientName(patient.getName());
        entity.setPatientEmail(normalizeEmail(request.patientEmail()));
        entity.setNutritionistEmail(normalizeEmail(nutritionistEmail));
        entity.setGoal(request.goal().trim());
        entity.setPlan(request.plan().trim());
        entity.setNotes(textOrEmpty(request.notes()));
        entity.setBreakfast(textOrEmpty(request.breakfast()));
        entity.setLunch(textOrEmpty(request.lunch()));
        entity.setDinner(textOrEmpty(request.dinner()));
        entity.setSnacks(textOrEmpty(request.snacks()));
        entity.setCalorieTarget(request.calorieTarget());
        setStructuredPlanFields(entity, request);
        return map(dietPlans.save(entity));
    }

    public DietPlanRecord updateDietPlan(Long id, DietPlanRequest request, String nutritionistEmail) {
        DietPlanEntity entity = dietPlans.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Diet plan was not found"));
        requireAssignedPatient(entity.getPatientEmail(), nutritionistEmail);
        if (!java.util.Objects.equals(normalizeEmail(entity.getNutritionistEmail()), normalizeEmail(nutritionistEmail))) {
            throw new IllegalArgumentException("You can only edit diet plans that you created");
        }
        if (!entity.getPatientEmail().equalsIgnoreCase(normalizeEmail(request.patientEmail()))) {
            throw new IllegalArgumentException("A saved diet plan cannot be reassigned to a different patient");
        }
        AccountEntity patient = requirePatientAccount(request.patientEmail());
        entity.setPatientName(patient.getName());
        entity.setGoal(request.goal().trim());
        entity.setPlan(request.plan().trim());
        entity.setNotes(textOrEmpty(request.notes()));
        entity.setBreakfast(textOrEmpty(request.breakfast()));
        entity.setLunch(textOrEmpty(request.lunch()));
        entity.setDinner(textOrEmpty(request.dinner()));
        entity.setSnacks(textOrEmpty(request.snacks()));
        entity.setCalorieTarget(request.calorieTarget());
        setStructuredPlanFields(entity, request);
        return map(dietPlans.save(entity));
    }

    private void setStructuredPlanFields(DietPlanEntity entity, DietPlanRequest request) {
        entity.setProteinTargetGrams(request.proteinTargetGrams());
        entity.setCarbohydratesTargetGrams(request.carbohydratesTargetGrams());
        entity.setFatTargetGrams(request.fatTargetGrams());
        entity.setBreakfastPortion(textOrEmpty(request.breakfastPortion()));
        entity.setBreakfastTime(textOrEmpty(request.breakfastTime()));
        entity.setLunchPortion(textOrEmpty(request.lunchPortion()));
        entity.setLunchTime(textOrEmpty(request.lunchTime()));
        entity.setDinnerPortion(textOrEmpty(request.dinnerPortion()));
        entity.setDinnerTime(textOrEmpty(request.dinnerTime()));
        entity.setSnacksPortion(textOrEmpty(request.snacksPortion()));
        entity.setSnacksTime(textOrEmpty(request.snacksTime()));
    }

    public NutritionTrackingRecord saveNutritionTracking(NutritionTrackingRequest request, String nutritionistEmail) {
        requireAssignedPatient(request.patientEmail(), nutritionistEmail);
        NutritionTrackingEntity entity = new NutritionTrackingEntity();
        entity.setPatientName(request.patientName().trim());
        entity.setPatientEmail(normalizeEmail(request.patientEmail()));
        entity.setNutritionistEmail(normalizeEmail(nutritionistEmail));
        entity.setWaterLitres(request.waterLitres());
        entity.setMealsCompleted(request.mealsCompleted());
        entity.setNotes(textOrEmpty(request.notes()));
        LocalDate trackingDate = request.trackedDate() == null ? LocalDate.now() : request.trackedDate();
        if (nutritionTracking.existsByPatientEmailAndTrackedDate(entity.getPatientEmail(), trackingDate)) {
            throw new IllegalArgumentException("A nutrition record already exists for this patient and date");
        }
        entity.setTrackedDate(trackingDate);
        NutritionTrackingEntity saved = nutritionTracking.save(entity);
        saveMealRecords(saved.getId(), request.meals());
        return mapTracking(List.of(saved)).get(0);
    }

    public Map<String, Object> getPatientAssignmentOptions() {
        List<AccountEntity> activeNutritionistAccounts = accounts.findAllByRoleOrderByNameAsc("nutritionist").stream()
            .filter(AccountEntity::isEnabled)
            .toList();
        Map<String, String> nutritionistNames = activeNutritionistAccounts.stream()
            .collect(java.util.stream.Collectors.toMap(
                account -> normalizeEmail(account.getEmail()),
                AccountEntity::getName
            ));
        List<RegisteredPatientRecord> registeredPatients = accounts.findAllByRoleOrderByNameAsc("patient").stream()
                .filter(AccountEntity::isEnabled)
                .map(account -> {
                NutritionPatientEntity assignment = nutritionPatients.findFirstByEmail(normalizeEmail(account.getEmail())).orElse(null);
                String assignedNutritionistEmail = assignment == null ? null : assignment.getNutritionistEmail();
                return new RegisteredPatientRecord(
                    account.getId(),
                    account.getName(),
                    account.getEmail(),
                    assignedNutritionistEmail,
                    assignedNutritionistEmail == null
                        ? "Unassigned"
                        : nutritionistNames.getOrDefault(normalizeEmail(assignedNutritionistEmail), assignedNutritionistEmail)
                );
            })
            .toList();
        List<RegisteredNutritionistRecord> registeredNutritionists = activeNutritionistAccounts.stream()
            .map(account -> new RegisteredNutritionistRecord(account.getId(), account.getName(), account.getEmail()))
            .toList();
        return Map.of("patients", registeredPatients, "nutritionists", registeredNutritionists);
    }

    public NutritionPatientRecord assignPatient(PatientAssignmentRequest request) {
        AccountEntity patientAccount = requirePatientAccount(request.patientEmail());
        if (!patientAccount.isEnabled()) {
            throw new IllegalArgumentException("This patient account is disabled");
        }
        String patientEmail = normalizeEmail(patientAccount.getEmail());
        String nutritionistEmail = normalizeEmail(request.nutritionistEmail());
        AccountEntity nutritionistAccount = accounts.findByEmail(nutritionistEmail)
            .filter(account -> account.getRole().equals("nutritionist") && account.isEnabled())
            .orElseThrow(() -> new IllegalArgumentException("Nutritionist email must belong to a registered nutritionist account"));
        NutritionPatientEntity existing = nutritionPatients.findFirstByEmail(patientEmail).orElse(null);
        if (existing != null && existing.getNutritionistEmail() != null) {
            throw new IllegalArgumentException("This patient is already assigned to a nutritionist");
        }
        NutritionPatientEntity assignment = existing == null ? new NutritionPatientEntity() : existing;
        assignment.setPatientName(patientAccount.getName());
        assignment.setEmail(patientEmail);
        assignment.setNutritionistEmail(normalizeEmail(nutritionistAccount.getEmail()));
        return map(nutritionPatients.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<RegisteredNutritionistRecord> getAppointmentNutritionists(String patientEmail) {
        String email = normalizeEmail(patientEmail);
        return nutritionPatients.findFirstByEmail(email)
            .filter(assignment -> assignment.getNutritionistEmail() != null)
            .flatMap(assignment -> accounts.findByEmail(normalizeEmail(assignment.getNutritionistEmail())))
            .filter(account -> account.getRole().equals("nutritionist") && account.isEnabled())
            .map(account -> List.of(new RegisteredNutritionistRecord(account.getId(), account.getName(), account.getEmail())))
            .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<AppointmentRecord> getAppointments(String email, String role) {
        List<AppointmentEntity> records = switch (role) {
            case "admin" -> appointments.findAll();
            case "nutritionist" -> appointments.findAllByNutritionistEmailOrderByStartsAtAsc(normalizeEmail(email));
            case "patient" -> appointments.findAllByPatientEmailOrderByStartsAtAsc(normalizeEmail(email));
            default -> List.of();
        };
        return records.stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public NutritionistProfileRecord getNutritionistProfile(String email) {
        AccountEntity account = accounts.findByEmail(normalizeEmail(email))
            .filter(existing -> existing.getRole().equals("nutritionist"))
            .orElseThrow(() -> new IllegalArgumentException("Nutritionist account was not found"));
        NutritionistEntity profile = nutritionists.findByEmail(normalizeEmail(email)).orElse(null);
        return profile == null
            ? new NutritionistProfileRecord(account.getName(), account.getEmail(), "", "", "", null, "")
            : map(profile);
    }

    public NutritionistProfileRecord saveNutritionistProfile(NutritionistProfileRequest request, String email) {
        String normalizedEmail = normalizeEmail(email);
        AccountEntity account = accounts.findByEmail(normalizedEmail)
            .filter(existing -> existing.getRole().equals("nutritionist"))
            .orElseThrow(() -> new IllegalArgumentException("Nutritionist account was not found"));
        NutritionistEntity profile = nutritionists.findByEmail(normalizedEmail).orElseGet(NutritionistEntity::new);
        profile.setEmail(normalizedEmail);
        profile.setName(request.name().trim());
        profile.setSpecialization(request.specialization().trim());
        profile.setPhone(textOrEmpty(request.phone()));
        profile.setQualification(textOrEmpty(request.qualification()));
        profile.setExperienceYears(request.experienceYears());
        profile.setBio(textOrEmpty(request.bio()));
        account.setName(request.name().trim());
        accounts.save(account);
        return map(nutritionists.save(profile));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRecord> getNutritionistAvailability(String email) {
        return availability.findAllByNutritionistEmailOrderByDayOfWeekAsc(normalizeEmail(email)).stream()
            .map(ModuleService::map)
            .toList();
    }

    public List<AvailabilityRecord> saveNutritionistAvailability(
        String email,
        List<AvailabilitySlotRequest> requestedAvailability
    ) {
        String normalizedEmail = normalizeEmail(email);
        java.util.Set<Integer> weekdays = new java.util.HashSet<>();
        List<NutritionistAvailabilityEntity> updated = requestedAvailability.stream().map(slot -> {
            if (slot == null || slot.dayOfWeek() == null || slot.dayOfWeek() < 1 || slot.dayOfWeek() > 7
                || slot.startTime() == null || slot.endTime() == null || slot.active() == null
                || !weekdays.add(slot.dayOfWeek())) {
                throw new IllegalArgumentException("Each weekday must be unique and have a valid schedule");
            }
            LocalTime start;
            LocalTime end;
            try {
                start = LocalTime.parse(slot.startTime());
                end = LocalTime.parse(slot.endTime());
            } catch (java.time.format.DateTimeParseException exception) {
                throw new IllegalArgumentException("Availability times must use HH:mm format");
            }
            if (!start.isBefore(end) || start.getMinute() % 15 != 0 || end.getMinute() % 15 != 0) {
                throw new IllegalArgumentException("Start and end times must be ordered 15-minute boundaries");
            }
            NutritionistAvailabilityEntity entity = new NutritionistAvailabilityEntity();
            entity.setNutritionistEmail(normalizedEmail);
            entity.setDayOfWeek(slot.dayOfWeek());
            entity.setStartTime(start.toString());
            entity.setEndTime(end.toString());
            entity.setActive(slot.active());
            return entity;
        }).toList();
        availability.deleteAllByNutritionistEmail(normalizedEmail);
        availability.flush();
        return availability.saveAll(updated).stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAppointmentSlots(String nutritionistEmail, LocalDate date, String patientEmail) {
        String patient = normalizeEmail(patientEmail);
        String nutritionist = normalizeEmail(nutritionistEmail);
        requireAssignedPatient(patient, nutritionist);
        if (date == null || date.isBefore(LocalDate.now())) {
            return List.of();
        }
        NutritionistAvailabilityEntity schedule = availability
            .findByNutritionistEmailAndDayOfWeek(nutritionist, date.getDayOfWeek().getValue())
            .filter(NutritionistAvailabilityEntity::isActive)
            .orElse(null);
        if (schedule == null) {
            return List.of();
        }
        LocalTime start = LocalTime.parse(schedule.getStartTime());
        LocalTime end = LocalTime.parse(schedule.getEndTime());
        List<String> slots = new java.util.ArrayList<>();
        for (LocalTime slot = start; !slot.plusMinutes(15).isAfter(end); slot = slot.plusMinutes(15)) {
            LocalDateTime startsAt = LocalDateTime.of(date, slot);
            if (startsAt.isAfter(LocalDateTime.now())
                && appointments.findAllByNutritionistEmailAndStartsAt(nutritionist, startsAt).stream()
                    .noneMatch(appointment -> !appointment.getStatus().equals("Cancelled"))
                && appointments.findAllByPatientEmailAndStartsAt(patient, startsAt).stream()
                    .noneMatch(appointment -> !appointment.getStatus().equals("Cancelled"))) {
                slots.add(startsAt.toString());
            }
        }
        return slots;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAssignedPatientDetails(String patientEmail, String nutritionistEmail) {
        String patient = normalizeEmail(patientEmail);
        requireAssignedPatient(patient, nutritionistEmail);
        AccountEntity account = requirePatientAccount(patient);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("patientName", account.getName());
        details.put("patientEmail", account.getEmail());
        details.put("profile", getPatientProfile(patient));
        details.put("bmiHistory", getPatientBmi(patient));
        details.put("nutritionHistory", getPatientNutritionTracking(patient));
        details.put("progressHistory", getPatientProgress(patient));
        details.put("dietPlans", getDietPlans(patient, "patient"));
        details.put("appointments", getAppointments(patient, "patient"));
        return details;
    }

    public AppointmentRecord bookAppointment(AppointmentRequest request, String patientEmail) {
        String email = normalizeEmail(patientEmail);
        AccountEntity patient = requirePatientAccount(email);
        if (!patient.isEnabled()) {
            throw new IllegalArgumentException("This patient account is disabled");
        }
        if (request.startsAt() == null || !request.startsAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Choose an appointment time in the future");
        }
        if (request.startsAt().getMinute() % 15 != 0 || request.startsAt().getSecond() != 0) {
            throw new IllegalArgumentException("Appointments must start on a 15-minute time slot");
        }
        String nutritionistEmail = normalizeEmail(request.nutritionistEmail());
        if (!isPatientAssignedToNutritionist(email, nutritionistEmail)) {
            throw new IllegalArgumentException("You can only book with your assigned nutritionist");
        }
        NutritionistAvailabilityEntity schedule = availability
            .findForUpdateByNutritionistAndDay(nutritionistEmail, request.startsAt().getDayOfWeek().getValue())
            .filter(NutritionistAvailabilityEntity::isActive)
            .orElseThrow(() -> new IllegalArgumentException("The nutritionist has no availability on that day"));
        LocalTime appointmentTime = request.startsAt().toLocalTime();
        if (appointmentTime.isBefore(LocalTime.parse(schedule.getStartTime()))
            || appointmentTime.plusMinutes(15).isAfter(LocalTime.parse(schedule.getEndTime()))) {
            throw new IllegalArgumentException("Choose an available 15-minute appointment slot");
        }
        AccountEntity nutritionist = accounts.findByEmail(nutritionistEmail)
            .filter(account -> account.getRole().equals("nutritionist") && account.isEnabled())
            .orElseThrow(() -> new IllegalArgumentException("Your assigned nutritionist is not active"));
        boolean nutritionistBusy = appointments.findAllByNutritionistEmailAndStartsAt(nutritionistEmail, request.startsAt())
            .stream().anyMatch(appointment -> !appointment.getStatus().equals("Cancelled"));
        boolean patientBusy = appointments.findAllByPatientEmailAndStartsAt(email, request.startsAt())
            .stream().anyMatch(appointment -> !appointment.getStatus().equals("Cancelled"));
        if (nutritionistBusy || patientBusy) {
            throw new IllegalArgumentException("This appointment time is already booked");
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setPatientEmail(email);
        appointment.setPatientName(patient.getName());
        appointment.setNutritionistEmail(nutritionistEmail);
        appointment.setNutritionistName(nutritionist.getName());
        appointment.setStartsAt(request.startsAt());
        appointment.setPatientNote(textOrEmpty(request.patientNote()));
        return map(appointments.save(appointment));
    }

    public AppointmentRecord updateAppointment(Long id, AppointmentUpdateRequest request, AuthenticatedUser user) {
        AppointmentEntity appointment = appointments.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Appointment was not found"));
        if (user.role().equals("patient")) {
            if (!appointment.getPatientEmail().equalsIgnoreCase(user.email())
                || !request.status().equals("Cancelled")
                || !appointment.getStartsAt().isAfter(LocalDateTime.now())
                || !List.of("Requested", "Confirmed").contains(appointment.getStatus())) {
                throw new IllegalArgumentException("This appointment cannot be cancelled by your account");
            }
            appointment.setStatus("Cancelled");
        } else if (user.role().equals("nutritionist")) {
            if (!appointment.getNutritionistEmail().equalsIgnoreCase(user.email())) {
                throw new IllegalArgumentException("You can only manage appointments for your assigned patients");
            }
            if (!List.of("Confirmed", "Completed", "Cancelled").contains(request.status())) {
                throw new IllegalArgumentException("Choose Confirmed, Completed or Cancelled");
            }
            if (request.status().equals("Completed") && !appointment.getStatus().equals("Confirmed")) {
                throw new IllegalArgumentException("Only confirmed appointments can be completed");
            }
            appointment.setStatus(request.status());
            appointment.setConsultationNote(textOrEmpty(request.consultationNote()));
        } else {
            throw new IllegalArgumentException("Your account cannot update appointments");
        }
        return map(appointments.save(appointment));
    }

    @Transactional(readOnly = true)
    public boolean isPatientAssignedToNutritionist(String patientEmail, String nutritionistEmail) {
        return nutritionPatients.existsByEmailAndNutritionistEmail(
            normalizeEmail(patientEmail),
            normalizeEmail(nutritionistEmail)
        );
    }

    public NutritionProgressRecord saveNutritionProgress(NutritionProgressRequest request, String nutritionistEmail) {
        requireAssignedPatient(request.patientEmail(), nutritionistEmail);
        NutritionProgressEntity entity = new NutritionProgressEntity();
        entity.setPatientName(request.patientName().trim());
        entity.setPatientEmail(normalizeEmail(request.patientEmail()));
        entity.setNutritionistEmail(normalizeEmail(nutritionistEmail));
        entity.setCurrentWeight(request.currentWeight());
        entity.setTargetWeight(request.targetWeight());
        entity.setProgress(request.progress().trim());
        entity.setNotes(textOrEmpty(request.notes()));
        return map(nutritionProgress.save(entity));
    }

    public PatientBmiRecord savePatientBmi(PatientBmiRequest request, String ownerEmail) {
        String email = normalizeEmail(ownerEmail);
        AccountEntity account = requirePatientAccount(email);
        NutritionPatientEntity assignment = nutritionPatients.findFirstByEmail(email).orElse(null);
        BmiAssessmentEntity entity = new BmiAssessmentEntity();
        entity.setPatientName(account.getName());
        entity.setPatientEmail(email);
        entity.setNutritionistEmail(assignment == null ? null : assignment.getNutritionistEmail());
        entity.setHeightCm(request.heightCm());
        entity.setWeightKg(request.weightKg());
        entity.setBmi(calculateBmi(request.heightCm(), request.weightKg()));
        BmiRecord record = map(bmiAssessments.save(entity));
        return new PatientBmiRecord(record.id(), record.patientName(), record.patientEmail(),
            record.heightCm(), record.weightKg(), record.bmi(), record.assessedAt());
    }

    public NutritionTrackingRecord savePatientNutritionTracking(PatientNutritionTrackingRequest request, String ownerEmail) {
        String email = normalizeEmail(ownerEmail);
        AccountEntity account = requirePatientAccount(email);
        NutritionPatientEntity assignment = nutritionPatients.findFirstByEmail(email).orElse(null);
        NutritionTrackingEntity entity = new NutritionTrackingEntity();
        entity.setPatientName(account.getName());
        entity.setPatientEmail(email);
        entity.setNutritionistEmail(assignment == null ? null : assignment.getNutritionistEmail());
        entity.setWaterLitres(request.waterLitres());
        entity.setMealsCompleted(request.mealsCompleted());
        entity.setNotes(textOrEmpty(request.notes()));
        LocalDate trackingDate = request.trackedDate() == null ? LocalDate.now() : request.trackedDate();
        if (nutritionTracking.existsByPatientEmailAndTrackedDate(email, trackingDate)) {
            throw new IllegalArgumentException("A nutrition record already exists for this date");
        }
        entity.setTrackedDate(trackingDate);
        NutritionTrackingEntity saved = nutritionTracking.save(entity);
        saveMealRecords(saved.getId(), request.meals());
        return mapTracking(List.of(saved)).get(0);
    }

    public PatientProgressRecord savePatientProgress(PatientProgressRequest request, String ownerEmail) {
        String email = normalizeEmail(ownerEmail);
        AccountEntity account = requirePatientAccount(email);
        NutritionPatientEntity assignment = nutritionPatients.findFirstByEmail(email).orElse(null);
        NutritionProgressEntity entity = new NutritionProgressEntity();
        entity.setPatientName(account.getName());
        entity.setPatientEmail(email);
        entity.setNutritionistEmail(assignment == null ? null : assignment.getNutritionistEmail());
        entity.setCurrentWeight(request.currentWeight());
        entity.setTargetWeight(request.targetWeight());
        entity.setProgress(request.progress().trim());
        entity.setNotes(textOrEmpty(request.notes()));
        NutritionProgressRecord record = map(nutritionProgress.save(entity));
        return new PatientProgressRecord(record.id(), record.patientName(), record.patientEmail(),
            record.currentWeight(), record.targetWeight(), record.progress(), record.notes(), record.recordedAt());
    }

    public PatientProfileRecord savePatientProfile(PatientProfileRequest request, String ownerEmail) {
        String email = normalizeEmail(ownerEmail);
        PatientProfileEntity entity = patientProfiles.findByEmail(email)
            .orElseGet(PatientProfileEntity::new);
        entity.setName(request.name().trim());
        entity.setEmail(email);
        entity.setPhone(request.phone().trim());
        entity.setDateOfBirth(request.dateOfBirth().trim());
        entity.setGender(request.gender().trim());
        entity.setHeightCm(request.heightCm());
        entity.setCurrentWeightKg(request.currentWeightKg());
        entity.setDisease(textOrEmpty(request.disease()));
        entity.setMedicalConditions(textOrEmpty(request.medicalConditions()));
        entity.setAllergies(textOrEmpty(request.allergies()));
        entity.setFoodPreferences(textOrEmpty(request.foodPreferences()));
        entity.setDietaryRestrictions(textOrEmpty(request.dietaryRestrictions()));
        entity.setFoodPreference(textOrEmpty(request.foodPreference()));
        entity.setWeightGoalKg(request.weightGoalKg());
        accounts.findByEmail(email).ifPresent(account -> {
            account.setName(request.name().trim());
            accounts.save(account);
        });
        return map(patientProfiles.save(entity));
    }

    @Transactional(readOnly = true)
    public PatientProfileRecord getPatientProfile(String email) {
        return patientProfiles.findByEmail(email.trim().toLowerCase()).map(ModuleService::map).orElse(null);
    }

    public PatientProfileRecord getPatientProfileOrAccount(String email) {
        AccountEntity account = requirePatientAccount(email);
        PatientProfileRecord profile = getPatientProfile(account.getEmail());
        if (profile != null) {
            return profile;
        }
        return emptyPatientProfile(account);
    }

    @Transactional(readOnly = true)
    public List<PatientProfileRecord> getPatientProfiles() {
        return accounts.findAllByRoleOrderByNameAsc("patient").stream()
            .map(account -> {
                PatientProfileRecord profile = getPatientProfile(account.getEmail());
                return profile == null ? emptyPatientProfile(account) : profile;
            })
            .toList();
    }

    private PatientProfileRecord emptyPatientProfile(AccountEntity account) {
        return new PatientProfileRecord(null, account.getName(), account.getEmail(), "", "", "",
            null, null, "", "", "", "", "", "", null);
    }

    @Transactional(readOnly = true)
    public List<AccountRecord> getNutritionists() {
        return getUsers(null, "nutritionist");
    }

    @Transactional(readOnly = true)
    public List<PatientRecord> getPatients() {
        return patients.findAll().stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<AccountRecord> getUsers(String search, String role) {
        String normalizedRole = role == null || role.isBlank() || role.equalsIgnoreCase("all")
            ? null
            : role.trim().toLowerCase(Locale.ROOT);
        if (normalizedRole != null
            && !List.of("patient", "nutritionist", "admin").contains(normalizedRole)) {
            throw new IllegalArgumentException("Role must be patient, nutritionist, admin or all");
        }
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return accounts.findAllByOrderByNameAsc().stream()
            .filter(account -> normalizedRole == null || account.getRole().equals(normalizedRole))
            .filter(account -> normalizedSearch.isEmpty()
                || account.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                || account.getEmail().toLowerCase(Locale.ROOT).contains(normalizedSearch))
            .map(ModuleService::map)
            .toList();
    }

    @Transactional(readOnly = true)
    public AccountRecord getUser(Long id) {
        return accounts.findById(id)
            .map(ModuleService::map)
            .orElseThrow(() -> new IllegalArgumentException("Registered account was not found"));
    }

    public AccountRecord setAccountEnabled(Long id, boolean enabled, String adminEmail) {
        AccountEntity account = accounts.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Registered account was not found"));
        if (!enabled && account.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("You cannot disable your own administrator account");
        }
        if (!enabled && account.getRole().equals("admin")
            && account.isEnabled() && accounts.countByRoleAndEnabledTrue("admin") <= 1) {
            throw new IllegalArgumentException("The clinic must keep at least one active administrator");
        }
        account.setEnabled(enabled);
        return map(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AccountRecord getAdminProfile(String email) {
        AccountEntity account = accounts.findByEmail(normalizeEmail(email))
            .filter(existing -> existing.getRole().equals("admin"))
            .orElseThrow(() -> new IllegalArgumentException("Administrator account was not found"));
        return map(account);
    }

    public AccountRecord updateAdminProfile(String email, String name) {
        AccountEntity account = accounts.findByEmail(normalizeEmail(email))
            .filter(existing -> existing.getRole().equals("admin"))
            .orElseThrow(() -> new IllegalArgumentException("Administrator account was not found"));
        account.setName(name.trim());
        return map(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public List<BmiRecord> getBmiAssessments(String email, String role) {
        List<BmiAssessmentEntity> records = role.equals("patient")
            ? bmiAssessments.findAllByPatientEmailOrderByAssessedAtDesc(normalizeEmail(email))
            : role.equals("nutritionist")
                ? bmiAssessments.findAllByNutritionistEmailOrderByAssessedAtDesc(normalizeEmail(email))
                : bmiAssessments.findAll();
        return records.stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<DietPlanRecord> getDietPlans(String email, String role) {
        List<DietPlanEntity> records = role.equals("patient")
            ? dietPlans.findAllByPatientEmail(normalizeEmail(email))
            : role.equals("nutritionist")
                ? dietPlans.findAllByNutritionistEmail(normalizeEmail(email))
                : dietPlans.findAll();
        return records.stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<NutritionTrackingRecord> getNutritionTracking(String email, String role) {
        List<NutritionTrackingEntity> records;
        if (role.equals("nutritionist")) {
            String nutritionistEmail = normalizeEmail(email);
            List<String> assignedPatientEmails = nutritionPatients.findAllByNutritionistEmail(nutritionistEmail).stream()
                .map(NutritionPatientEntity::getEmail)
                .filter(java.util.Objects::nonNull)
                .map(this::normalizeEmail)
                .filter(assignedEmail -> assignedEmail != null && !assignedEmail.isBlank())
                .toList();
            if (assignedPatientEmails.isEmpty()) {
                return List.of();
            }
            records = nutritionTracking.findAllByNutritionistEmailOrderByTrackedDateDesc(nutritionistEmail).stream()
                .filter(record -> assignedPatientEmails.contains(normalizeEmail(record.getPatientEmail())))
                .toList();
        } else if (role.equals("patient")) {
            records = nutritionTracking.findAllByPatientEmailOrderByTrackedDateDesc(normalizeEmail(email));
        } else {
            records = nutritionTracking.findAll();
        }
        return mapTracking(records);
    }

    @Transactional(readOnly = true)
    public List<NutritionPatientRecord> getNutritionPatients(String email, String role) {
        List<NutritionPatientEntity> records = role.equals("nutritionist")
            ? nutritionPatients.findAllByNutritionistEmail(normalizeEmail(email))
            : nutritionPatients.findAll();
        return records.stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<NutritionProgressRecord> getNutritionProgress(String email, String role) {
        List<NutritionProgressEntity> records;
        if (role.equals("nutritionist")) {
            String nutritionistEmail = normalizeEmail(email);
            List<String> assignedPatientEmails = nutritionPatients.findAllByNutritionistEmail(nutritionistEmail).stream()
                .map(NutritionPatientEntity::getEmail)
                .filter(java.util.Objects::nonNull)
                .map(this::normalizeEmail)
                .filter(assignedEmail -> assignedEmail != null && !assignedEmail.isBlank())
                .toList();
            if (assignedPatientEmails.isEmpty()) {
                return List.of();
            }
            records = nutritionProgress.findAllByNutritionistEmailOrderByRecordedAtDesc(nutritionistEmail).stream()
                .filter(record -> assignedPatientEmails.contains(normalizeEmail(record.getPatientEmail())))
                .toList();
        } else if (role.equals("patient")) {
            records = nutritionProgress.findAllByPatientEmailOrderByRecordedAtDesc(normalizeEmail(email));
        } else {
            records = nutritionProgress.findAll();
        }
        return records.stream().map(ModuleService::map).toList();
    }

    @Transactional(readOnly = true)
    public List<PatientBmiRecord> getPatientBmi(String email) {
        List<PatientBmiRecord> current = getBmiAssessments(email, "patient").stream()
            .map(record -> new PatientBmiRecord(record.id(), record.patientName(), record.patientEmail(),
                record.heightCm(), record.weightKg(), record.bmi(), record.assessedAt()))
            .toList();
        List<PatientBmiRecord> legacy = patientBmi.findAllByEmail(normalizeEmail(email)).stream()
            .map(ModuleService::map).toList();
        return java.util.stream.Stream.concat(legacy.stream(), current.stream()).toList();
    }

    @Transactional(readOnly = true)
    public List<NutritionTrackingRecord> getPatientNutritionTracking(String email) {
        List<NutritionTrackingRecord> current = getNutritionTracking(email, "patient");
        List<NutritionTrackingRecord> legacy = patientNutritionTracking.findAllByEmail(normalizeEmail(email)).stream()
            .map(ModuleService::map)
            .map(record -> new NutritionTrackingRecord(record.id(), record.patientName(), record.patientEmail(),
                record.waterLitres(), record.mealsCompleted(), record.notes(), null, List.of(),
                0.0, 0.0, 0.0, 0.0))
            .toList();
        return java.util.stream.Stream.concat(legacy.stream(), current.stream()).toList();
    }

    @Transactional(readOnly = true)
    public List<PatientProgressRecord> getPatientProgress(String email) {
        List<PatientProgressRecord> current = getNutritionProgress(email, "patient").stream()
            .map(record -> new PatientProgressRecord(record.id(), record.patientName(), record.patientEmail(),
                record.currentWeight(), record.targetWeight(), record.progress(), record.notes(), record.recordedAt()))
            .toList();
        List<PatientProgressRecord> legacy = patientProgress.findAllByEmail(normalizeEmail(email)).stream()
            .map(ModuleService::map).toList();
        return java.util.stream.Stream.concat(legacy.stream(), current.stream()).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalytics(String nutritionistEmail) {
        List<NutritionTrackingRecord> tracking = getNutritionTracking(nutritionistEmail, "nutritionist");
        List<NutritionProgressRecord> progress = getNutritionProgress(nutritionistEmail, "nutritionist");
        List<DietPlanRecord> plans = getDietPlans(nutritionistEmail, "nutritionist");
        double averageWater = tracking.stream().mapToDouble(NutritionTrackingRecord::waterLitres).average().orElse(0.0);
        double averageMeals = tracking.stream().mapToInt(NutritionTrackingRecord::mealsCompleted).average().orElse(0.0);
        double averageBmi = nutritionPatients.findAllByNutritionistEmail(normalizeEmail(nutritionistEmail)).stream()
            .flatMap(assignment -> getPatientBmi(assignment.getEmail()).stream())
            .map(PatientBmiRecord::bmi)
            .filter(value -> value != null && value > 0)
            .mapToDouble(Double::doubleValue)
            .average().orElse(0.0);
        Map<String, Long> progressStatuses = new LinkedHashMap<>();
        progress.forEach(record -> progressStatuses.merge(record.progress(), 1L, Long::sum));

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("trackedPatients", tracking.stream().map(NutritionTrackingRecord::patientName).distinct().count());
        analytics.put("patientsManaged", nutritionPatients.findAllByNutritionistEmail(normalizeEmail(nutritionistEmail)).size());
        analytics.put("dietPlans", plans.size());
        analytics.put("progressUpdates", progress.size());
        analytics.put("averageWaterLitres", round(averageWater));
        analytics.put("averageMealsCompleted", round(averageMeals));
        analytics.put("averageCalories", round(tracking.stream()
            .mapToDouble(NutritionTrackingRecord::totalCalories)
            .average()
            .orElse(0.0)));
        analytics.put("averageBmi", round(averageBmi));
        analytics.put("progressStatuses", progressStatuses);
        analytics.put("recentTracking", tracking);
        analytics.put("recentProgress", progress);
        analytics.put("recentDietPlans", plans);
        analytics.put("upcomingAppointments", getAppointments(nutritionistEmail, "nutritionist").stream()
            .filter(appointment -> appointment.startsAt().isAfter(LocalDateTime.now()))
            .filter(appointment -> List.of("Requested", "Confirmed").contains(appointment.status()))
            .count());
        analytics.put("pendingFollowUps", progress.stream()
            .filter(record -> record.progress().equalsIgnoreCase("Needs Attention"))
            .count());
        return analytics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("patients", accounts.countByRole("patient"));
        dashboard.put("nutritionists", accounts.countByRole("nutritionist"));
        dashboard.put("users", accounts.count());
        dashboard.put("dietPlans", dietPlans.count());
        dashboard.put("appointments", appointments.count());
        return dashboard;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientDashboard(String email) {
        String patientEmail = normalizeEmail(email);
        List<PatientBmiRecord> bmi = getPatientBmi(patientEmail);
        List<DietPlanRecord> plans = getDietPlans(patientEmail, "patient");
        List<NutritionTrackingRecord> tracking = getNutritionTracking(patientEmail, "patient");
        AppointmentRecord nextAppointment = getAppointments(patientEmail, "patient").stream()
            .filter(appointment -> appointment.startsAt().isAfter(LocalDateTime.now()))
            .filter(appointment -> List.of("Requested", "Confirmed").contains(appointment.status()))
            .findFirst()
            .orElse(null);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("latestBmi", bmi.isEmpty() ? null : bmi.get(0));
        dashboard.put("dietPlanCount", plans.size());
        dashboard.put("todayTracking", tracking.stream()
            .filter(record -> LocalDate.now().equals(record.trackedDate()))
            .findFirst().orElse(null));
        dashboard.put("nextAppointment", nextAppointment);
        return dashboard;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("users", accounts.count());
        report.put("patients", accounts.countByRole("patient"));
        report.put("nutritionists", accounts.countByRole("nutritionist"));
        report.put("dietPlans", dietPlans.count());
        report.put("bmiAssessments", bmiAssessments.count());
        report.put("trackingRecords", nutritionTracking.count());
        report.put("progressUpdates", nutritionProgress.count());
        report.put("assignedPatients", nutritionPatients.count());
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientReport(
        String patientEmail,
        String reportType,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        AccountEntity patient = requirePatientAccount(patientEmail);
        List<?> records;
        boolean dateFilteringSupported = !reportType.equals("Patient Health Details");

        switch (reportType) {
            case "Complete Patient Report" -> {
                List<Map<String, Object>> sections = new java.util.ArrayList<>();
                sections.add(reportSection("Health details", getPatientProfile(patient.getEmail())));
                sections.add(reportSection("BMI history", getPatientBmi(patient.getEmail()).stream()
                    .filter(record -> inDateRange(record.assessedAt(), fromDate, toDate)).toList()));
                sections.add(reportSection("Nutrition tracking", getPatientNutritionTracking(patient.getEmail()).stream()
                    .filter(record -> inDateRange(record.trackedDate(), fromDate, toDate)).toList()));
                sections.add(reportSection("Weight progress", getPatientProgress(patient.getEmail()).stream()
                    .filter(record -> inDateRange(record.recordedAt(), fromDate, toDate)).toList()));
                sections.add(reportSection("Diet plans", getDietPlans(patient.getEmail(), "patient").stream()
                    .filter(record -> inDateRange(record.createdAt(), fromDate, toDate)).toList()));
                sections.add(reportSection("Appointments", getAppointments(patient.getEmail(), "patient").stream()
                    .filter(record -> inDateRange(record.startsAt(), fromDate, toDate)).toList()));
                records = sections;
            }
            case "BMI Report" -> records = getPatientBmi(patient.getEmail()).stream()
                .filter(record -> inDateRange(record.assessedAt(), fromDate, toDate))
                .toList();
            case "Patient Health Details" -> {
                PatientProfileRecord profile = getPatientProfile(patient.getEmail());
                records = profile == null ? List.of() : List.of(profile);
            }
            case "Diet Plan Report" -> records = getDietPlans(patient.getEmail(), "patient").stream()
                .filter(record -> fromDate == null || (record.createdAt() != null
                    && !record.createdAt().toLocalDate().isBefore(fromDate)))
                .filter(record -> toDate == null || (record.createdAt() != null
                    && !record.createdAt().toLocalDate().isAfter(toDate)))
                .toList();
            case "BMI and Progress Report" -> {
                List<Map<String, Object>> sections = new java.util.ArrayList<>();
                sections.add(reportSection("BMI history", getPatientBmi(patient.getEmail()).stream()
                    .filter(record -> inDateRange(record.assessedAt(), fromDate, toDate)).toList()));
                sections.add(reportSection("Weight progress", getPatientProgress(patient.getEmail()).stream()
                    .filter(record -> inDateRange(record.recordedAt(), fromDate, toDate)).toList()));
                records = sections;
            }
            case "Nutrition Tracking Report" -> records = getPatientNutritionTracking(patient.getEmail()).stream()
                .filter(record -> inDateRange(record.trackedDate(), fromDate, toDate))
                .toList();
            case "Appointment Report" -> records = getAppointments(patient.getEmail(), "patient").stream()
                .filter(record -> inDateRange(record.startsAt(), fromDate, toDate))
                .toList();
            default -> throw new IllegalArgumentException("Select a supported patient report type");
        }

        Map<String, Object> patientReport = new LinkedHashMap<>();
        patientReport.put("patientName", patient.getName());
        patientReport.put("patientEmail", patient.getEmail());
        patientReport.put("reportType", reportType);
        patientReport.put("dateFilteringSupported", dateFilteringSupported);
        patientReport.put("records", records);
        return patientReport;
    }

    private Map<String, Object> reportSection(String title, Object sectionRecords) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", title);
        section.put("records", sectionRecords == null ? List.of()
            : sectionRecords instanceof List<?> values ? values
            : List.of(sectionRecords));
        return section;
    }

    private boolean inDateRange(LocalDateTime dateTime, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || dateTime != null && !dateTime.toLocalDate().isBefore(fromDate))
            && (toDate == null || dateTime != null && !dateTime.toLocalDate().isAfter(toDate));
    }

    private boolean inDateRange(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return (fromDate == null || date != null && !date.isBefore(fromDate))
            && (toDate == null || date != null && !date.isAfter(toDate));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("patients", accounts.countByRole("patient"));
        overview.put("nutritionists", accounts.countByRole("nutritionist"));
        overview.put("users", accounts.count());
        overview.put("dietPlans", dietPlans.count());
        overview.put("bmiAssessments", bmiAssessments.count());
        overview.put("trackingRecords", nutritionTracking.count());
        overview.put("progressUpdates", nutritionProgress.count());
        overview.put("patientProfiles", patientProfiles.count());
        overview.put("serviceStatus", "All services operational");
        return overview;
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void saveMealRecords(Long trackingId, List<MealRecordRequest> meals) {
        if (meals == null || meals.isEmpty()) {
            return;
        }
        List<MealRecordEntity> entities = meals.stream().map(meal -> {
            if (meal == null || meal.mealType() == null || meal.mealType().isBlank()
                || meal.foodName() == null || meal.foodName().isBlank()
                || meal.portion() == null || meal.portion().isBlank()
                || !nonNegativeFinite(meal.calories()) || !nonNegativeFinite(meal.proteinGrams())
                || !nonNegativeFinite(meal.carbohydratesGrams()) || !nonNegativeFinite(meal.fatGrams())) {
                throw new IllegalArgumentException("Each meal needs a type, food, portion and valid non-negative nutrient values");
            }
            MealRecordEntity entity = new MealRecordEntity();
            entity.setTrackingId(trackingId);
            entity.setMealType(meal.mealType().trim());
            entity.setFoodName(meal.foodName().trim());
            entity.setPortion(meal.portion().trim());
            entity.setCalories(meal.calories());
            entity.setProteinGrams(meal.proteinGrams());
            entity.setCarbohydratesGrams(meal.carbohydratesGrams());
            entity.setFatGrams(meal.fatGrams());
            return entity;
        }).toList();
        mealRecords.saveAll(entities);
    }

    private boolean nonNegativeFinite(Double value) {
        return value != null && Double.isFinite(value) && value >= 0;
    }

    private List<NutritionTrackingRecord> mapTracking(List<NutritionTrackingEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> ids = entities.stream().map(NutritionTrackingEntity::getId).toList();
        Map<Long, List<MealRecordEntity>> mealsByTracking = new java.util.HashMap<>();
        mealRecords.findAllByTrackingIdInOrderByIdAsc(ids).forEach(meal ->
            mealsByTracking.computeIfAbsent(meal.getTrackingId(), ignored -> new java.util.ArrayList<>()).add(meal));
        return entities.stream().map(entity -> {
            List<MealRecordView> meals = mealsByTracking.getOrDefault(entity.getId(), List.of()).stream()
                .map(meal -> new MealRecordView(meal.getId(), meal.getMealType(), meal.getFoodName(), meal.getPortion(),
                    meal.getCalories(), meal.getProteinGrams(), meal.getCarbohydratesGrams(), meal.getFatGrams()))
                .toList();
            return new NutritionTrackingRecord(entity.getId(), entity.getPatientName(), entity.getPatientEmail(),
                entity.getWaterLitres(), entity.getMealsCompleted(), entity.getNotes(), entity.getTrackedDate(), meals,
                round(meals.stream().mapToDouble(MealRecordView::calories).sum()),
                round(meals.stream().mapToDouble(MealRecordView::proteinGrams).sum()),
                round(meals.stream().mapToDouble(MealRecordView::carbohydratesGrams).sum()),
                round(meals.stream().mapToDouble(MealRecordView::fatGrams).sum()));
        }).toList();
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    public boolean isRegisteredPatient(String email) {
        return accounts.findByEmail(normalizeEmail(email))
            .filter(account -> account.getRole().equals("patient"))
            .isPresent();
    }

    private AccountEntity requirePatientAccount(String email) {
        return accounts.findByEmail(normalizeEmail(email))
            .filter(account -> account.getRole().equals("patient"))
            .orElseThrow(() -> new IllegalArgumentException("Patient must have a verified NutriCare patient account"));
    }

    private void requireAssignedPatient(String patientEmail, String nutritionistEmail) {
        if (nutritionistEmail != null
            && !nutritionPatients.existsByEmailAndNutritionistEmail(normalizeEmail(patientEmail), normalizeEmail(nutritionistEmail))) {
            throw new IllegalArgumentException("Add this registered patient to your patient list before saving care records");
        }
        AccountEntity patient = requirePatientAccount(patientEmail);
        if (!patient.isEnabled()) {
            throw new IllegalArgumentException("This patient account is disabled");
        }
    }

    private double calculateBmi(Double heightCm, Double weightKg) {
        double heightMetres = heightCm / 100.0;
        return round(weightKg / (heightMetres * heightMetres));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static PatientRecord map(PatientEntity entity) {
        return new PatientRecord(entity.getId(), entity.getName(), entity.getEmail(), entity.getPhone());
    }

    private static AccountRecord map(AccountEntity entity) {
        return new AccountRecord(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getRole(),
            entity.isEnabled()
        );
    }

    private static BmiRecord map(BmiAssessmentEntity entity) {
        return new BmiRecord(
            entity.getId(),
            entity.getPatientName(),
            entity.getPatientEmail(),
            entity.getHeightCm(),
            entity.getWeightKg(),
            entity.getBmi(),
            entity.getAssessedAt()
        );
    }

    private static DietPlanRecord map(DietPlanEntity entity) {
        return new DietPlanRecord(entity.getId(), entity.getPatientName(), entity.getPatientEmail(),
            entity.getGoal(), entity.getPlan(), entity.getNotes(), entity.getBreakfast(), entity.getLunch(),
            entity.getDinner(), entity.getSnacks(), entity.getCalorieTarget(), entity.getProteinTargetGrams(),
            entity.getCarbohydratesTargetGrams(), entity.getFatTargetGrams(), entity.getBreakfastPortion(),
            entity.getBreakfastTime(), entity.getLunchPortion(), entity.getLunchTime(), entity.getDinnerPortion(),
            entity.getDinnerTime(), entity.getSnacksPortion(), entity.getSnacksTime(),
            entity.getStatus(), entity.getCreatedAt());
    }

    private static NutritionTrackingRecord map(NutritionTrackingEntity entity) {
        return new NutritionTrackingRecord(entity.getId(), entity.getPatientName(), entity.getPatientEmail(),
            entity.getWaterLitres(), entity.getMealsCompleted(), entity.getNotes(), entity.getTrackedDate(),
            List.of(), 0.0, 0.0, 0.0, 0.0);
    }

    private static NutritionPatientRecord map(NutritionPatientEntity entity) {
        return new NutritionPatientRecord(entity.getId(), entity.getPatientName(), entity.getEmail(), entity.getNotes());
    }

    private static NutritionProgressRecord map(NutritionProgressEntity entity) {
        return new NutritionProgressRecord(entity.getId(), entity.getPatientName(), entity.getPatientEmail(),
            entity.getCurrentWeight(), entity.getTargetWeight(), entity.getProgress(), entity.getNotes(), entity.getRecordedAt());
    }

    private static PatientBmiRecord map(PatientBmiEntity entity) {
        return new PatientBmiRecord(
            entity.getId(),
            entity.getPatientName(),
            entity.getEmail(),
            entity.getHeightCm(),
            entity.getWeightKg(),
            entity.getBmi(),
            null
        );
    }

    private static PatientNutritionTrackingRecord map(PatientNutritionTrackingEntity entity) {
        return new PatientNutritionTrackingRecord(entity.getId(), entity.getPatientName(), entity.getEmail(), entity.getWaterLitres(), entity.getMealsCompleted(), entity.getNotes());
    }

    private static PatientProgressRecord map(PatientProgressEntity entity) {
        return new PatientProgressRecord(entity.getId(), entity.getPatientName(), entity.getEmail(),
            entity.getCurrentWeight(), entity.getTargetWeight(), entity.getProgress(), entity.getNotes(), null);
    }

    private static PatientProfileRecord map(PatientProfileEntity entity) {
        return new PatientProfileRecord(entity.getId(), entity.getName(), entity.getEmail(), entity.getPhone(),
            entity.getDateOfBirth(), entity.getGender(), entity.getHeightCm(), entity.getCurrentWeightKg(), entity.getDisease(),
            entity.getMedicalConditions(), entity.getAllergies(), entity.getFoodPreferences(),
            entity.getDietaryRestrictions(), entity.getFoodPreference(), entity.getWeightGoalKg());
    }

    private static AppointmentRecord map(AppointmentEntity entity) {
        return new AppointmentRecord(entity.getId(), entity.getPatientEmail(), entity.getPatientName(),
            entity.getNutritionistEmail(), entity.getNutritionistName(), entity.getStartsAt(), entity.getStatus(),
            entity.getPatientNote(), entity.getConsultationNote());
    }

    private static NutritionistProfileRecord map(NutritionistEntity entity) {
        return new NutritionistProfileRecord(entity.getName(), entity.getEmail(), entity.getPhone(),
            entity.getSpecialization(), entity.getQualification(), entity.getExperienceYears(), entity.getBio());
    }

    private static AvailabilityRecord map(NutritionistAvailabilityEntity entity) {
        return new AvailabilityRecord(entity.getDayOfWeek(), entity.getStartTime(), entity.getEndTime(), entity.isActive());
    }
}
