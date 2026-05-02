package com.qlda.documentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.entity.HoSoCongViec;
import com.qlda.documentservice.specification.HoSoCongViecSpecification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CaseFileSpecificationDataJpaTest {

    @Autowired
    private HoSoCongViecRepository hoSoCongViecRepository;

    @Test
    void repositoryAndSpecification_shouldFilterByKeywordAndOwner() {
        HoSoCongViec a = new HoSoCongViec();
        a.setMaHoSo("HS-001");
        a.setTenHoSo("Ho so cap phep");
        a.setDonViId(1);
        a.setNguoiPhuTrachId(10L);
        a.setTrangThai(1);
        a.setGhiChu("[NhomHoSo:NHOM-A]");
        hoSoCongViecRepository.save(a);

        HoSoCongViec b = new HoSoCongViec();
        b.setMaHoSo("HS-002");
        b.setTenHoSo("Ho so khac");
        b.setDonViId(2);
        b.setNguoiPhuTrachId(20L);
        b.setTrangThai(0);
        hoSoCongViecRepository.save(b);

        assertThat(hoSoCongViecRepository.existsByMaHoSo("HS-001")).isTrue();

        List<HoSoCongViec> filtered = hoSoCongViecRepository.findAll(
            HoSoCongViecSpecification.keyword("cap phep")
                .and(HoSoCongViecSpecification.donViId(1))
                .and(HoSoCongViecSpecification.nguoiPhuTrachId(10L))
                .and(HoSoCongViecSpecification.trangThai(1))
                .and(HoSoCongViecSpecification.nhomHoSoKeyword("NHOM-A"))
        );

        assertThat(filtered).hasSize(1);
        assertThat(filtered.getFirst().getMaHoSo()).isEqualTo("HS-001");
    }
}
