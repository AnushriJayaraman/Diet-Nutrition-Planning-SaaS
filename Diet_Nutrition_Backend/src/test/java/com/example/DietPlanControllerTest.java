package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
class DietPlanControllerTest {

    private static final String TEST_DATA_FILE = Path.of(
        "target", "test-data", "diet-plans-" + UUID.randomUUID() + ".json"
    ).toString();

    @DynamicPropertySource
    static void useIsolatedDataFile(DynamicPropertyRegistry registry) {
        registry.add("app.data.file", () -> TEST_DATA_FILE);
    }

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

    @Test
    void saveDietPlanShouldWork() throws Exception {
        AccountEntity patient = accountRepository.findByEmail("gobi@example.com")
            .orElseGet(() -> accountRepository.save(new AccountEntity("gobi", "gobi@example.com", "test-hash", "patient")));
        AccountEntity nutritionistAccount = accountRepository.findByEmail("nutritionist@example.com")
            .orElseGet(() -> accountRepository.save(new AccountEntity(
                "Nutritionist One", "nutritionist@example.com", "test-hash", "nutritionist"
            )));
        nutritionistAccount.setEnabled(true);
        accountRepository.save(nutritionistAccount);
        patient.setEnabled(true);
        accountRepository.save(patient);
        AccountEntity otherPatient = accountRepository.findByEmail("another@example.com")
            .orElseGet(() -> accountRepository.save(new AccountEntity(
                "Another Patient", "another@example.com", "test-hash", "patient"
            )));
        otherPatient.setEnabled(true);
        accountRepository.save(otherPatient);
        if (!nutritionPatientRepository.existsByEmailAndNutritionistEmail(patient.getEmail(), "nutritionist@example.com")) {
            NutritionPatientEntity assignment = new NutritionPatientEntity();
            assignment.setPatientName(patient.getName());
            assignment.setEmail(patient.getEmail());
            assignment.setNutritionistEmail("nutritionist@example.com");
            assignment.setNotes("");
            nutritionPatientRepository.save(assignment);
        }
        Map<String, String> request = Map.of(
            "patientName", patient.getName(),
            "patientEmail", "gobi@example.com",
            "goal", "Healthy Nutrition",
            "plan", "have a healthy foods",
            "notes", "do exercise regularly"
        );

        mockMvc.perform(post("/api/diet-plans")
                .header("Authorization", "Bearer " + authTokenService.issue("nutritionist@example.com", "nutritionist"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Diet plan saved successfully"));

        mockMvc.perform(get("/api/diet-plans")
                .header("Authorization", "Bearer " + authTokenService.issue("nutritionist@example.com", "nutritionist")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dietPlans[0].patientName").value("gobi"))
            .andExpect(jsonPath("$.dietPlans[0].goal").value("Healthy Nutrition"));

        mockMvc.perform(get("/api/diet-plans")
                .header("Authorization", "Bearer " + authTokenService.issue("gobi@example.com", "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dietPlans[0].patientEmail").value("gobi@example.com"));

        mockMvc.perform(get("/api/diet-plans")
                .header("Authorization", "Bearer " + authTokenService.issue("another@example.com", "patient")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dietPlans").isEmpty());
    }
}
