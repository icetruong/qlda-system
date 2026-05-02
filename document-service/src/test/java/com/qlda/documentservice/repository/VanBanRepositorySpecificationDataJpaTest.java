package com.qlda.documentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.specification.VanBanSpecification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class VanBanRepositorySpecificationDataJpaTest {

    @Autowired
    private VanBanRepository vanBanRepository;

    @Autowired
    private LoaiVanBanRepository loaiVanBanRepository;

    @Test
    void customMethods_shouldRespectDaXoaFlag() {
        LoaiVanBan type = new LoaiVanBan();
        type.setMaLoaiVanBan("CV");
        type.setTenLoaiVanBan("Cong van");
        type.setSuDung(true);
        type = loaiVanBanRepository.save(type);

        VanBan active = new VanBan();
        active.setTrichYeu("Active");
        active.setSoKyHieu("01/CV/2026");
        active.setLoaiVanBan(type);
        active.setDaXoa(false);
        active.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        active.setNgayTao(LocalDateTime.of(2026, 1, 10, 0, 0));
        vanBanRepository.save(active);

        VanBan deleted = new VanBan();
        deleted.setTrichYeu("Deleted");
        deleted.setSoKyHieu("01/DEL/2026");
        deleted.setLoaiVanBan(type);
        deleted.setDaXoa(true);
        deleted.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        deleted.setNgayTao(LocalDateTime.of(2026, 1, 11, 0, 0));
        vanBanRepository.save(deleted);

        assertThat(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("01/CV/2026")).isTrue();
        assertThat(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("01/DEL/2026")).isFalse();
        assertThat(vanBanRepository.findByIdAndDaXoaFalse(deleted.getId())).isEmpty();
    }

    @Test
    void specification_shouldFilterByKeywordTypeAndDate() {
        LoaiVanBan typeA = new LoaiVanBan();
        typeA.setMaLoaiVanBan("QD");
        typeA.setTenLoaiVanBan("Quyet dinh");
        typeA.setSuDung(true);
        typeA = loaiVanBanRepository.save(typeA);

        VanBan matched = new VanBan();
        matched.setTrichYeu("Thong bao hop");
        matched.setSoKyHieu("02/QD/2026");
        matched.setLoaiVanBan(typeA);
        matched.setDaXoa(false);
        matched.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        matched.setNgayTao(LocalDateTime.of(2026, 4, 20, 0, 0));
        vanBanRepository.save(matched);

        VanBan notMatched = new VanBan();
        notMatched.setTrichYeu("Cong van khac");
        notMatched.setSoKyHieu("03/CV/2026");
        notMatched.setLoaiVanBan(typeA);
        notMatched.setDaXoa(false);
        notMatched.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DI);
        notMatched.setNgayTao(LocalDateTime.of(2026, 1, 5, 0, 0));
        vanBanRepository.save(notMatched);

        List<VanBan> results = vanBanRepository.findAll(
            VanBanSpecification.phanLoai(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN)
                .and(VanBanSpecification.daXoaFalse())
                .and(VanBanSpecification.keyword("thong bao"))
                .and(VanBanSpecification.loaiVanBanId(typeA.getId()))
                .and(VanBanSpecification.ngayTaoBetween(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)))
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getSoKyHieu()).isEqualTo("02/QD/2026");
    }
}
