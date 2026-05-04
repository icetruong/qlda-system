package com.qlda.workflowservice.specification;

import com.qlda.workflowservice.entity.XuLyVanBan;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class XuLyVanBanSpecification {
    private XuLyVanBanSpecification() {
    }

    public static Specification<XuLyVanBan> pendingApprovals(Long nguoiNhanId, String keyword, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            var predicates = cb.and(
                    cb.equal(root.get("nguoiNhanId"), nguoiNhanId),
                    cb.equal(root.get("trangThaiXuLy"), 1)
            );
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("hanhDongXuLy")), like),
                        cb.like(cb.lower(root.get("yKienXuLy")), like)
                ));
            }
            if (fromDate != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("ngayNhan"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("ngayNhan"), toDate.atTime(23, 59, 59)));
            }
            return predicates;
        };
    }

    public static Specification<XuLyVanBan> nearDeadline(LocalDateTime checkDate, int beforeHours) {
        LocalDateTime from = checkDate;
        LocalDateTime to = checkDate.plusHours(beforeHours);
        return (root, query, cb) -> cb.and(
                cb.isNull(root.get("ngayHoanThanh")),
                cb.isNotNull(root.get("hanXuLy")),
                cb.between(root.get("hanXuLy"), from, to)
        );
    }

    public static Specification<XuLyVanBan> overdue(LocalDateTime checkDate) {
        return (root, query, cb) -> cb.and(
                cb.isNull(root.get("ngayHoanThanh")),
                cb.isNotNull(root.get("hanXuLy")),
                cb.lessThan(root.get("hanXuLy"), checkDate)
        );
    }

    public static Specification<XuLyVanBan> slaViolations(LocalDate fromDate, LocalDate toDate, Integer donViId) {
        return (root, query, cb) -> {
            var predicates = cb.and(
                    cb.isNotNull(root.get("hanXuLy")),
                    cb.or(
                            cb.isNull(root.get("ngayHoanThanh")),
                            cb.greaterThan(root.get("ngayHoanThanh"), root.get("hanXuLy"))
                    )
            );
            if (fromDate != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("hanXuLy"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("hanXuLy"), toDate.atTime(23, 59, 59)));
            }
            if (donViId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("donViXuLyId"), donViId));
            }
            return predicates;
        };
    }

    public static Specification<XuLyVanBan> internalFilter(LocalDate fromDate, LocalDate toDate, Integer donViId, Long nguoiXuLyId) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (fromDate != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("ngayNhan"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("ngayNhan"), toDate.atTime(23, 59, 59)));
            }
            if (donViId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("donViXuLyId"), donViId));
            }
            if (nguoiXuLyId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("nguoiNhanId"), nguoiXuLyId));
            }
            return predicates;
        };
    }
}
