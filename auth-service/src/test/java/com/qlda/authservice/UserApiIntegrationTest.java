package com.qlda.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import com.qlda.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiIntegrationTest {

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

    private Integer donViId;
    private Integer nhomQuyenId;

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
        donViId = savedDonVi.getId();

        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setMaNhomQuyen("ADMIN");
        nhomQuyen.setTenNhomQuyen("Admin");
        nhomQuyen.setSuDung(true);
        NhomQuyen savedNhomQuyen = nhomQuyenRepository.save(nhomQuyen);
        nhomQuyenId = savedNhomQuyen.getId();

        NguoiDung admin = new NguoiDung();
        admin.setUserName("admin");
        admin.setHoTen("Quan Tri");
        admin.setEmail("admin@company.com");
        admin.setDonVi(savedDonVi);
        admin.setNhomQuyen(savedNhomQuyen);
        admin.setTrangThai(1);
        nguoiDungRepository.save(admin);
    }

    @Test
    void createUserShouldReturnUnauthorizedWhenNoToken() throws Exception {
        String payload = objectMapper.writeValueAsString(buildCreateUserPayload("user1", "u1@company.com"));

        mockMvc.perform(post("/api/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void createUserShouldCreateWhenTokenValid() throws Exception {
        String accessToken = loginAndGetAccessToken();
        String payload = objectMapper.writeValueAsString(buildCreateUserPayload("newuser", "newuser@company.com"));

        mockMvc.perform(post("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty());

        assertTrue(nguoiDungRepository.findByUserName("newuser").isPresent());
    }

    @Test
    void createUserShouldReturnBadRequestWhenInvalidPayload() throws Exception {
        String accessToken = loginAndGetAccessToken();
        String invalidPayload = objectMapper.writeValueAsString(Map.of(
                "username", "",
                "hoTen", "",
                "email", "invalid-email"
        ));

        mockMvc.perform(post("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void getUsersShouldReturnDataWhenAuthenticated() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(get("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    private String loginAndGetAccessToken() throws Exception {
        NguoiDung user = nguoiDungRepository.findByUserName("admin").orElseThrow();
        return jwtService.generateAccessToken(user);
    }

    private Map<String, Object> buildCreateUserPayload(String username, String email) {
        return Map.of(
                "username", username,
                "hoTen", "Nguoi Dung Moi",
                "email", email,
                "dienThoai", "0909123456",
                "donViId", donViId,
                "chucVu", "Nhan vien",
                "nhomQuyenId", nhomQuyenId,
                "azureAdId", "azure-new-user"
        );
    }
}
