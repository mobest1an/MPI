package com.iverpa.mpi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iverpa.mpi.AbstractIntegrationTest;
import com.iverpa.mpi.controller.dto.requests.SubmitComplaintRequest;
import com.iverpa.mpi.dao.SummonService;
import com.iverpa.mpi.dao.UserService;
import com.iverpa.mpi.service.ConvoyService;
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
class PublicComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SummonService summonService;

    @Autowired
    private ConvoyService convoyService;

    @Value("${private.key.filename}")
    private String privateKeyFilename;

    @Test
    void getActiveConvoys_ShouldReturnListOfConvoys() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/public/convoys")
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void submitComplaint_ShouldCreateNewComplaint() throws Exception {
        // Act & Assert
        SubmitComplaintRequest request = new SubmitComplaintRequest(1L);
        
        mockMvc.perform(post("/api/v1/public/complaint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isOk());
    }

    @Test
    void submitComplaint_ShouldReturnBadRequest_WhenConvoyNotFound() throws Exception {
        // Act & Assert
        SubmitComplaintRequest request = new SubmitComplaintRequest(999L);
        
        mockMvc.perform(post("/api/v1/public/complaint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + generateToken("recruit1", "RECRUIT")))
                .andExpect(status().isBadRequest());
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