package com.qlda.documentservice.specification;

import com.qlda.documentservice.entity.VanBan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class VanBanSpecification {
    private VanBanSpecification() {
    }

    public static Specification<VanBan> phanLoai(Integer phanLoai) {
        return (root, query, cb) -> cb.equal(root.get("phanLoaiVanBan"), phanLoai);
    }

    public static Specification<VanBan> daXoaFalse() {
        return (root, query, cb) -> cb.isFalse(root.get("daXoa"));
    }

    public static Specification<VanBan> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("trichYeu")), likeValue),
                cb.like(cb.lower(root.get("soKyHieu")), likeValue)
            );
        };
    }

    public static Specification<VanBan> loaiVanBanId(Integer loaiVanBanId) {
        return (root, query, cb) -> loaiVanBanId == null
            ? cb.conjunction()
            : cb.equal(root.get("loaiVanBan").get("id"), loaiVanBanId);
    }

    public static Specification<VanBan> donViChuTriId(Integer donViChuTriId) {
        return (root, query, cb) -> donViChuTriId == null
            ? cb.conjunction()
            : cb.equal(root.get("donViChuTriId"), donViChuTriId);
    }

    public static Specification<VanBan> trangThai(Integer trangThai) {
        return (root, query, cb) -> trangThai == null ? cb.conjunction() : cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<VanBan> ngayTaoBetween(LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            if (fromDate == null && toDate == null) {
                return cb.conjunction();
            }
            LocalDateTime from = fromDate == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : fromDate.atStartOfDay();
            LocalDateTime to = toDate == null ? LocalDateTime.now().plusYears(100) : toDate.plusDays(1).atStartOfDay();
            return cb.between(root.get("ngayTao"), from, to);
        };
    }
}

