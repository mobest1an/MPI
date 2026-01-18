package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.dao.repository.ComplaintRepository;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.model.*;
import com.iverpa.mpi.service.ComplaintService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AbstractIntegrationTest
class MilitaryPoliceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SummonService summonService;

    @Autowired
    private ConvoyRepository convoyRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintService complaintService;

    @Value("${private.key.filename}")
    private String privateKeyFilename;

    private User policeUser;
    private Convoy convoy;

    @BeforeEach
    void setUp() {
        // Создаём пользователя военной полиции для тестов
        policeUser = userService.findByUsername("admin1");
        convoy = convoyRepository.findById(1L).orElse(null);
    }

    @Test
    void getComplaints_ShouldReturnEmptyList_WhenNoComplaints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/military-police/complaints")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getComplaints_ShouldReturnComplaintGroups_WhenComplaintsExist() throws Exception {
        // Arrange - создаём жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/military-police/complaints")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getActiveComplaint_ShouldReturnNull_WhenNoActiveComplaint() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/military-police/complaints/active")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void takeComplaint_ShouldReturnOk_WhenComplaintExists() throws Exception {
        // Arrange - создаём жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
        }

        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/1/take")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk());
    }

    @Test
    void takeComplaint_ShouldReturnError_WhenConvoyNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/999/take")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeComplaint_ShouldReturnOk_WhenHasActiveComplaint() throws Exception {
        // Arrange - создаём и берём жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
            complaintService.takeComplaint(convoy.getId(), policeUser);
        }

        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/1/complete")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk());
    }

    @Test
    void completeComplaint_ShouldReturnError_WhenNoActiveComplaint() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/1/complete")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelComplaint_ShouldReturnOk_WhenHasActiveComplaint() throws Exception {
        // Arrange - создаём и берём жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
            complaintService.takeComplaint(convoy.getId(), policeUser);
        }

        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/1/cancel")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk());
    }

    @Test
    void cancelComplaint_ShouldReturnError_WhenNoActiveComplaint() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/military-police/complaints/1/cancel")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getActiveComplaint_ShouldReturnComplaint_WhenUserHasActiveComplaint() throws Exception {
        // Arrange - создаём и берём жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
            complaintService.takeComplaint(convoy.getId(), policeUser);
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/military-police/complaints/active")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convoyId").value(1));
    }

    @Test
    void takeComplaint_ShouldReturnError_WhenUserAlreadyHasActiveComplaint() throws Exception {
        // Arrange - создаём и берём первую жалобу
        if (convoy != null) {
            complaintService.submitComplaint(convoy.getId());
            complaintService.submitComplaint(convoy.getId()); // Вторая жалоба
            complaintService.takeComplaint(convoy.getId(), policeUser);
        }

        // Act & Assert - пытаемся взять вторую
        mockMvc.perform(post("/api/v1/military-police/complaints/1/take")
                .header("Authorization", "Bearer " + generateToken("admin1", "MILITARY_POLICE")))
                .andExpect(status().isBadRequest());
    }

    private String generateToken(String username, String role) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        List<String> rolesList = new ArrayList<>();
        rolesList.add(role);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", rolesList);
        claims.put("authorities", rolesList);
        claims.put("username", username);

        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(privateKeyFilename).toURI()));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
