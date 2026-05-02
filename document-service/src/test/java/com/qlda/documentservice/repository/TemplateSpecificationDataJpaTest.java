package com.qlda.documentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TemplateVanBan;
import com.qlda.documentservice.specification.TemplateVanBanSpecification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TemplateSpecificationDataJpaTest {

    @Autowired
    private TemplateVanBanRepository templateVanBanRepository;

    @Autowired
    private LoaiVanBanRepository loaiVanBanRepository;

    @Test
    void customMethodsAndSpecification_shouldFilterCorrectly() {
        LoaiVanBan type1 = new LoaiVanBan();
        type1.setMaLoaiVanBan("CV");
        type1.setTenLoaiVanBan("Cong Van");
        type1.setSuDung(true);
        type1 = loaiVanBanRepository.save(type1);

        TemplateVanBan template1 = new TemplateVanBan();
        template1.setMaTemplate("TMP-A");
        template1.setTenTemplate("Template A");
        template1.setLoaiVanBan(type1);
        template1.setSuDung(true);
        templateVanBanRepository.save(template1);

        TemplateVanBan template2 = new TemplateVanBan();
        template2.setMaTemplate("TMP-B");
        template2.setTenTemplate("Template B");
        template2.setLoaiVanBan(type1);
        template2.setSuDung(false);
        templateVanBanRepository.save(template2);

        assertThat(templateVanBanRepository.existsByMaTemplate("TMP-A")).isTrue();
        assertThat(templateVanBanRepository.findByIdAndSuDungTrue(template2.getId())).isEmpty();

        List<TemplateVanBan> filtered = templateVanBanRepository.findAll(
            TemplateVanBanSpecification.keyword("tmp-a")
                .and(TemplateVanBanSpecification.loaiVanBanId(type1.getId()))
                .and(TemplateVanBanSpecification.suDung(true))
        );

        assertThat(filtered).hasSize(1);
        assertThat(filtered.getFirst().getMaTemplate()).isEqualTo("TMP-A");
    }
}
