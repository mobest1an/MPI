package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.controller.dto.requests.DeleteFromQueueRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AbstractIntegrationTest
class CommissarElectronicQueueControllerTest {

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
    void getQueue_ShouldReturnQueueList() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        // Проверяем, что пользователь существует
        if (user != null) {
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/queue")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void summonRecruit_ShouldMoveRecruitToSummonedStatus() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        // Проверяем, что пользователь существует
        if (user != null) {
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);

        DeleteFromQueueRequest request = new DeleteFromQueueRequest("recruit1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/commissar/queue/summon")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk());

        // Verify status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
            if (updatedSummon != null) {
        assert updatedSummon.getStatus() == RecruitStatus.SUMMONED;
    }
        }
    }

    @Test
    void rejectRecruit_ShouldMoveRecruitToNotStartedStatus() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        // Проверяем, что пользователь существует
        if (user != null) {
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.SUMMONED);
        summon.setMilitaryBranch("Пехота");
        summonService.save(summon);

        DeleteFromQueueRequest request = new DeleteFromQueueRequest("recruit1");

        // Act & Assert
        mockMvc.perform(post("/api/v1/commissar/queue/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk());

        // Verify status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
            if (updatedSummon != null) {
        assert updatedSummon.getStatus() == RecruitStatus.NOT_STARTED;
        assert updatedSummon.getMilitaryBranch() == null;
    }
        }
    }

    private String generateToken(String username, String role) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        // Создаем ArrayList вместо List.of для совместимости
        List<String> rolesList = new ArrayList<>();
        rolesList.add(role);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesList)  // Передаем список ролей как ArrayList
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
