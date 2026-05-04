package com.qlda.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.authservice.entity.ChucNang;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.entity.PhanQuyen;
import com.qlda.authservice.repository.ChucNangRepository;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "internal.auth.token=test-internal-token",
        "internal.auth.allowed-services[0]=document-service",
        "internal.auth.allowed-services[1]=workflow-service",
        "internal.auth.allowed-services[2]=report-service",
        "internal.auth.allowed-services[3]=notification-service",
        "internal.auth.allowed-services[4]=support-service"
})
class InternalAuthControllerTest {

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
    private ChucNangRepository chucNangRepository;

    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    private Long userId;
    private Integer unitId;

    @BeforeEach
    void setupData() {
        phanQuyenRepository.deleteAll();
        nguoiDungRepository.deleteAll();
        chucNangRepository.deleteAll();
        nhomQuyenRepository.deleteAll();
        donViRepository.deleteAll();

        DonVi donVi = new DonVi();
        donVi.setMaDonVi("DV01");
        donVi.setTenDonVi("Phong HC");
        donVi.setSuDung(true);
        DonVi savedDonVi = donViRepository.save(donVi);
        unitId = savedDonVi.getId();

        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setMaNhomQuyen("CHUYEN_VIEN");
        nhomQuyen.setTenNhomQuyen("Chuyen vien");
        nhomQuyen.setSuDung(true);
        NhomQuyen savedRole = nhomQuyenRepository.save(nhomQuyen);

        NguoiDung user = new NguoiDung();
        user.setUserName("nva");
        user.setHoTen("Nguyen Van A");
        user.setEmail("nva@company.com");
        user.setDonVi(savedDonVi);
        user.setNhomQuyen(savedRole);
        user.setTrangThai(1);
        userId = nguoiDungRepository.save(user).getId();

        ChucNang chucNang = new ChucNang();
        chucNang.setMaChucNang("DOCUMENT_INCOMING");
        chucNang.setTenChucNang("Van ban den");
        chucNang.setSuDung(true);
        ChucNang savedFunction = chucNangRepository.save(chucNang);

        PhanQuyen phanQuyen = new PhanQuyen();
        phanQuyen.setNhomQuyen(savedRole);
        phanQuyen.setChucNang(savedFunction);
        phanQuyen.setIsView(true);
        phanQuyen.setIsCreate(false);
        phanQuyen.setIsEdit(true);
        phanQuyen.setIsDelete(false);
        phanQuyen.setIsApprove(false);
        phanQuyenRepository.save(phanQuyen);
    }

    @Test
    void getInternalUserShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("nva"));
    }

    @Test
    void validateUsersShouldReturnSuccess() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("userIds", new Long[]{userId, 999L}));
        mockMvc.perform(post("/internal/auth/users/validate")
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.invalidUserIds[0]").value(999));
    }

    @Test
    void getInternalUnitShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/internal/auth/units/{id}", unitId)
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(unitId))
                .andExpect(jsonPath("$.data.maDonVi").value("DV01"));
    }

    @Test
    void validateUnitsShouldReturnSuccess() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("unitIds", new Integer[]{unitId, 777}));
        mockMvc.perform(post("/internal/auth/units/validate")
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.invalidUnitIds[0]").value(777));
    }

    @Test
    void getUserRolesShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}/roles", userId)
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.roles[0]").value("CHUYEN_VIEN"))
                .andExpect(jsonPath("$.data.permissions[0].maChucNang").value("DOCUMENT_INCOMING"));
    }

    @Test
    void checkPermissionShouldReturnAllowedTrue() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "maChucNang", "DOCUMENT_INCOMING",
                "permission", "IsEdit"
        ));

        mockMvc.perform(post("/internal/auth/permissions/check")
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allowed").value(true));
    }

    @Test
    void checkPermissionShouldReturnAllowedFalse() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "maChucNang", "DOCUMENT_INCOMING",
                "permission", "IsDelete"
        ));

        mockMvc.perform(post("/internal/auth/permissions/check")
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allowed").value(false));
    }
}
