package com.qlda.documentservice.specification;

import com.qlda.documentservice.entity.HoSoCongViec;
import org.springframework.data.jpa.domain.Specification;

public final class HoSoCongViecSpecification {
    private HoSoCongViecSpecification() {
    }

    public static Specification<HoSoCongViec> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("maHoSo")), likeValue),
                cb.like(cb.lower(root.get("tenHoSo")), likeValue)
            );
        };
    }

    public static Specification<HoSoCongViec> donViId(Integer donViId) {
        return (root, query, cb) -> donViId == null ? cb.conjunction() : cb.equal(root.get("donViId"), donViId);
    }

    public static Specification<HoSoCongViec> nguoiPhuTrachId(Long nguoiPhuTrachId) {
        return (root, query, cb) -> nguoiPhuTrachId == null
            ? cb.conjunction()
            : cb.equal(root.get("nguoiPhuTrachId"), nguoiPhuTrachId);
    }

    public static Specification<HoSoCongViec> trangThai(Integer trangThai) {
        return (root, query, cb) -> trangThai == null ? cb.conjunction() : cb.equal(root.get("trangThai"), trangThai);
    }

    public static Specification<HoSoCongViec> nhomHoSoKeyword(String nhomHoSo) {
        return (root, query, cb) -> {
            if (nhomHoSo == null || nhomHoSo.isBlank()) {
                return cb.conjunction();
            }
            String likeValue = "%" + nhomHoSo.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("ghiChu")), likeValue);
        };
    }
}

