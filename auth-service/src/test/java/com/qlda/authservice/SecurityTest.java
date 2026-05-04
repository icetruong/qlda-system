package com.qlda.authservice;

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
        "internal.auth.allowed-services[0]=document-service"
})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
    void loginAzureShouldBePermitAll() throws Exception {
        mockMvc.perform(post("/api/auth/login/azure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void meEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void meEndpointShouldReturn200WithValidUserJwt() throws Exception {
        NguoiDung user = nguoiDungRepository.findByUserName("admin").orElseThrow();
        String accessToken = jwtService.generateAccessToken(user);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void internalEndpointShouldRejectUserJwtWithoutInternalToken() throws Exception {
        NguoiDung user = nguoiDungRepository.findByUserName("admin").orElseThrow();
        String userJwt = jwtService.generateAccessToken(user);

        mockMvc.perform(get("/internal/auth/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + userJwt)
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INTERNAL_TOKEN"));
    }
}
