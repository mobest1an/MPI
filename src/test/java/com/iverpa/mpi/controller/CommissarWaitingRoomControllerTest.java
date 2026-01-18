package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.controller.dto.requests.SendToWaitingRoomRequest;
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
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AbstractIntegrationTest
class CommissarWaitingRoomControllerTest {

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
    void sendRecruitToWaitingRoom_ShouldReturnOk_WhenRecruitIsSummoned() throws Exception {
        // Arrange - устанавливаем статус SUMMONED для призывника
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.SUMMONED);
        summonService.save(summon);

        SendToWaitingRoomRequest request = new SendToWaitingRoomRequest("recruit1", "Пехота");

        // Act & Assert
        mockMvc.perform(post("/api/v1/commissar/room/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk());

        // Verify status was updated
        var updatedSummon = summonService.findByUsername("recruit1");
        assert updatedSummon.getStatus() == RecruitStatus.WAITING_ESCORT;
        assert updatedSummon.getMilitaryBranch().equals("Пехота");
    }

    @Test
    void sendRecruitToWaitingRoom_ShouldReturnError_WhenRecruitIsNotSummoned() throws Exception {
        // Arrange - статус NOT_STARTED (по умолчанию из test-data.sql)
        SendToWaitingRoomRequest request = new SendToWaitingRoomRequest("recruit1", "Пехота");

        // Act & Assert
        mockMvc.perform(post("/api/v1/commissar/room/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendRecruitToWaitingRoom_ShouldSetMilitaryBranch() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.SUMMONED);
        summonService.save(summon);

        String militaryBranch = "Танковые войска";
        SendToWaitingRoomRequest request = new SendToWaitingRoomRequest("recruit1", militaryBranch);

        // Act
        mockMvc.perform(post("/api/v1/commissar/room/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk());

        // Assert
        var updatedSummon = summonService.findByUsername("recruit1");
        assert updatedSummon.getMilitaryBranch().equals(militaryBranch);
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnTrue_WhenStatusIsWaitingEscort() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.WAITING_ESCORT);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnTrue_WhenStatusIsInConvoy() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_CONVOY);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnTrue_WhenStatusIsDone() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.DONE);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnFalse_WhenStatusIsNotStarted() throws Exception {
        // Arrange - статус NOT_STARTED (по умолчанию)

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnFalse_WhenStatusIsInQueue() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.IN_QUEUE);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void userExistsInWaitingRoom_ShouldReturnFalse_WhenStatusIsSummoned() throws Exception {
        // Arrange
        var user = userService.findByUsername("recruit1");
        var summon = summonService.findByUserId(user.getId());
        summon.setStatus(RecruitStatus.SUMMONED);
        summonService.save(summon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/commissar/room/exists/recruit1")
                .header("Authorization", "Bearer " + generateToken("commissar1", "COMMISSAR")))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
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
