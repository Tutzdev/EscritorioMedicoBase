package io.github.officemed.medical_office_api.integration;

import com.jayway.jsonpath.JsonPath;
import io.github.officemed.medical_office_api.user.entity.Role;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthorizationIntegrationTest {

    private static final String PASSWORD = "senha-de-teste-forte";

    private static final String NEW_PATIENT = """
            {
              "name": "Maria da Silva",
              "cpf": "529.982.247-25",
              "birthDate": "1990-04-12",
              "phone": "(24) 99999-0000",
              "email": "maria@example.com"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createUsers() {
        createUser("admin@clinica.test", Role.ADMIN);
        createUser("recepcao@clinica.test", Role.RECEPTIONIST);
        createUser("medico@clinica.test", Role.DOCTOR);
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin@clinica.test", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIssueAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin@clinica.test", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokens.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokens.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedRoutes() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLetReceptionRegisterPatients() throws Exception {
        String token = login("recepcao@clinica.test");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_PATIENT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria da Silva"));
    }

    @Test
    void shouldForbidDoctorsFromRegisteringPatients() throws Exception {
        String token = login("medico@clinica.test");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_PATIENT))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidReceptionFromManagingUsers() throws Exception {
        String token = login("recepcao@clinica.test");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCpf() throws Exception {
        String token = login("recepcao@clinica.test");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_PATIENT.replace("529.982.247-25", "111.111.111-11")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAnswerBadRequestForMalformedJson() throws Exception {
        String token = login("recepcao@clinica.test");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest());
    }

    private void createUser(String email, Role role) {
        userRepository.save(User.create(role.name(), email, passwordEncoder.encode(PASSWORD), role));
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.tokens.accessToken");
    }

    private static String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}
