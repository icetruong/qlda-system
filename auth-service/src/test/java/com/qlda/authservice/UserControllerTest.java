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
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private NhomQuyenRepository nhomQuyenRepository;

    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    private Integer donViId;
    private Integer nhomQuyenId;
    private Integer secondNhomQuyenId;
    private String accessToken;

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
        donViId = donViRepository.save(donVi).getId();

        NhomQuyen role1 = new NhomQuyen();
        role1.setMaNhomQuyen("ADMIN");
        role1.setTenNhomQuyen("Admin");
        role1.setSuDung(true);
        nhomQuyenId = nhomQuyenRepository.save(role1).getId();

        NhomQuyen role2 = new NhomQuyen();
        role2.setMaNhomQuyen("CHUYEN_VIEN");
        role2.setTenNhomQuyen("Chuyen Vien");
        role2.setSuDung(true);
        secondNhomQuyenId = nhomQuyenRepository.save(role2).getId();

        NguoiDung admin = new NguoiDung();
        admin.setUserName("admin");
        admin.setHoTen("Quan Tri");
        admin.setEmail("admin@company.com");
        admin.setDonVi(donViRepository.findById(donViId).orElseThrow());
        admin.setNhomQuyen(nhomQuyenRepository.findById(nhomQuyenId).orElseThrow());
        admin.setTrangThai(1);
        admin = nguoiDungRepository.save(admin);
        accessToken = jwtService.generateAccessToken(admin);
    }

    @Test
    void getUsersShouldSuccess() throws Exception {
        mockMvc.perform(get("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUserDetailShouldSuccess() throws Exception {
        Long id = createUser("detailuser", "detail@company.com");
        mockMvc.perform(get("/api/auth/users/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    void createUserShouldSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload("createuser", "create@company.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void updateUserShouldSuccess() throws Exception {
        Long id = createUser("updateuser", "update_old@company.com");
        mockMvc.perform(put("/api/auth/users/{id}", id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "hoTen", "Update Name",
                                "email", "update_new@company.com",
                                "dienThoai", "0909",
                                "donViId", donViId,
                                "chucVu", "Lead",
                                "nhomQuyenId", nhomQuyenId,
                                "trangThai", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    void patchStatusShouldSuccess() throws Exception {
        Long id = createUser("statususer", "status@company.com");
        mockMvc.perform(patch("/api/auth/users/{id}/status", id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("trangThai", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value(0));
    }

    @Test
    void patchRoleShouldSuccess() throws Exception {
        Long id = createUser("roleuser", "role@company.com");
        mockMvc.perform(patch("/api/auth/users/{id}/role", id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nhomQuyenId", secondNhomQuyenId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nhomQuyenId").value(secondNhomQuyenId));
    }

    @Test
    void deleteUserShouldSoftDeleteSuccess() throws Exception {
        Long id = createUser("deleteuser", "delete@company.com");
        mockMvc.perform(delete("/api/auth/users/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    private Long createUser(String username, String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload(username, email))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Map<String, Object> createPayload(String username, String email) {
        return Map.of(
                "username", username,
                "hoTen", "Nguoi Dung",
                "email", email,
                "dienThoai", "0909",
                "donViId", donViId,
                "chucVu", "Nhan vien",
                "nhomQuyenId", nhomQuyenId,
                "azureAdId", "azure-" + username
        );
    }
}
