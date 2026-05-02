package com.qlda.workflowservice.repository;

import com.qlda.workflowservice.entity.BuocQuyTrinh;
import com.qlda.workflowservice.entity.QuyTrinh;
import com.qlda.workflowservice.entity.XuLyVanBan;
import com.qlda.workflowservice.specification.QuyTrinhSpecification;
import com.qlda.workflowservice.specification.XuLyVanBanSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=true"
})
@ActiveProfiles("test")
class WorkflowRepositorySpecificationDataJpaTest {

    @Autowired
    private QuyTrinhRepository quyTrinhRepository;
    @Autowired
    private BuocQuyTrinhRepository buocQuyTrinhRepository;
    @Autowired
    private XuLyVanBanRepository xuLyVanBanRepository;

    private QuyTrinh quyTrinh1;
    private QuyTrinh quyTrinh2;
    private BuocQuyTrinh step1;
    private BuocQuyTrinh step2;

    @BeforeEach
    void initData() {
        xuLyVanBanRepository.deleteAll();
        buocQuyTrinhRepository.deleteAll();
        quyTrinhRepository.deleteAll();

        quyTrinh1 = quyTrinhRepository.save(QuyTrinh.builder()
                .maQuyTrinh("QT_DEN")
                .tenQuyTrinh("Quy trinh den")
                .loaiVanBanId(1)
                .moTa("Xu ly van ban den")
                .soBuoc(2)
                .suDung(true)
                .build());

        quyTrinh2 = quyTrinhRepository.save(QuyTrinh.builder()
                .maQuyTrinh("QT_DI")
                .tenQuyTrinh("Quy trinh di")
                .loaiVanBanId(2)
                .moTa("Xu ly van ban di")
                .soBuoc(1)
                .suDung(false)
                .build());

        step1 = buocQuyTrinhRepository.save(BuocQuyTrinh.builder()
                .quyTrinh(quyTrinh1)
                .tenBuoc("Tiep nhan")
                .thuTuBuoc(1)
                .thoiGianXuLy(8)
                .batBuocPheDuyet(true)
                .build());

        step2 = buocQuyTrinhRepository.save(BuocQuyTrinh.builder()
                .quyTrinh(quyTrinh1)
                .tenBuoc("Phe duyet")
                .thuTuBuoc(2)
                .thoiGianXuLy(24)
                .batBuocPheDuyet(true)
                .build());

        xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(100L)
                .buocQuyTrinh(step1)
                .nguoiGuiId(1L)
                .nguoiNhanId(2L)
                .donViXuLyId(5)
                .hanhDongXuLy("TRANSFER")
                .yKienXuLy("Can xu ly")
                .ngayNhan(LocalDateTime.now().minusHours(2))
                .hanXuLy(LocalDateTime.now().plusHours(2))
                .trangThaiXuLy(1)
                .tyLeHoanThanh(30)
                .build());

        xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(100L)
                .buocQuyTrinh(step2)
                .nguoiGuiId(2L)
                .nguoiNhanId(3L)
                .donViXuLyId(6)
                .hanhDongXuLy("APPROVE")
                .yKienXuLy("Done")
                .ngayNhan(LocalDateTime.now().minusHours(10))
                .hanXuLy(LocalDateTime.now().minusHours(5))
                .ngayHoanThanh(LocalDateTime.now().minusHours(3))
                .trangThaiXuLy(2)
                .tyLeHoanThanh(100)
                .build());

        xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(200L)
                .buocQuyTrinh(step1)
                .nguoiGuiId(5L)
                .nguoiNhanId(2L)
                .donViXuLyId(5)
                .hanhDongXuLy("TRANSFER")
                .yKienXuLy("Qua han")
                .ngayNhan(LocalDateTime.now().minusDays(1))
                .hanXuLy(LocalDateTime.now().minusHours(1))
                .trangThaiXuLy(1)
                .tyLeHoanThanh(50)
                .build());
    }

    @Test
    void quyTrinhRepository_shouldFindByCodeAndExists() {
        assertTrue(quyTrinhRepository.findByMaQuyTrinh("QT_DEN").isPresent());
        assertTrue(quyTrinhRepository.existsByMaQuyTrinh("QT_DEN"));
        assertTrue(quyTrinhRepository.findByIdAndSuDungTrue(quyTrinh1.getId()).isPresent());
        assertTrue(quyTrinhRepository.findByIdAndSuDungTrue(quyTrinh2.getId()).isEmpty());
    }

    @Test
    void buocQuyTrinhRepository_shouldQueryStepOrderAndCount() {
        List<BuocQuyTrinh> steps = buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(quyTrinh1.getId());
        assertEquals(2, steps.size());
        assertEquals(1, steps.get(0).getThuTuBuoc());
        assertTrue(buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(step1.getId(), quyTrinh1.getId()).isPresent());
        assertTrue(buocQuyTrinhRepository.existsByQuyTrinh_IdAndThuTuBuoc(quyTrinh1.getId(), 1));
        assertEquals(2L, buocQuyTrinhRepository.countByQuyTrinh_Id(quyTrinh1.getId()));
    }

    @Test
    void xuLyVanBanRepository_shouldQueryByMethods() {
        assertTrue(xuLyVanBanRepository.findByVanBanIdOrderByNgayNhanAsc(100L).size() >= 2);
        assertTrue(xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(100L).size() >= 2);
        assertNotNull(xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(100L).orElseThrow());
        assertEquals(1, xuLyVanBanRepository.findByNguoiNhanIdAndTrangThaiXuLy(3L, 2, PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void quyTrinhSpecification_filterShouldWork() {
        List<QuyTrinh> result = quyTrinhRepository.findAll(QuyTrinhSpecification.filter("den", 1, true));
        assertEquals(1, result.size());
        assertEquals("QT_DEN", result.getFirst().getMaQuyTrinh());
    }

    @Test
    void xuLyVanBanSpecification_pendingApprovalsShouldWork() {
        var spec = XuLyVanBanSpecification.pendingApprovals(2L, "transfer",
                LocalDate.now().minusDays(2), LocalDate.now());

        List<XuLyVanBan> result = xuLyVanBanRepository.findAll(spec);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(it -> it.getNguoiNhanId().equals(2L)));
        assertTrue(result.stream().allMatch(it -> it.getTrangThaiXuLy() == 1));
    }

    @Test
    void xuLyVanBanSpecification_deadlineAndViolationShouldWork() {
        long nearDeadline = xuLyVanBanRepository.count(
                XuLyVanBanSpecification.nearDeadline(LocalDateTime.now(), 3));
        long overdue = xuLyVanBanRepository.count(
                XuLyVanBanSpecification.overdue(LocalDateTime.now()));
        List<XuLyVanBan> violations = xuLyVanBanRepository.findAll(
                XuLyVanBanSpecification.slaViolations(LocalDate.now().minusDays(2), LocalDate.now(), 5));

        assertTrue(nearDeadline >= 1);
        assertTrue(overdue >= 1);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(it -> it.getDonViXuLyId().equals(5)));
    }
}
