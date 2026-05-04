package com.qlda.authservice;

import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class InternalAuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonViRepository donViRepository;

    @Autowired
    private NhomQuyenRepository nhomQuyenRepository;

    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    private Long userId;

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
        userId = nguoiDungRepository.save(user).getId();
    }

    @Test
    void internalEndpointShouldReturn401WhenMissingAuthorization() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void internalEndpointShouldReturn401WhenInvalidToken() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("Authorization", "Bearer wrong-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void internalEndpointShouldReturn403WhenMissingServiceName() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("Authorization", "Bearer test-internal-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void internalEndpointShouldReturn403WhenServiceNameIsNotAllowed() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "unauthorized-service"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void internalEndpointShouldPassWhenTokenAndServiceAreValid() throws Exception {
        mockMvc.perform(get("/internal/auth/users/{id}", userId)
                        .header("Authorization", "Bearer test-internal-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
