package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.controller.dto.requests.CreateConvoyRequest;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.dao.repository.ConvoyRepository;
import com.iverpa.mpi.model.RecruitStatus;
import com.iverpa.mpi.service.ComplaintService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
class EscortWaitingRoomControllerTest {

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
    private ComplaintService complaintService;

    @Value("${private.key.filename}")
    private String privateKeyFilename;

    @Test
    void getWaitingRoom_ShouldReturnEmptyList_WhenNoRecruitsWaiting() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/room")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getWaitingRoom_ShouldReturnRecruits_WhenRecruitsAreWaiting() throws Exception {
        // Arrange - помещаем призывника в зал ожидания
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summon.setMilitaryBranch("Пехота");
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/room")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("recruit1"))
                .andExpect(jsonPath("$[0].militaryBranch").value("Пехота"));
    }

    @Test
    void getActiveConvoy_ShouldReturnConvoy_WhenEscortHasActiveConvoy() throws Exception {
        // Arrange - у escort1 уже есть конвой из test-data.sql

        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convoyId").value(1));
    }

    @Test
    void hasActiveConvoy_ShouldReturnTrue_WhenEscortHasActiveConvoy() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy/exists")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void hasActiveConvoy_ShouldReturnFalse_WhenEscortHasNoActiveConvoy() throws Exception {
        // Arrange - удаляем конвой escort1
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy/exists")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void createConvoy_ShouldReturnConvoy_WhenValidRequest() throws Exception {
        // Arrange - удаляем существующий конвой и подготавливаем призывника
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summon.setMilitaryBranch("Пехота");
        summonService.save(summon);

        CreateConvoyRequest request = new CreateConvoyRequest(List.of(summon.getId()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convoyId").isNotEmpty())
                .andExpect(jsonPath("$.recruits").isArray())
                .andExpect(jsonPath("$.recruits[0].username").value("recruit1"));

        // Verify recruit status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
        assert updatedSummon.getStatus() == RecruitStatus.IN_CONVOY;
    }

    @Test
    void createConvoy_ShouldReturnError_WhenEscortAlreadyHasConvoy() throws Exception {
        // Arrange - у escort1 уже есть конвой
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summonService.save(summon);

        CreateConvoyRequest request = new CreateConvoyRequest(List.of(summon.getId()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createConvoy_ShouldReturnError_WhenNoRecruitsSelected() throws Exception {
        // Arrange - удаляем существующий конвой
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        CreateConvoyRequest request = new CreateConvoyRequest(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createConvoy_ShouldReturnError_WhenRecruitNotWaitingEscort() throws Exception {
        // Arrange - удаляем существующий конвой
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        // Призывник в статусе NOT_STARTED
        var summon = summonService.findByUsername("recruit1");
        CreateConvoyRequest request = new CreateConvoyRequest(List.of(summon.getId()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dismissConvoy_ShouldReturnOk_WhenEscortHasActiveConvoy() throws Exception {
        // Arrange - привязываем призывника к конвою
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        var convoy = convoyRepository.findById(1L).orElse(null);
        summon.setStatus(RecruitStatus.IN_CONVOY);
        summon.setConvoy(convoy);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/dismiss")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk());

        // Verify recruit status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
        assert updatedSummon.getStatus() == RecruitStatus.DONE;
        assert updatedSummon.getConvoy() == null;
    }

    @Test
    void dismissConvoy_ShouldReturnError_WhenEscortHasNoActiveConvoy() throws Exception {
        // Arrange - удаляем конвой
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        // Act & Assert
        mockMvc.perform(post("/api/v1/escort/convoy/dismiss")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComplaintsCount_ShouldReturnZero_WhenNoComplaints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy/complaints-count")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getComplaintsCount_ShouldReturnCount_WhenComplaintsExist() throws Exception {
        // Arrange - создаём жалобу на конвой
        complaintService.submitComplaint(1L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy/complaints-count")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getComplaintsCount_ShouldReturnZero_WhenNoActiveConvoy() throws Exception {
        // Arrange - удаляем конвой
        var convoy = convoyRepository.findById(1L).orElse(null);
        if (convoy != null) {
            convoyRepository.delete(convoy);
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/escort/convoy/complaints-count")
                .header("Authorization", "Bearer " + generateToken("escort1", "ESCORT")))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
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
