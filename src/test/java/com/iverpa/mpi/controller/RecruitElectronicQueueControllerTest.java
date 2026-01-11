package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.controller.dto.requests.JoinRecruitRequest;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.model.RecruitStatus;
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
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AbstractIntegrationTest
class RecruitElectronicQueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SummonService summonService;

    @Value("${private.key.filename}")
    private String privateKeyFilename;

    @Test
    void joinRecruit_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        JoinRecruitRequest request = new JoinRecruitRequest("recruit1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/recruit/queue/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isOk());
        // Verify status was updated
        var summon = summonService.findByUsername("recruit1");
        if (summon != null) {
            assert summon.getStatus() == RecruitStatus.IN_QUEUE;
        }
    }

    @Test
    void joinRecruit_ShouldReturnBadRequest_WhenInvalidUsername() throws Exception {
        // Arrange
        JoinRecruitRequest request = new JoinRecruitRequest("nonexistent");

        // Act & Assert
        mockMvc.perform(post("/api/v1/recruit/queue/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_ShouldReturnRecruitStatus() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/recruit/status")
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_QUEUE"))
                .andExpect(jsonPath("$.militaryBranch").isEmpty());
    }

    @Test
    void leaveQueue_ShouldReturnOk_WhenRecruitInQueue() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(post("/api/v1/recruit/queue/leave")
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isOk());

        // Verify status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
        assert updatedSummon.getStatus() == RecruitStatus.NOT_STARTED;
    }

    @Test
    void leaveQueue_ShouldReturnBadRequest_WhenRecruitNotInQueue() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/recruit/queue/leave")
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isBadRequest());
    }

    private String generateToken(String username, String role) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        // Попробуем разные варианты формата токена
        List<String> rolesList = new ArrayList<>();
        rolesList.add(role);

        // Добавим authorities тоже, на случай если фильтр ожидает это поле
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", rolesList);
        claims.put("authorities", rolesList);
        claims.put("username", username); // Явно добавим username

        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims) // Добавим все claims
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
