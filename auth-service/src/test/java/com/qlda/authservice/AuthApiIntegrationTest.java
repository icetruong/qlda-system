package com.qlda.authservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import com.qlda.authservice.security.JwtService;
import com.qlda.authservice.service.RefreshTokenStoreService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private NhomQuyenRepository nhomQuyenRepository;

    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenStoreService refreshTokenStoreService;

    @Autowired
    private AuthProperties authProperties;

    @BeforeEach
    void setupData() {
        phanQuyenRepository.deleteAll();
        nguoiDungRepository.deleteAll();
        nhomQuyenRepository.deleteAll();
        donViRepository.deleteAll();

        DonVi donVi = new DonVi();
        donVi.setMaDonVi("DV01");
        donVi.setTenDonVi("Phong HC");
        donVi.setSuDung(true);
        DonVi savedDonVi = donViRepository.save(donVi);

        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setMaNhomQuyen("ADMIN");
        nhomQuyen.setTenNhomQuyen("Admin");
        nhomQuyen.setSuDung(true);
        NhomQuyen savedNhomQuyen = nhomQuyenRepository.save(nhomQuyen);

        NguoiDung user = new NguoiDung();
        user.setUserName("admin");
        user.setHoTen("Quan Tri");
        user.setEmail("admin@company.com");
        user.setDonVi(savedDonVi);
        user.setNhomQuyen(savedNhomQuyen);
        user.setTrangThai(1);
        nguoiDungRepository.save(user);
    }

    @Test
    void protectedEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void protectedEndpointShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void meEndpointShouldReturnCurrentUserWhenTokenProvided() throws Exception {
        JsonNode loginData = loginAndGetTokenData();
        String accessToken = loginData.path("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void refreshTokenShouldReturnNewTokens() throws Exception {
        JsonNode loginData = loginAndGetTokenData();
        String accessToken = loginData.path("accessToken").asText();
        String refreshToken = loginData.path("refreshToken").asText();
        String payload = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));

        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void logoutShouldRevokeRefreshToken() throws Exception {
        JsonNode loginData = loginAndGetTokenData();
        String accessToken = loginData.path("accessToken").asText();
        String refreshToken = loginData.path("refreshToken").asText();
        String logoutPayload = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String refreshPayload = objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    private JsonNode loginAndGetTokenData() {
        NguoiDung user = nguoiDungRepository.findByUserName("admin").orElseThrow();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenStoreService.save(
                refreshToken,
                user.getId(),
                user.getUserName(),
                Instant.now().plusSeconds(authProperties.getJwt().getRefreshTokenSeconds())
        );
        return objectMapper.valueToTree(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
}
