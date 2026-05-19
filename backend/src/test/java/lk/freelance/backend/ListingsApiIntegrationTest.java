package lk.freelance.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.UserRole;
import lk.freelance.backend.enums.UserStatus;
import lk.freelance.backend.repository.UserRepository;
import lk.freelance.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListingsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        User owner = createUser("owner@listing.test");
        User other = createUser("other@listing.test");
        ownerToken = jwtTokenProvider.generateAccessToken(owner.getUserId(), owner.getEmail());
        otherToken = jwtTokenProvider.generateAccessToken(other.getUserId(), other.getEmail());
    }

    @Test
    void createGigAndReadPublicEndpoints() throws Exception {
        String listingId = createGigAndReturnListingId();

        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.listingId").value(listingId))
                .andExpect(jsonPath("$.data.type").value("GIG"));

        mockMvc.perform(get("/api/listings")
                        .param("type", "GIG")
                        .param("category", "Design")
                        .param("keyword", "Logo")
                        .param("minPrice", "100")
                        .param("maxPrice", "1000")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].listingId").value(listingId));
    }

    @Test
    void createJobAndFilterByTypeAndPrice() throws Exception {
        Map<String, Object> jobPayload = new HashMap<>();
        jobPayload.put("title", "Frontend React Contractor");
        jobPayload.put("description", "Looking for a React developer with API integration experience.");
        jobPayload.put("employmentType", "CONTRACT");
        jobPayload.put("hourlyRate", 1500);
        jobPayload.put("monthlySalary", null);
        jobPayload.put("location", "Colombo");
        jobPayload.put("isRemote", true);

        mockMvc.perform(post("/api/listings/jobs")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("PART_TIME_JOB"));

        mockMvc.perform(get("/api/listings")
                        .param("type", "JOB")
                        .param("minPrice", "1400")
                        .param("maxPrice", "1600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].type").value("PART_TIME_JOB"));
    }

    @Test
    void updateAndDeleteRequireOwnership() throws Exception {
        String listingId = createGigAndReturnListingId();

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Updated Logo Design Package");
        updatePayload.put("description", "Updated premium logo design with brand kit and source files.");
        updatePayload.put("category", "Design");
        updatePayload.put("pricingModel", "FIXED");
        updatePayload.put("basePrice", 800);
        updatePayload.put("deliveryDays", 3);

        mockMvc.perform(put("/api/listings/{id}", listingId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ACCESS"));

        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_ACCESS"));

        mockMvc.perform(put("/api/listings/{id}", listingId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Logo Design Package"));

        mockMvc.perform(delete("/api/listings/{id}", listingId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/listings/{id}", listingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGigValidationFailureReturnsBadRequest() throws Exception {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("title", "");
        invalidPayload.put("description", "short");
        invalidPayload.put("category", "");
        invalidPayload.put("pricingModel", "FIXED");
        invalidPayload.put("basePrice", -1);
        invalidPayload.put("deliveryDays", 0);

        mockMvc.perform(post("/api/listings/gigs")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    private String createGigAndReturnListingId() throws Exception {
        Map<String, Object> gigPayload = new HashMap<>();
        gigPayload.put("title", "Logo Design Service");
        gigPayload.put("description", "Professional logo design package with source files and style guide.");
        gigPayload.put("category", "Design");
        gigPayload.put("pricingModel", "FIXED");
        gigPayload.put("basePrice", 500);
        gigPayload.put("deliveryDays", 4);

        MvcResult result = mockMvc.perform(post("/api/listings/gigs")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gigPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("GIG"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("listingId").asText();
    }

    private User createUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash("hashed-password")
                .role(UserRole.FREELANCER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .build());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
