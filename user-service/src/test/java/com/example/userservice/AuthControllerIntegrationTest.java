package com.example.userservice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.userservice.service.UserEventProducer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret",
        "app.jwt.access-token-expiration-ms=900000",
        "app.jwt.refresh-token-expiration-ms=604800000",
        "app.security.issuer=test-issuer",
        "app.kafka.topic=user-events",
        "spring.kafka.bootstrap-servers=localhost:9092",
    }
)
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("crypto")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserEventProducer userEventProducer;

    @Test
    void registerLoginRefreshFlow() throws Exception {
        String username = "alice_" + System.currentTimeMillis();
        String email = username + "@example.com";
        String password = "password123";

        String registerPayload = objectMapper.writeValueAsString(
            Map.of("username", username, "email", email, "password", password)
        );

        String registerResponse = mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerPayload)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.createdAt", notNullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode registerJson = objectMapper.readTree(registerResponse);
        String userId = registerJson.get("userId").asText();
        org.junit.jupiter.api.Assertions.assertNotNull(userId);

        String loginPayload = objectMapper.writeValueAsString(
            Map.of("username", username, "password", password)
        );

        String loginResponse = mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginPayload)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn", notNullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken = loginJson.get("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotNull(refreshToken);

        String refreshPayload = objectMapper.writeValueAsString(
            Map.of("refreshToken", refreshToken)
        );

        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(refreshPayload)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.expiresIn", notNullValue()));
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        String payload = objectMapper.writeValueAsString(
            Map.of("username", "shortpass", "email", "shortpass@example.com", "password", "123")
        );

        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Validation failed")));
    }

    @Test
    void refreshRejectsInvalidToken() throws Exception {
        String payload = objectMapper.writeValueAsString(
            Map.of("refreshToken", "invalid-token")
        );

        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            )
            .andExpect(status().isUnauthorized());
    }
}
